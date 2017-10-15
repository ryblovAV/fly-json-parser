package fly.json.parser

import fly.json.parser.ErrorStats._

object JsonUtl {

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


