package io.github.suitougreentea.util

// Returns List<(startIndex, length)>
fun IntArray.splitToIntervals(): List<Pair<Int, Int>> {
  val sorted = this.sorted()
  val result = mutableListOf<Pair<Int, Int>>()
  var start = sorted[0]
  sorted.forEachIndexed { i, _ ->
    if (i == sorted.size - 1 || sorted[i] + 1 != sorted[i + 1]) {
      result.add(Pair(start, sorted[i] - start + 1))
      if (i != sorted.size - 1) start = sorted[i + 1]
    }
  }

  return result.toList()
}
