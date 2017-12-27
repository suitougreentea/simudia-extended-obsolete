package io.github.suitougreentea.util.swing

/*
 * Original: https://github.com/Vuksa/SwingDSL/blob/dsl_complete/src/dsl/SwingDSL.kt
 */

import java.awt.Dimension
import java.awt.Rectangle
import java.net.URL
import javax.swing.*
import javax.swing.table.DefaultTableModel

typealias Width = Int
typealias Height = Int
typealias CoordinateX = Int
typealias CoordinateY = Int

inline fun JFrame.frame(title: String = "", init: JFrame.() -> Unit): JFrame = apply {
  this.title = title
  init()
}

inline fun JFrame.menuBar(init: JMenuBar.() -> Unit) = JMenuBar().apply {
  init()
}.also { jMenuBar = it }

inline fun JMenuBar.menu(name: String = "", init: JMenu.() -> Unit) = JMenu(name).apply {
  init()
}.also { add(it) }

inline fun JMenu.item(name: String = "", init: JMenuItem.() -> Unit) = JMenuItem(name).apply {
  init()
}.also { add(it) }

inline fun JPanel.button(label: String = "", init: JButton.() -> Unit) = JButton(label).apply {
  init()
}.also { add(it) }


inline fun JFrame.panel(init: JPanel.() -> Unit) = JPanel().apply {
  init()
}.also { contentPane = it }

inline fun JPanel.label(label: String = "", init: JLabel.() -> Unit) = JLabel(label).apply {
  init()
}.also { add(it) }

inline fun JPanel.scrollableTable(initScroll: JScrollPane.() -> Unit, initTable: JTable.() -> Unit) =
    Pair(JScrollPane().apply { initScroll() }, JTable().apply { initTable() }).also {
      it.first.setViewportView(it.second)
      add(it.first)
    }

inline fun JPanel.tabbedPane(tabPlacement: Int, init: JTabbedPane.() -> Unit) = JTabbedPane(tabPlacement).apply {
  init()
}.also { add(it) }

inline fun JPanel.textField(label: String = "", init: JTextField.() -> Unit) = JTextField(label).apply {
  init()
}.also { add(it) }


inline fun popup(init: JPopupMenu.() -> Unit) = JPopupMenu().apply {
  init()
}

inline fun JPopupMenu.item(name: String = "", init: JMenuItem.() -> Unit) = JMenuItem(name).apply {
  init()
}.also { add(it) }

inline fun <reified T> resource(resourceDestination: String): URL {
  return T::class.java.getResource(resourceDestination)
}

fun rectangle(dimension: Dimension) = Rectangle(0, 0, dimension.width, dimension.height)
fun rectangle(x: CoordinateX, y: CoordinateY, dimension: Dimension) = Rectangle(x, y, dimension.width, dimension.height)

infix fun Width.x(height: Height) = Dimension(this, height)

