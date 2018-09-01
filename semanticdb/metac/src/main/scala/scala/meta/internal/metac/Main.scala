package scala.meta.internal.metac

import java.io._
import java.net._
import java.nio.channels._
import java.nio.file._
import javax.tools._
import scala.collection.JavaConverters._
import scala.meta.cli._
import scala.meta.internal.semanticdb.javac.{SemanticdbPlugin => JavacSemanticdbPlugin}
import scala.meta.internal.semanticdb.scalac.{SemanticdbPlugin => ScalacSemanticdbPlugin}
import scala.meta.metac._
import scala.tools.nsc.{Main => ScalacMain}

class Main(settings: Settings, reporter: Reporter) {
  def process(): Boolean = {
    val isScala = !settings.compilerArgs.exists(_.endsWith(".java"))
    if (isScala) {
      val manifestDir = Files.createTempDirectory("semanticdb-scalac_")
      val resourceUrl = classOf[ScalacSemanticdbPlugin].getResource("/scalac-plugin.xml")
      val resourceChannel = Channels.newChannel(resourceUrl.openStream())
      val manifestStream = new FileOutputStream(manifestDir.resolve("scalac-plugin.xml").toFile)
      manifestStream.getChannel().transferFrom(resourceChannel, 0, Long.MaxValue)
      manifestStream.close()
      val pluginClasspath = classOf[ScalacSemanticdbPlugin].getClassLoader match {
        case null => manifestDir.toString
        case cl: URLClassLoader => cl.getURLs.map(_.getFile).mkString(File.pathSeparator)
        case cl => sys.error(s"unsupported classloader: $cl")
      }
      val enablePluginArgs = List("-Xplugin:" + pluginClasspath, "-Xplugin-require:semanticdb")
      val enableRangeposArgs = List("-Yrangepos")
      val stopAfterPluginArgs = List("-Ystop-after:semanticdb-typer")
      val args = settings.compilerArgs ++ enablePluginArgs ++ enableRangeposArgs ++ stopAfterPluginArgs
      scala.Console.withOut(reporter.out) {
        scala.Console.withErr(reporter.err) {
          ScalacMain.process(args.toArray)
        }
      }
      !ScalacMain.reporter.hasErrors
    } else {
      // TODO: This implementation is lacking in multiple ways, e.g.:
      //  * It doesn't handle -cp arguments passed through compilerArgs.
      //  * It doesn't handle -d arguments passed through compilerArgs.
      //  * It doesn't provide a way to pass a custom sourceroot to semanticdb-javac.
      //  * It doesn't support emitting SemanticDB files for mixed Java/Scala projects.
      val pluginClasspath = classOf[JavacSemanticdbPlugin].getClassLoader match {
        case null => Files.createTempDirectory("semanticdb-javac_").toString
        case cl: URLClassLoader => cl.getURLs.map(_.getFile).mkString(File.pathSeparator)
        case cl => sys.error(s"unsupported classloader: $cl")
      }
      val args = List.newBuilder[String]
      settings.compilerArgs.foreach(arg => args += arg)
      args += "-cp"
      args += pluginClasspath
      args += "-d"
      args += Files.createTempDirectory("javac_").toString
      args += s"-Xplugin:semanticdb . --sourceroot ${sys.props("user.dir")}"
      args += "-parameters"
      val compiler = ToolProvider.getSystemJavaCompiler()
      val code = compiler.run(null, reporter.out, reporter.err, args.result: _*)
      code == 0
    }
  }
}
