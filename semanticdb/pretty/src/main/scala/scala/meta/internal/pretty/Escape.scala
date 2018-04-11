package scala.meta.internal.pretty

object Escape {
  private def escape(x: Char, quote: Char): String = {
    x match {
      case x if x == quote => "\\" + x
      case '\b' => "\\b"
      case '\n' => "\\n"
      case '\t' => "\\t"
      case '\r' => "\\r"
      case '\f' => "\\f"
      case '\\' => "\\\\"
      case other => other.toString
    }
  }

  def apply(x: Char): String = {
    escape(x, '\'')
  }

  def apply(x: String): String = {
    x.flatMap(escape(_, '"'))
  }
}
