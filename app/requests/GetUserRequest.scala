package requests

import com.fasterxml.jackson.databind.JsonNode
import play.api.mvc.{Request, WrappedRequest}

class GetUserRequest[A](val obrasSociales: Seq[String], val user: String, request: Request[A]) extends WrappedRequest[A](request){

}
