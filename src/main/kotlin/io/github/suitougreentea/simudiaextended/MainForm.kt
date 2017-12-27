package io.github.suitougreentea.simudiaextended

import io.github.suitougreentea.util.splitToIntervals
import io.github.suitougreentea.util.swing.*
import java.awt.BorderLayout
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.File
import java.util.*
import javax.swing.*
import javax.swing.event.*
import javax.swing.table.DefaultTableModel

class MainForm: JFrame() {
  lateinit var tab: JTabbedPane
  lateinit var stationsTable: JTable
  lateinit var stationsTableModel: DefaultTableModel

  var userEditing = true
  var currentFile: File? = null
  var diagram = Diagram()

  //<editor-fold desc="undo/redo, file modified stuff">
  val undoStack = Stack<Pair<Action, EditingState>>()
  val redoStack = Stack<Pair<Action, EditingState>>()
  var modified = false
  var lastActionSaved: Action? = null
  var editingState = EditingState()

  fun perform(action: Action) {
    userEditing = false
    val result = action.doAction()
    userEditing = true
    if (result.success) {
      undoStack.push(Pair(action, editingState.copy()))
      redoStack.removeAllElements()
    }
    updateModifyState()
  }

  fun undo() {
    if (undoStack.size == 0) return
    userEditing = false
    undoStack.pop().let {
      it.first.undoAction()
      it.second.apply()
      redoStack.push(it)
    }
    userEditing = true
    updateModifyState()
  }

  fun redo() {
    if (redoStack.size == 0) return
    userEditing = false
    redoStack.pop().let {
      it.second.apply()
      it.first.doAction()
      undoStack.push(it)
    }
    userEditing = true
    updateModifyState()
  }
  //</editor-fold>

  init {
    frame(PRODUCT_FULLNAME) {
      defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
      size = 640 x 400
      isVisible = true
      addWindowListener(object: WindowListener {
        override fun windowDeiconified(e: WindowEvent?) { }
        override fun windowClosing(e: WindowEvent?) {
          if (onClosing()) {
            System.exit(0)
          }
        }
        override fun windowClosed(e: WindowEvent?) { }
        override fun windowActivated(e: WindowEvent?) { }
        override fun windowDeactivated(e: WindowEvent?) { }
        override fun windowOpened(e: WindowEvent?) { }
        override fun windowIconified(e: WindowEvent?) { }
      })

      menuBar {
        menu("File") {
          item("Open...") {
            addActionListener {
              onOpen()
            }
          }
          item("Save") {
            addActionListener {
              onSave()
            }
          }
          item("Save as...") {
            addActionListener {
              onSaveAs()
            }
          }
          item("Exit") {
            addActionListener {
              this.dispatchEvent(WindowEvent(this@MainForm, WindowEvent.WINDOW_CLOSING))
            }
          }
        }
        menu("Edit") {
          val undoItem = item("Undo") {
            addActionListener {
              undo()
            }
          }
          val redoItem = item("Redo") {
            addActionListener {
              redo()
            }
          }
          addMenuListener(object: MenuListener {
            override fun menuSelected(e: MenuEvent?) {
              undoItem.isEnabled = undoStack.size > 0
              redoItem.isEnabled = redoStack.size > 0
            }
            override fun menuCanceled(e: MenuEvent?) { }
            override fun menuDeselected(e: MenuEvent?) { }
          })
        }
      }
      panel {
        layout = BorderLayout()
        tab = tabbedPane(JTabbedPane.BOTTOM) {
          addTab("Stations",
              panel {
                layout = BorderLayout()
                scrollableTable({

                }, {
                  DefaultTableModel(arrayOf("Name", "Marker"), 1).apply {
                    addTableModelListener {
                      if (userEditing && it.type == TableModelEvent.UPDATE && it.firstRow == it.lastRow) {
                        val row = it.firstRow
                        val name = this.getValueAt(row, 0) as String? ?: ""
                        val marker = this.getValueAt(row, 1) as String? ?: ""

                        if (row == diagram.stations.getSize()) {
                          val element = StationElement(name, marker, diagram.stations.createId())
                          perform(ActionNewStationRow(this@MainForm, element))
                        } else {
                          val oldElement = diagram.stations.get(row)
                          val newElement = StationElement(name, marker, oldElement.id)
                          perform(ActionUpdateStationRow(this@MainForm, row, oldElement, newElement))
                        }
                      }
                    }
                  }.also { this.model = it; stationsTableModel = it }

                  selectionModel.addListSelectionListener {
                    editingState.stationsFocusRow = selectedRow
                    editingState.stationsFocusColumn = selectedColumn
                    editingState.stationsSelectionRows = selectedRows.clone()
                  }
                  columnModel.selectionModel.addListSelectionListener {
                    editingState.stationsFocusRow = selectedRow
                    editingState.stationsFocusColumn = selectedColumn
                  }

                  addMouseListener(MouseListenerStationsContextMenu())

                  putClientProperty("terminateEditOnFocusLost", true)
                }).also { stationsTable = it.second }
              })
          addTab("Times",
              panel {
                layout = BorderLayout()

              })
          addTab("Lines",
              panel {
                layout = BorderLayout()

              })
          addChangeListener { editingState.currentTab = selectedIndex }
        }
      }
    }
    updateTitle()
  }

