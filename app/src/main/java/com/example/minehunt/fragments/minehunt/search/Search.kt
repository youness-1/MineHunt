package com.example.minehunt.fragments.minehunt.search

import com.example.minehunt.fragments.minehunt.GridView
import com.example.minehunt.fragments.minehunt.TileType
import java.util.*


class Search(gridView: GridView, heuristic: Heuristic){ //class that generates the graph and executes A*
    private var heuristic: Heuristic
    private var gridView: GridView
    private lateinit var graph: Graph
    init {
        this.gridView=gridView
        this.heuristic=heuristic
    }
    private fun getGraph() {
        val grid=gridView.grid
        graph= Graph(heuristic)
        val checkAdjacent = arrayOf(Pair(-1,0), Pair(0,-1), Pair(1,0), Pair(0,1))
        var count = 0
        for(i in grid.indices) {
            for (j in grid[i].indices) {
                if(grid[i][j].type!= TileType.MINABLE || grid[i][j].type!= TileType.MINABLE2){
                    graph.addVertex(count, i, j)
                    count += 1
                }

            }
        }
        for(i in grid.indices) {
            for (j in grid[i].indices) {
                val v1=graph.getVertex(i, j)
                if(v1 != null) {
                    for (offset in checkAdjacent) {
                        val x = offset.first + j
                        val y = offset.second + i
                        if (y in grid.indices && x in grid[i].indices) {
                            val v2= graph.getVertex(y, x)
                            if(v2 != null) {
                                graph.addEdge(v1, v2, 1.0)
                            }
                        }
                    }
                }
            }
        }
    }
    private fun aStar(start: Vertex, stop: Vertex): Boolean {
        val comparator = Comparator<Vertex> { arg0, arg1 -> arg0.f.compareTo(arg1.f) }
        val queue = PriorityQueue(1, comparator)
        start.distance = 0.0
        start.f = 0.0
        queue.add(start)
        var ex = start
        while (!queue.isEmpty()) {
            val extracted = queue.poll()
            extracted!!.discovered = true
            ex=extracted
            if (extracted === stop)
                break
            for (i in extracted.edges.indices) {
                val edge = extracted.edges[i]
                val neighbor = edge.destination
                if (!neighbor.discovered) {
                    graph.heuristic(neighbor, stop)
                    if (neighbor.f > extracted.f + edge.weight) {
                        neighbor.distance = extracted.distance + edge.weight
                        graph.heuristic(neighbor, stop)
                        neighbor.f = neighbor.distance + neighbor.heuristic
                        neighbor.parent = extracted
                        queue.remove(neighbor)
                        queue.add(neighbor)
                    }
                }
            }
        }
        return ex==stop
    }

    fun run(): Boolean{
        getGraph()
        val start = graph.getVertex(gridView.game.playerCoords.first, gridView.game.playerCoords.second)
        val stop = graph.getVertex(gridView.game.exit.first, gridView.game.exit.second)
        return if (start != null && stop != null) {
            aStar(start, stop)
        } else
            false
    }
}