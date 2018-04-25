package requests

import com.fasterxml.jackson.databind.JsonNode
import play.api.mvc.{Request, WrappedRequest}

class UserRequest[A](val obrasSociales: Seq[String], val rootNode: JsonNode, val json: String, val user: String, request: Request[A]) extends WrappedRequest[A](request){

}