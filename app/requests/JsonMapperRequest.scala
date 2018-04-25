package requests

import com.fasterxml.jackson.databind.JsonNode
import play.api.mvc.{Request, WrappedRequest}

class JsonMapperRequest[A](val rootNode: JsonNode, val json: String, request: Request[A]) extends WrappedRequest[A](request){

}
