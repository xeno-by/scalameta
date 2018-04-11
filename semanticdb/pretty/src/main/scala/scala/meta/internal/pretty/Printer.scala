package scala.meta.internal.pretty

import java.io._
import java.nio.charset.StandardCharsets._

final class Printer(out: OutputStream) {
  private var indentation = 0
  private var afterNewline = true

  private[pretty] def append(s: String): Unit = {
    if (afterNewline) {
      val prefix = "  " * indentation
      out.write(prefix.getBytes(UTF_8))
      afterNewline = false
    }
    out.write(s.getBytes(UTF_8))
    afterNewline = s.contains(EOL)
  }

  def str[T: Str](x: T): Unit = {
    implicitly[Str[T]].apply(this, x)
  }

  def str(x: Pretty): Unit = {
    if (x != null) x.printStr(this)
    else str("null")
  }

  def str(x: String): Unit = {
    append(x)
  }

  def rep[T](pre: String, xs: Iterable[T], sep: String, suf: String)(f: T => Unit): Unit = {
    if (xs.nonEmpty) {
      append(pre)
      rep(xs, sep)(f)
      append(suf)
    }
  }

  def rep[T](pre: String, xs: Iterable[T], sep: String)(f: T => Unit): Unit = {
    rep(pre, xs, sep, "")(f)
  }

  def rep[T](xs: Iterable[T], sep: String, suf: String)(f: T => Unit): Unit = {
    rep("", xs, sep, suf)(f)
  }

  def rep[T](xs: Iterable[T], sep: String)(f: T => Unit): Unit = {
    if (xs.nonEmpty) {
      xs.init.foreach { x =>
        f(x)
        append(sep)
      }
      f(xs.last)
    }
  }

  def opt[T](pre: String, xs: Option[T], suf: String)(f: T => Unit): Unit = {
    xs.foreach { x =>
      append(pre)
      f(x)
      append(suf)
    }
  }

  def opt[T](pre: String, xs: Option[T])(f: T => Unit): Unit = {
    opt(pre, xs, "")(f)
  }

  def opt[T](xs: Option[T], suf: String)(f: T => Unit): Unit = {
    opt("", xs, suf)(f)
  }

  def opt[T](xs: Option[T])(f: T => Unit): Unit = {
    opt("", xs, "")(f)
  }

  def opt(pre: String, s: String, suf: String)(f: String => Unit): Unit = {
    if (s.nonEmpty) {
      append(pre)
      f(s)
      append(suf)
    }
  }

  def opt(s: String, suf: String)(f: String => Unit): Unit = {
    opt("", s, suf)(f)
  }

  def opt(s: String)(f: String => Unit): Unit = {
    opt("", s, "")(f)
  }

  def indent(n: Int = 1): Unit = {
    indentation += n
  }

  def unindent(n: Int = 1): Unit = {
    indentation -= n
  }

  def newline(): Unit = {
    append(EOL)
  }
}
