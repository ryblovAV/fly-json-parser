package fly.json.parser

import fly.json.parser.ErrorStats._

import scala.util.{Failure, Success, Try}

object JsonUtl {
  type JsonParser = String => Try[ErrorStats]
}

object Json4sUtl {

  import org.json4s._
  import org.json4s.jackson.Serialization
  import org.json4s.jackson.JsonMethods._

  private implicit val formats = {
    Serialization.formats(FullTypeHints(List(classOf[ErrorStats])))
  }

  def run(input: String): Try[ErrorStats] = {
    Try(parse(input)).flatMap { json =>
      (json \ CID, json \ CLIENT, json \ CURRENT, json \ ERROR) match {
        case (JString(cid), JString(client), JString(current), JString(error)) => Success(ErrorStats(cid, client, current, error))
        case _ => Failure(new Exception(s"can't parse ${compact(render(json))}"))
      }
    }
  }
}

object CirceJsonUtl {

  import io.circe.parser._
  import io.circe.{Decoder, _}

  implicit val errorDecoder: Decoder[ErrorStats] = Decoder.forProduct4(CID, CLIENT, CURRENT, ERROR)(ErrorStats.apply)

  def run(input: String): Try[ErrorStats] = {
    parse(input) match {
      case Left(error) => Failure(error)
      case Right(json) => json.as[ErrorStats].toTry
    }
  }
}