  fun resetComponents() {
    undoStack.clear()
    redoStack.clear()
    lastActionSaved = null
    editingState = EditingState()

    userEditing = false
    stationsTableModel.rowCount = 0
    stationsTableModel.rowCount = diagram.stations.getSize() + 1
    diagram.stations.getList().forEachIndexed { i, it -> setRowToStationData(i, it) }
    stationsTable.addRowSelectionInterval(0, 0)
    userEditing = true
  }

  // true if closing succeeds
  fun onClosing(): Boolean {
    if (modified) {
      val result = JOptionPane.showConfirmDialog(this, "Save before closing?")
      return when (result) {
        JOptionPane.CANCEL_OPTION -> false
        JOptionPane.YES_OPTION -> onSave()
        JOptionPane.NO_OPTION -> true
        else -> false
      }
    }
    return true
  }

  fun onOpen(): Boolean {
    if (onClosing()) {
      val chooser = JFileChooser()
      val result = chooser.showOpenDialog(this)
      if (result == JFileChooser.APPROVE_OPTION) {
        val file = chooser.selectedFile
        diagram = DiagramRdwr.read(file)
        resetComponents()
        currentFile = file
        modified = false
        updateTitle()
        return true
      }
      return false
    }
    return false
  }

  fun onSave(): Boolean {
    val currentFile = currentFile
    if (currentFile == null) {
      return onSaveAs()
    } else {
      DiagramRdwr.write(diagram, currentFile)
      lastActionSaved = if (undoStack.size > 0) undoStack.peek().first else null
      modified = false
      updateTitle()
      return true
    }
  }

  fun onSaveAs(): Boolean {
    // TODO: overwrite confirmation
    val chooser = JFileChooser()
    val result = chooser.showSaveDialog(this)
    if (result == JFileChooser.APPROVE_OPTION) {
      val file = chooser.selectedFile
      DiagramRdwr.write(diagram, file)
      lastActionSaved = if (undoStack.size > 0) undoStack.peek().first else null
      modified = false
      updateTitle()
      currentFile = file
      return true
    }
    return false
  }

  fun updateTitle() {
    val name = currentFile?.name ?: "New file"
    title = "$name${if (modified) " *" else ""} - $PRODUCT_FULLNAME"
  }

  fun updateModifyState() {
    val lastAction = if (undoStack.size > 0) undoStack.peek() else null
    modified = lastAction != lastActionSaved
    updateTitle()
  }

  fun setRowToStationData(index: Int, element: StationElement) {
    if (userEditing) throw IllegalStateException("Must be called when userEditing = false")
    stationsTableModel.setValueAt(element.name, index,0)
    stationsTableModel.setValueAt(element.marker, index,1)
  }

  inner class MouseListenerStationsContextMenu: MouseListener {
    override fun mouseReleased(e: MouseEvent?) {
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseClicked(e: MouseEvent?) {
      if (e == null) return
      if (SwingUtilities.isRightMouseButton(e)) {
        val row = stationsTable.rowAtPoint(e.getPoint())
        if (stationsTable.selectedRows.all { it != row }) {
          stationsTable.clearSelection()
          stationsTable.addRowSelectionInterval(row, row)
        }
        popup {
          item("Insert") {
            addActionListener {
              val splitRowIndices = stationsTable.selectedRows.splitToIntervals()
              perform(ActionInsertStationRows(this@MainForm, splitRowIndices))
            }
          }
          item("Delete") {
            addActionListener {
              val selection = stationsTable.selectedRows.filter { it < diagram.stations.getSize() }
              perform(ActionDeleteStationRows(this@MainForm, selection))
            }
          }
        }.show(e.component, e.x, e.y)
      }
    }

    override fun mouseExited(e: MouseEvent?) {
    }

    override fun mousePressed(e: MouseEvent?) {
    }
  }

  inner class EditingState {
    var currentTab = 0
    var stationsSelectionRows = intArrayOf()
    var stationsFocusColumn = 0
    var stationsFocusRow = 0

    fun apply() {
      tab.selectedIndex = currentTab
      // TODO: update selection
      //stationsTable.clearSelection()
    }

    fun copy(): EditingState = EditingState().also {
      it.currentTab = currentTab
      it.stationsSelectionRows = stationsSelectionRows.clone()
      it.stationsFocusColumn = stationsFocusColumn
      it.stationsFocusRow = stationsFocusRow
    }
  }
}

