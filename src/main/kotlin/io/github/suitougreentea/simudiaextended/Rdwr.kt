package io.github.suitougreentea.simudiaextended

import com.beust.klaxon.JSON
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.int
import java.io.*

object DiagramRdwr {
  val currentVersion = 1

  fun read(file: File): Diagram {
    return DiagramReader(file).read()
  }

  fun write(diagram: Diagram, file: File) {
    DiagramWriter(file, diagram).write()
  }
}

class DiagramReader(val file: File) {
  val fileReader = FileReader(file)
  val json = Parser().parse(fileReader) as JsonObject
  var version: Int = -1

  fun read(): Diagram {
    version = json.int("_simudiaex_version") ?: -1
    return Diagram.read(this, json)
  }
}

class DiagramWriter(val file: File, val diagram: Diagram) {
  fun write() {
    val json = JsonObject(mapOf("_simudiaex_version" to DiagramRdwr.currentVersion) + diagram.write(this))
    val string = json.toJsonString(true)
    val writer = FileWriter(file)
    writer.write(string)
    writer.close()
  }
}
