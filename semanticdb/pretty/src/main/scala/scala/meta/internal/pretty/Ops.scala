package scala.meta.internal.pretty

import java.io._
import java.nio.charset.StandardCharsets._

trait Ops {
  implicit class StrOps[T: Str](x: T) {
    def str: String = {
      val out = new ByteArrayOutputStream
      val p = new Printer(out)
      implicitly[Str[T]].apply(p, x)
      new String(out.toByteArray, UTF_8)
    }
  }
}
