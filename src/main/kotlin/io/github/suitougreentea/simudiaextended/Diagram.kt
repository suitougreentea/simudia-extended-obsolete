package io.github.suitougreentea.simudiaextended

import com.beust.klaxon.*
import java.util.*

class Diagram {
  var monthLength = Time(0)
  var stations = StationsData()

  fun write(writer: DiagramWriter): Map<String, Any?> = mapOf(
      "month_length" to monthLength.tick,
      "stations" to JsonObject(stations.write(writer)))

  companion object {
    fun read(reader: DiagramReader, json: JsonObject): Diagram {
      val diagram = Diagram()
      diagram.monthLength = Time(json.long("month_length") ?: 0L)
      diagram.stations = StationsData.read(reader, json.obj("stations") ?: JsonObject())
      return diagram
    }
  }
}

class StationsData {
  private var list_ = mutableListOf<StationElement>()
  private val random = Random(System.currentTimeMillis())
  fun getList() = list_.toList()
  fun getSize() = list_.size

  fun insert(index: Int) = insert(index, StationElement("", "", createId()))

  fun insert(index: Int, element: StationElement) {
    list_.add(index, element)
  }

  fun delete(index: Int) {
    list_.removeAt(index)
  }

  fun get(index: Int) = list_[index]
  fun set(index: Int, element: StationElement) = list_.set(index, element)

  fun add(element: StationElement) = list_.add(element)

  fun updateName(index: Int, name: String) {
    if (index > list_.size) return
    if (index == list_.size) list_.add(StationElement(name, "", createId()))
    else list_.get(index).updateName(name)
  }

  fun updateMarker(index: Int, marker: String) {
    if (index > list_.size) return
    if (index == list_.size) list_.add(StationElement("", marker, createId()))
    else list_.get(index).updateMarker(marker)
  }

  fun createId(): Long {
    while(true) {
      val id = random.nextLong()
      if (list_.all { it.id != id && id != -1L }) return id
    }
  }

  fun write(writer: DiagramWriter): Map<String, Any?> = mapOf(
      "list" to JsonArray(list_.map { it.write(writer) })
  )

  companion object {
    fun read(reader: DiagramReader, json: JsonObject): StationsData {
      val data = StationsData()
      val list = json.array<JsonObject>("list")
      list?.forEach { data.add(StationElement.read(reader, it)) }
      data.list_.forEachIndexed { i, it ->
        if (it.id == -1L) {
          data.set(i, StationElement(it.name, it.marker, data.createId()))
        }
      }
      return data
    }
  }
}

class StationElement(var name: String, var marker: String, val id: Long) {
  fun updateName(name: String) { this.name = name }
  fun updateMarker(marker: String) { this.marker = marker }

  fun write(writer: DiagramWriter): Map<String, Any?> = mapOf(
      "name" to name,
      "marker" to marker,
      "id" to id
  )

  companion object {
    fun read(reader: DiagramReader, json: JsonObject): StationElement {
      val name = json.string("name") ?: ""
      val marker = json.string("marker") ?: ""
      val id = json.long("id") ?: -1L
      return StationElement(name, marker, id)
    }
  }
}
