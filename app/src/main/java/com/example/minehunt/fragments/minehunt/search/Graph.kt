package com.example.minehunt.fragments.minehunt.search

import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

class Edge(var destination: Vertex, var weight: Double)
class Vertex(
    var id: Int,
    var i: Int, var j: Int
) {
    var parent: Vertex? = null
    var distance = Double.POSITIVE_INFINITY
    var edges: LinkedList<Edge> = LinkedList<Edge>()
    var discovered = false
    var heuristic = 0.0

    var f = Double.POSITIVE_INFINITY
}

class Graph(heuristicType: Heuristic) {
    private var heuristicType: Heuristic
    private var vertex = ArrayList<Vertex>()
    init {
        this.heuristicType=heuristicType
    }
    fun addVertex(id: Int, i: Int, j: Int) {
        val v1 = Vertex(id, i, j)
        vertex.add(v1)
    }

    fun addEdge(source: Vertex, destination: Vertex?, weight: Double) {
        source.edges.add(Edge(destination!!, weight))
    }

    fun getVertex(i: Int, j: Int): Vertex? {
        for (c in vertex.indices) if (vertex[c].i == i && vertex[c].j == j) return vertex[c]
        return null
    }

    fun heuristic(v: Vertex, destination: Vertex) {
        if (heuristicType == Heuristic.EUCLIDEAN){
            v.heuristic = sqrt(((v.i - destination.i) * (v.i - destination.i) + (v.j - destination.j) * (v.j - destination.j)).toDouble())
        }
        else{
            v.heuristic = abs(destination.i.toDouble() - v.i.toDouble()) + abs(destination.j.toDouble() - v.j.toDouble())
        }
    }
}

enum class Heuristic {
    MANHATTAN,
    EUCLIDEAN
}