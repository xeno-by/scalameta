package scala.meta.metac

import scala.meta.cli._

final class Settings private (val compilerArgs: List[String]) {
  private def this() = {
    this(compilerArgs = Nil)
  }

  @deprecated("Use `compilerArgs`.", "4.0.0")
  def scalacArgs: List[String] = {
    compilerArgs
  }

  @deprecated("Use `withCompilerArgs`.", "4.0.0")
  def withScalacArgs(scalacArgs: List[String]): Settings = {
    withCompilerArgs(scalacArgs)
  }

  def withCompilerArgs(compilerArgs: List[String]): Settings = {
    copy(compilerArgs = compilerArgs)
  }

  private def copy(compilerArgs: List[String] = compilerArgs): Settings = {
    new Settings(compilerArgs = compilerArgs)
  }
}

object Settings {
  def parse(args: List[String], reporter: Reporter): Option[Settings] = {
    Some(new Settings(args))
  }

  def apply(): Settings = {
    new Settings()
  }
}
