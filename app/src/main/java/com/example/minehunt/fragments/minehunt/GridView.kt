package com.example.minehunt.fragments.minehunt

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.minehunt.R

enum class TileType{
    MINABLE,
    MINABLE2,
    MINED,
    PLAYER
}

enum class Directions(val type: Pair<Int,Int>) {
    DOWN(Pair(1,0)),
    UP(Pair(-1,0)),
    RIGHT(Pair(0,1)),
    LEFT(Pair(0,-1))
}

class GridView(context: Context, attributeSet: AttributeSet): View(context, attributeSet){
    private val paint :Paint = Paint()
    lateinit var game: Game
    var tileSize = 40f
        set(value){
            field=value
            this.rows = height/field.toInt()
            this.columns= width/field.toInt()

            this.grid = Array(rows) { mutableListOf() }
            for(i in grid.indices) {
                val rowArray = mutableListOf<Tile>()
                for(j in 0 until columns) {
                    val random=(0..100).random()
                    rowArray.add(Tile(if(random < 30) TileType.MINABLE2 else TileType.MINABLE, Item.EMPTY))
                }
                grid[i] = rowArray
            }
            if (rows > 0 && columns > 0)
                grid[0][0]=Tile(TileType.PLAYER, Item.EMPTY)
            this.paddingTop = (height-(rows*tileSize))/2
            this.paddingLeft = (width-(columns*tileSize))/2
            terrainBitmap.forEach { (k, _) ->
                terrainBitmap[k] = terrainBitmap[k]?.let { Bitmap.createScaledBitmap(it, tileSize.toInt(), tileSize.toInt(), true) }
            }
            invalidate()
        }
    private var rows = 0
    private var columns = 0
    var paddingLeft=0f
    var paddingTop=0f


    private var move = false
    private var mx = -1f
    private var my = -1f


    var grid = Array(rows) { mutableListOf<Tile>() }

