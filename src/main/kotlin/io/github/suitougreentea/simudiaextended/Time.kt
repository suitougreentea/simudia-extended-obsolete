package io.github.suitougreentea.simudiaextended

class Time(val tick: Long) {
  constructor(hour: Long, minute: Byte, second: Byte, tick: Int = 0): this(tick + 10000000 * (second + 60 * (minute + 60 * hour)))
}