package io.github.suitougreentea.simudiaextended

interface Action {
  fun doAction(): ActionResult
  fun undoAction(): ActionResult
}

class ActionNewStationRow(val form: MainForm, val element: StationElement): Action {
  override fun doAction(): ActionResult {
    val index = form.diagram.stations.getSize()
    form.diagram.stations.add(element)
    form.setRowToStationData(index, element)
    form.stationsTableModel.addRow(arrayOf())
    return ActionResult(true)
  }

  override fun undoAction(): ActionResult {
    val index = form.diagram.stations.getSize() - 1
    form.diagram.stations.delete(index)
    form.stationsTableModel.removeRow(index)
    return ActionResult(true)
  }
}

class ActionUpdateStationRow(val form: MainForm, val index: Int, val old: StationElement, val new: StationElement): Action {
  override fun doAction(): ActionResult {
    form.diagram.stations.set(index, new)
    form.setRowToStationData(index, new)
    return ActionResult(true)
  }

  override fun undoAction(): ActionResult {
    form.diagram.stations.set(index, old)
    form.setRowToStationData(index, old)
    return ActionResult(true)
  }
}

class ActionInsertStationRows(val form: MainForm, val splitRowIndices: List<Pair<Int, Int>>): Action {
  override fun doAction(): ActionResult {
    splitRowIndices.sortedByDescending { a -> a.first }.forEach {
      val startIndex = it.first
      val length = it.second
      repeat(length) {
        form.stationsTableModel.insertRow(startIndex, arrayOf())
        form.diagram.stations.insert(startIndex)
      }
    }
    return ActionResult(true)
  }

  override fun undoAction(): ActionResult {
    splitRowIndices.forEach {
      val startIndex = it.first
      val length = it.second
      repeat(length) {
        form.stationsTableModel.removeRow(startIndex)
        form.diagram.stations.delete(startIndex)
      }
    }
    return ActionResult(true)
  }
}

class ActionDeleteStationRows(val form: MainForm, val rowIndices: List<Int>): Action {
  lateinit var removedElements: List<StationElement>
  override fun doAction(): ActionResult {
    removedElements = rowIndices.map { form.diagram.stations.get(it) }
    rowIndices.sortedDescending().forEach {
      form.stationsTableModel.removeRow(it)
      form.diagram.stations.delete(it)
    }
    return ActionResult(true)
  }

  override fun undoAction(): ActionResult {
    rowIndices.sorted().forEachIndexed { i, it ->
      form.stationsTableModel.insertRow(it, arrayOf())
      form.setRowToStationData(it, removedElements[i])
      form.diagram.stations.insert(it, removedElements[i])
    }

    return ActionResult(true)
  }
}

class ActionResult(val success: Boolean)