    private var terrainBitmap =  mutableMapOf("dirt" to BitmapFactory.decodeResource(resources, R.drawable.dirt),
        "stone" to BitmapFactory.decodeResource(resources, R.drawable.stone),
        "stone2" to BitmapFactory.decodeResource(resources, R.drawable.stone2)
    )
    private var itemsBitmap =  mutableMapOf("mine" to BitmapFactory.decodeResource(resources, R.drawable.bomb),
        "exploded" to BitmapFactory.decodeResource(resources, R.drawable.explosion),
        "life" to BitmapFactory.decodeResource(resources, R.drawable.heart),
        "coin" to BitmapFactory.decodeResource(resources, R.drawable.coin),
        "ladder" to BitmapFactory.decodeResource(resources, R.drawable.ladder),
        "shield" to BitmapFactory.decodeResource(resources, R.drawable.shield),
        "skull" to BitmapFactory.decodeResource(resources, R.drawable.skull)
    )
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        tileSize=tileSize
        game.resume()
        super.onSizeChanged(w, h, oldw, oldh)
    }
    private fun toGrayscale(bmpOriginal: Bitmap): Bitmap? {
        val height: Int = bmpOriginal.height
        val width: Int = bmpOriginal.width
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, null, Rect(0,0,width,height), paint)
        return bmpGrayscale
    }
    override fun onDraw(canvas: Canvas?) {
        val coords = object{
            var left = {j: Float -> (j*tileSize+paddingLeft)}
            var top = {i: Float -> (i*tileSize+paddingTop)}
            var right = {j: Float -> (left(j)+tileSize)}
            var bottom = {i: Float -> (top(i)+tileSize)}
        }
        val rawPlayerBitmap = BitmapFactory.decodeResource(resources,game.player.sprite.image)
        var playerSprite = when(game.lastMove){
            Directions.DOWN -> Bitmap.createBitmap(rawPlayerBitmap, 0, 0, rawPlayerBitmap.width/4, rawPlayerBitmap.height)
            Directions.UP -> Bitmap.createBitmap(rawPlayerBitmap, rawPlayerBitmap.width/4, 0, rawPlayerBitmap.width/4, rawPlayerBitmap.height)
            Directions.RIGHT -> Bitmap.createBitmap(rawPlayerBitmap, rawPlayerBitmap.width/4*2, 0, rawPlayerBitmap.width/4, rawPlayerBitmap.height)
            Directions.LEFT -> Bitmap.createBitmap(rawPlayerBitmap, rawPlayerBitmap.width/4*3, 0, rawPlayerBitmap.width/4, rawPlayerBitmap.height)
        }
        if (game.state["life"]==0){
            playerSprite=toGrayscale(playerSprite)!!
        }
        for(i in grid.indices) {
            for(j in grid[i].indices){
                paint.style = Paint.Style.STROKE; paint.setARGB(0,0,0,0)
                canvas?.drawRect(coords.left(j.toFloat()),coords.top(i.toFloat()),coords.right(j.toFloat()), coords.bottom(i.toFloat()), paint)
                when(grid[i][j].type){
                    TileType.MINED -> {
                        val itemBitmap = when(grid[i][j].content){
                            Item.EMPTY -> terrainBitmap["dirt"]!!
                            Item.MINE -> itemsBitmap["mine"]!!
                            Item.EXPLODED -> itemsBitmap["exploded"]!!
                            Item.LIFE -> itemsBitmap["life"]!!
                            Item.SHIELD -> itemsBitmap["shield"]!!
                            Item.SKULL -> itemsBitmap["skull"]!!
                            Item.COIN -> itemsBitmap["coin"]!!
                            Item.EXIT -> itemsBitmap["ladder"]!!
                        }
                        canvas?.drawBitmap(
                            terrainBitmap["dirt"]!!, null, Rect(
                                coords.left(j.toFloat()).toInt(),
                                coords.top(i.toFloat()).toInt(),
                                coords.right(j.toFloat()).toInt(),
                                coords.bottom(i.toFloat()).toInt()
                            ), null
                        )
                        canvas?.drawBitmap(
                            itemBitmap, null, Rect(
                                coords.left(j.toFloat()).toInt(),
                                coords.top(i.toFloat()).toInt(),
                                coords.right(j.toFloat()).toInt(),
                                coords.bottom(i.toFloat()).toInt()
                            ), null
                        )
                    }
                    TileType.MINABLE -> {
                        canvas?.drawBitmap(terrainBitmap["stone"]!!, null, Rect(coords.left(j.toFloat()).toInt(),
                        coords.top(i.toFloat()).toInt(),
                        coords.right(j.toFloat()).toInt(), coords.bottom(i.toFloat()).toInt()
                    ),null)}
                    TileType.MINABLE2 -> {
                        canvas?.drawBitmap(terrainBitmap["stone2"]!!, null, Rect(coords.left(j.toFloat()).toInt(),
                            coords.top(i.toFloat()).toInt(),
                            coords.right(j.toFloat()).toInt(), coords.bottom(i.toFloat()).toInt()
                        ),null)}
                    TileType.PLAYER -> {
                        canvas?.drawBitmap(terrainBitmap["dirt"]!!, null, Rect(coords.left(j.toFloat()).toInt(),
                            coords.top(i.toFloat()).toInt(),
                            coords.right(j.toFloat()).toInt(), coords.bottom(i.toFloat()).toInt()
                        ),null)
                        canvas?.drawBitmap(playerSprite!!, null, Rect(coords.left(j.toFloat()).toInt(),
                            coords.top(i.toFloat()).toInt(),
                            coords.right(j.toFloat()).toInt(), coords.bottom(i.toFloat()).toInt()
                        ),null)
                    }
                }

            }
        }
        super.onDraw(canvas)
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        if (!game.running.get() || game.animation.get()) return false
        if (motionEvent.x > (width-paddingLeft) || motionEvent.x < paddingLeft || motionEvent.y > (height-paddingTop) || motionEvent.y < paddingTop) return false //click outside canvas
        val x = ((motionEvent.x-paddingLeft) / tileSize).toInt() //conversion to grid coordinates
        val y = ((motionEvent.y-paddingTop) / tileSize).toInt()
        if (y>=grid.size || x>=grid[0].size) return false //checks if indexes are outside of bounds
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            mx = motionEvent.x
            my = motionEvent.y
            move = false
        }
        else if (motionEvent.action == MotionEvent.ACTION_MOVE){ //TODO and is
            if (mx - motionEvent.x > 100 && !move) {
                mx = motionEvent.x
                my = motionEvent.y
                move = true
                game.move(Directions.LEFT)
                //MainActivity.img_swipe.setVisibility(INVISIBLE); TODO
            } else if (motionEvent.x - mx > 100 && !move) {
                mx = motionEvent.x
                my = motionEvent.y
                move = true
                game.move(Directions.RIGHT)
            } else if (motionEvent.y - my > 100 && !move) {
                mx = motionEvent.x
                my = motionEvent.y
                move = true
                game.move(Directions.DOWN)
            } else if (my - motionEvent.y > 100 && !move) {
                mx = motionEvent.x
                my = motionEvent.y
                move = true
                game.move(Directions.UP)
            }
        }
        else if (motionEvent.action == MotionEvent.ACTION_UP){
            move = false
        }
        postInvalidate()
        return true
    }

}
