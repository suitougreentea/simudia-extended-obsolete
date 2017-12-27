package io.github.suitougreentea.simudiaextended

import javax.swing.SwingUtilities.invokeAndWait
import javax.swing.SwingUtilities.invokeLater
import javax.swing.UIManager

val PRODUCT_NAME = "SimuDia-Extended"
val PRODUCT_VERSION = "0.0.5"
val PRODUCT_FULLNAME = "${PRODUCT_NAME} Version ${PRODUCT_VERSION}"

fun main(args: Array<String>) {
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

  invokeLater {
  //invokeAndWait {
    MainForm()
  }
}