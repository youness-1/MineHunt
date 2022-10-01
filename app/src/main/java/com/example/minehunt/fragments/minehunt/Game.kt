package com.example.minehunt.fragments.minehunt

import android.media.MediaPlayer
import java.util.concurrent.atomic.AtomicBoolean
import android.content.Context
import android.widget.*
import com.example.minehunt.Player
import com.example.minehunt.R
import com.example.minehunt.fragments.minehunt.search.Heuristic
import com.example.minehunt.fragments.minehunt.search.Search

enum class Item(val probability: Int) {
    MINE(10),
    LIFE(5),
    SHIELD(5),
    SKULL(4),
    COIN(7),
    EXPLODED(0),
    EXIT(0),
    EMPTY(69)
}

interface GameData {
    val grid : List<List<Tile>>
    val lastMove : Directions
    val state : MutableMap<String,Int>
}

data class Tile(var type: TileType, var content: Item)
class Game(var gridView: GridView, val player: Player, private val context: Context, private val textBar: TextView, private val startButton: Button, private val spinDifficulty: Spinner, private val spinSize: Spinner, private val editName: EditText, var playerCoords: Pair<Int, Int> = Pair(-1,-1), var exit: Pair<Int,Int> = Pair(-1,-1)) :
    GameData {
    var running = AtomicBoolean(false)
    var animation = AtomicBoolean(false)
    var animationTime = 120L
    private var foundExit=false
    lateinit var mediaPlayer: MediaPlayer
    var gameData : GameData? = null
    override var grid=gridView.grid.map { it.map { Tile(it.type,it.content) } }
    override var lastMove=Directions.DOWN
    override var state=mutableMapOf("life" to 1, "score" to 0, "difficulty" to 0, "shield" to 0, "undead" to 0)
    private val UNDEAD_TURNS=5

    private fun getBarText(): String {
        var text="${player.name} | near mines: ${getAdjacentBombs()} "
        for (i in state.keys){
            if(i!="difficulty")
                text+="$i: ${if (i=="life" && state[i]==-1) "???" else state[i]} "
            if (i=="life")
                text+="\n"
        }
        return text
    }
    fun start(difficulty: Int){
        editName.setText(player.name)
        state=mutableMapOf("life" to 1, "score" to 0, "difficulty" to 0, "shield" to 0, "undead" to 0)
        if (this::mediaPlayer.isInitialized && mediaPlayer.isPlaying)
            mediaPlayer.stop()
        mediaPlayer = MediaPlayer.create(context, R.raw.game)
        mediaPlayer.isLooping=true
        mediaPlayer.start()
        lastMove=Directions.DOWN
        foundExit=false
        when(difficulty){
            0 -> state["life"]=3
            1 -> state["life"]=2
            2 -> state["life"]=1
        }
        state["difficulty"]=difficulty
        initGraph(gridView, state["difficulty"]!!)
        val astar = Search(gridView, Heuristic.MANHATTAN)
        while (!astar.run()){
            initGraph(gridView,state["difficulty"]!!)
        }
        running.set(true)
        grid=gridView.grid.map { it.map { Tile(it.type,it.content) } }
        textBar.text=getBarText()
    }


    private fun message(text: String){
        println(text)
        Toast.makeText(context,text,Toast.LENGTH_SHORT).show()
    }

    private fun revealMap(){
        for(i in gridView.grid.indices){
            for(j in gridView.grid[i].indices) {
                if (gridView.grid[i][j].type != TileType.PLAYER)
                    gridView.grid[i][j].type=TileType.MINED
            }
        }
        gridView.invalidate()
    }
    fun loose(){
        revealMap()
        state["life"] = 0
        gameData=null
        player.undead=false
        running.set(false)
        player.looses+=1
        mediaPlayer.stop()
        mediaPlayer = MediaPlayer.create(context, R.raw.dead)
        mediaPlayer.isLooping=false
        mediaPlayer.start()
        textBar.text=player.toString()
        makeSelectable()
    }

    private fun makeSelectable(){
        startButton.text="Play"
        spinDifficulty.isEnabled=true
        spinSize.isEnabled=true
        editName.isEnabled=true
    }

    private fun win(){
        revealMap()
        gameData=null
        player.undead=false
        running.set(false)
        player.wins+=1
        mediaPlayer.stop()
        mediaPlayer = MediaPlayer.create(context, R.raw.win)
        mediaPlayer.isLooping=false
        mediaPlayer.start()
        message("You Won!")
        message("${state["score"]} added to your balance")
        player.score+=state["score"]!!
        textBar.text=player.toString()
        makeSelectable()
    }
    private fun getAdjacentBombs(): Int{
        var bombs=0
        val corner=arrayOf(Pair(1,1),Pair(-1,-1),Pair(-1,1),Pair(1,-1))
        for(direction in Directions.values()){
            val i = direction.type.first+playerCoords.first
            val j = direction.type.second+playerCoords.second
            if (i in gridView.grid.indices && j in gridView.grid[i].indices){
                if (gridView.grid[i][j].content==Item.MINE)
                    bombs+=1
            }
        }
        for(direction in corner){
            val i = direction.first+playerCoords.first
            val j = direction.second+playerCoords.second
            if (i in gridView.grid.indices && j in gridView.grid[i].indices){
                if (gridView.grid[i][j].content==Item.MINE)
                    bombs+=1
            }
        }
        return bombs
    }
    private fun initGraph(gridView: GridView, difficulty: Int, player: Pair<Int, Int> = Pair(-1,-1), exit: Pair<Int,Int> = Pair(-1,-1)){
        for(i in gridView.grid.indices) {
            val rowArray = mutableListOf<Tile>()
            for(j in gridView.grid[i].indices) {
                val randomNum = (1..Item.values().sumOf { if (it==Item.MINE) it.probability*(difficulty+1) else if (it==Item.SKULL && difficulty!=2) 0 else it.probability }).random()
                var count=0
                for(item in Item.values()){
                    count+=if (item==Item.MINE) item.probability*(difficulty+1) else if (item==Item.SKULL && difficulty!=2) 0 else item.probability
                    if(count>=randomNum){
                        rowArray.add(Tile(if (randomNum%3==0) TileType.MINABLE2 else TileType.MINABLE, item))
                        break
                    }
                }
            }
            gridView.grid[i] = rowArray
        }
        this.playerCoords = if(player == Pair(-1,-1)) Pair(0, (0 until gridView.grid[0].size).random()) else player
        this.exit = if(exit == Pair(-1,-1)) Pair(gridView.grid.indices.last, (0 until gridView.grid[0].size).random()) else exit
        println(this.playerCoords)
        println(this.exit)
        gridView.grid[this.playerCoords.first][this.playerCoords.second]=Tile(TileType.PLAYER, Item.EMPTY)
        if (difficulty==2)
            gridView.grid[this.exit.first][this.exit.second]=Tile(TileType.MINABLE, Item.EXIT)
        else
            gridView.grid[this.exit.first][this.exit.second]=Tile(TileType.MINED, Item.EXIT)
    }
    fun resume(){
        if (gameData!=null){
            mediaPlayer = if (!player.undead) MediaPlayer.create(context, R.raw.game) else MediaPlayer.create(context, R.raw.undead)
            mediaPlayer.isLooping=true
            mediaPlayer.start()
            running.set(true)
            lastMove= gameData!!.lastMove
            state= gameData!!.state.toMutableMap()
            for (i in gridView.grid.indices) {
                val rowArray = mutableListOf<Tile>()
                for (j in gridView.grid[i].indices) {
                    if (i in gameData!!.grid.indices && j in gameData!!.grid[i].indices) {
                        rowArray.add(gameData!!.grid[i][j])
                        println(gameData!!.grid[i][j].type.name)
                        println(gameData!!.grid[i][j].content.name)
                        if (gameData!!.grid[i][j].type == TileType.PLAYER)
                            playerCoords = Pair(i, j)
                        if (gameData!!.grid[i][j].content == Item.EXIT)
                            exit = Pair(i, j)
                    }
                }
                gridView.grid[i] = rowArray
            }
            gameData=null
            grid=gridView.grid.map { it.map { Tile(it.type,it.content) } }
            textBar.text=getBarText()
            editName.setText(player.name)
        }
    }
    fun resume(game: GameData){
        this.gameData=game
    }
    private fun animate(tile: Tile, array: Array<Item>){
        val animation= object :Thread(){
            override fun run() {
                animation.set(true)
                for(i in array.indices){
                    try {
                        tile.content=array[i]
                        gridView.postInvalidate()
                        sleep(animationTime)
                    } catch (e: Exception){
                        println("Fragment changed")
                    }
                }
                animation.set(false)
                super.run()
            }
        }
        animation.start()
    }
    fun move(direction: Directions){
        lastMove=direction
        val toX=playerCoords.first + direction.type.first
        val toY=playerCoords.second + direction.type.second
        if(toX in gridView.grid.indices && toY in gridView.grid[0].indices) {
            val toTile = gridView.grid[toX][toY]
            if (toTile.type == TileType.MINED){
                when(toTile.content){
                    Item.LIFE -> {
                        state["life"] = if(state["life"] == 3 || player.undead) state["life"]!! else state["life"]!! + 1
                        MediaPlayer.create(context, R.raw.life).start()
                    }
                    Item.SHIELD -> {
                        MediaPlayer.create(context, R.raw.shield).start()
                        state["shield"] = 1

                    }
                    Item.SKULL -> {
                        state["undead"] = UNDEAD_TURNS
                        mediaPlayer.stop()
                        mediaPlayer = MediaPlayer.create(context, R.raw.undead)
                        mediaPlayer.start()
                        state["life"]=-1
                        player.undead=true
                    }
                    Item.COIN -> {
                        state["score"] = state["score"]!! + (state["difficulty"]!! + 1)
                        MediaPlayer.create(context, R.raw.coin).start()
                    }
                    else -> {}
                }
                gridView.grid[playerCoords.first][playerCoords.second] = Tile(TileType.MINED, Item.EMPTY)
                playerCoords = Pair(
                    playerCoords.first + direction.type.first,
                    playerCoords.second + direction.type.second
                )
                gridView.grid[playerCoords.first][playerCoords.second].type=TileType.PLAYER
                if(playerCoords.first==exit.first && playerCoords.second == exit.second){
                    running.set(false)
                    println("Vittoria!")
                    gameData=null
                    win()
                    return
                }
            }
            else{
                if(state["undead"]!! > 0){
                    state["undead"]=state["undead"]!!-1
                }
                toTile.type = TileType.MINED
                if (toTile.content==Item.MINE) {
                    if (state["shield"]==1)
                        state["shield"]=0
                    else if (!player.undead)
                        state["life"] = state["life"]!! - 1
                    MediaPlayer.create(context, R.raw.explosion).start()
                    animate(toTile, arrayOf(Item.MINE, Item.EXPLODED, Item.EMPTY))
                    if ((state["life"]!!) == 0) {
                        running.set(false)
                        message("${player.name} blew up :(")
                        loose()
                    }
                    toTile.content = Item.EMPTY
                }
            }
            if(player.undead && state["undead"]!! == 0){
                running.set(false)
                message("${player.name} died of undead curse")
                loose()
            }
            grid=gridView.grid.map { it.map { Tile(it.type,it.content) } }
            textBar.text=getBarText()
        }
        gridView.invalidate()
    }
}