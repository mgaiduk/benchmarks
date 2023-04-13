import kotlin.math.sqrt

val TARGET = 100_000_000

fun main() {
    val vec = mutableListOf<Int>()
    for (i in 2..TARGET) {
        var flag = false
        for (prime in vec) {
            if (i % prime == 0) {
                flag = true
                break
            }
            if (prime > sqrt(i.toDouble())) {
                break
            }
        }
        if (!flag) {
            vec.add(i)
        }
    }
    println(vec.last())
}