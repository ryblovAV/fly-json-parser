package fly.json.parser

import fly.json.parser.ErrorStats._

sealed trait JsonUtl[T] {
  def run(input: String): Either[T, ErrorStats]
}

object CirceJsonUtl extends JsonUtl[io.circe.Error] {

  import io.circe.parser._
  import io.circe.{Decoder, _}

  implicit val errorDecoder: Decoder[ErrorStats] = Decoder.forProduct3(CID, CLIENT, ERROR)(ErrorStats.apply)

  override def run(input: String): Either[Error, ErrorStats] = {
    parse(input) match {
      case Left(error) => Left(error)
      case Right(json) => json.as[ErrorStats]
    }
  }
}

object Json4sUtl extends JsonUtl[Throwable] {

  import org.json4s._
  import org.json4s.jackson.Serialization
  import org.json4s.jackson.JsonMethods._

  private implicit val formats = {
    Serialization.formats(FullTypeHints(List(classOf[ErrorStats])))
  }

  def run(input: String): Either[Throwable, ErrorStats] = {

    val json = parse(input)

    (json \ CID, json \ CLIENT, json \ ERROR) match {
      case (JString(cid), JString(client), JString(error)) => Right(ErrorStats(cid, client, error))
      case _ => Left(new Exception(s"can't parse $input"))
    }
  }
}


