package fly.json.parser

import fly.json.parser.ErrorStats._

import scala.util.{Failure, Success, Try}

object JsonUtl {

  import org.json4s._
  import org.json4s.jackson.Serialization
  import org.json4s.jackson.JsonMethods._

  private implicit val formats = {
    Serialization.formats(FullTypeHints(List(classOf[ErrorStats])))
  }

  def run(input: String): Try[ErrorStats] = {
    Try(parse(input)).flatMap { json =>
      (json \ CID, json \ CLIENT, json \ ERROR) match {
        case (JString(cid), JString(client), JString(error)) => Success(ErrorStats(cid, client, error))
        case _ => Failure(new Exception(s"can't parse ${compact(render(json))}"))
      }
    }
  }
}


