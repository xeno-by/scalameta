package scala.meta.internal.pretty

import java.io._
import java.nio.charset.StandardCharsets._

trait Pretty {
  def printStr(p: Printer): Unit

  def str: String = {
    val out = new ByteArrayOutputStream
    val p = new Printer(out)
    printStr(p)
    new String(out.toByteArray, UTF_8)
  }

  final override def toString: String = {
    str
  }
}
