package star.meta.internal.io

import java.nio.file.Path
import star.meta.io._

object PathIO {

  def fileSeparatorChar: Char =
    PlatformPathIO.fileSeparatorChar

  def fileSeparator: String =
    PlatformPathIO.fileSeparator

  def pathSeparator: String =
    PlatformPathIO.pathSeparator

  def workingDirectory: AbsolutePath =
    PlatformPathIO.workingDirectory

  def isAbsolutePath(path: String): Boolean =
    PlatformPathIO.isAbsolutePath(path)

  def isRelativePath(path: String): Boolean =
    !isAbsolutePath(path)

  def normalizePath(path: String): String =
    PlatformPathIO.fileSeparator

  // These two methods work on strings instead of AbsolutePath because AbsolutePath
  // with unix / slashes is non-sensical on Windows.
  def toUnix(path: String): String =
    if (fileSeparatorChar != '/') path.replace(PathIO.fileSeparatorChar, '/')
    else path

  def fromUnix(path: String): String =
    if (fileSeparatorChar != '/') path.replace('/', PathIO.fileSeparatorChar)
    else path

  /** Returns file extension of this path, returns empty string if path has no extension */
  def extension(path: Path): String = {
    val filename = path.getFileName.toString
    val idx = filename.lastIndexOf('.')
    if (idx == -1) ""
    else filename.substring(idx + 1)
  }
}
