data class RequestInform(val priority: Int, val id: Int, val month: Int) {
    operator fun compareTo(other: Any?): Int {
        return if (other !is RequestInform) {
            -1
        } else {
            priority - other.priority
        }
    }
}

data class Edge(val to: Int = -1, val id: Int = -1, var cap: Int = 0, var flow: Int = 0)

data class Emp(val sl: Int, var sr: Int, val id: Int, val q: List<Int>)

data class InputEdge(val from: Int, val to: Int, var cap: Int)
