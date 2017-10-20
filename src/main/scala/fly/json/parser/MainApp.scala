package fly.json.parser

import java.io.{File, FileInputStream, PrintWriter}

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

import scala.collection.parallel.mutable.ParArray
import scala.io.Source
import scala.util.{Failure, Success, Try}

case class ErrorStats(cid: String, client: String, current: String, error: String) {
  def isMedia: Boolean = error.contains("Media")
}

object ErrorStats {
  val CID = "CID"
  val CLIENT = "Client"
  val CURRENT = "Current"
  val ERROR = "error"
}

object MainApp {

  private def processFolder(path: String): ParArray[String] = {
    val d = new File(path)
    if (d.exists && d.isDirectory) {
      d.listFiles
        .filter(f => f.isFile && f.getName.endsWith(".tar.gz"))
        .par
        .flatMap(processArchive)
    } else {
      ParArray.empty[String]
    }
  }

  private def decodeFileName(fileName: String): Try[String] = {
    Try(fileName.split('/').last.split('-')).flatMap {
      case Array(_, year, month, day, hour, minutes, _*) => Success(s"$day.$month.$year $hour:$minutes:$day")
      case _ => Failure(new Exception(s"cant't extract date from file name: $fileName"))
    }
  }

  private def processArchive(file: File): List[String] = {
    val optIn = Option(new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file))))
    try {
      optIn.toList.flatMap(in =>
        Iterator.continually(in.getNextTarEntry)
          .takeWhile(_ != null)
          .filter(entry => !entry.isDirectory && entry.getName.endsWith(".json"))
          .flatMap(entry => readFile(in, entry.getName))
      )
    } finally {
      optIn.foreach(_.close())
    }
  }

  private def buildInsertSQL(err: ErrorStats, fileName: String): Option[String] = {
    decodeFileName(fileName)
      .map(date =>
        s"insert into t_error values('$date','','${err.cid}','${err.client}}','${err.current}}', '${err.error}');\n"
      ).toOption
  }

  private def readFile[T](tarEntry: TarArchiveInputStream, fileName: String): Option[String] = {
    JsonUtl.run(Source.fromInputStream(tarEntry, "UTF-8").mkString) match {
      case Success(errorStats) if errorStats.isMedia => buildInsertSQL(errorStats, fileName)
      case Failure(e) => println(e.getMessage); None
      case _ => None
    }
  }

  private def runWithMeasure[T](f: => T): (T, Long) = {
    val start = System.nanoTime()
    val result = f
    val end = System.nanoTime()
    result -> (end - start) / 1000000
  }

  def main(args: Array[String]): Unit = {

    if (args.isEmpty) {
      println(s"Set path to folder !!!")
    }

    val path = args.head

    val (_, time) = runWithMeasure(
      {
        val pw = new PrintWriter(new File("error.sql"))
        processFolder(path).toList.grouped(1000).map(l => l.mkString + "commit;\n").foreach(pw.write)
        pw.close()
      }
    )

    println(s"-------------------------------")
    println(s"Elapsed time: $time ms")
  }
}