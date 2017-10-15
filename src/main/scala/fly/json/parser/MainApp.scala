package fly.json.parser

import java.io.{File, FileInputStream}

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

import scala.io.Source
import scala.util.Try

case class Result(cntErr: Int, cntSuccess: Int, errMsgs: List[String]) {
  def incErr(msg: String): Result = copy(cntErr = cntErr + 1, errMsgs = msg :: errMsgs)
  def incOk: Result = copy(cntSuccess = cntSuccess + 1)
}

object Result {
  def empty = Result(0, 0, List.empty[String])
}

case class ErrorStats(cid: String, client: String, error: String)

object ErrorStats {
  val CID = "CID"
  val CLIENT = "Client"
  val ERROR = "error"
}

object MainApp {

  val sourceFolder = "/Users/user/projects/fly-json/data"

  private def processFolder(path: String): Unit = {
    val d = new File(path)
    if (d.exists && d.isDirectory) {
      d.listFiles
        .filter(f => f.isFile && f.getName.endsWith(".tar.gz"))
        .par
        .foreach(f => println(s"${f.getName}: ${processArchive(f)}"))
    }
  }

  private def processArchive(file: File): Option[Result] = {
    val optIn = Option(new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file))))
    try {
      optIn.map(in =>
        Iterator.continually(in.getNextTarEntry)
          .takeWhile(_ != null)
          .filter(entry => !entry.isDirectory && entry.getName.endsWith(".json"))
          .map(_ => readFile(in))
          .foldLeft(Result.empty){ case (acc, r) =>
            if (r.isFailure) acc.incErr(r.toString) else acc.incOk }
      )
    } finally {
      optIn.foreach(_.close())
    }
  }

  private def readFile[T](tarEntry: TarArchiveInputStream): Try[ErrorStats] = {
    val s = Source.fromInputStream(tarEntry).mkString
    JsonUtl.run(s)
  }

  def main(args: Array[String]): Unit = {
    processFolder(sourceFolder)
  }
}