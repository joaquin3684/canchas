package actions

import javax.inject.Inject

import play.api.libs.json.JsValue
import play.api.mvc._
import requests.JsonMapperRequest
import services.JsonMapper

import scala.concurrent.{ExecutionContext, Future}

class JsonMapperAction @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext)
 extends ActionBuilder[JsonMapperRequest, AnyContent] with ActionTransformer[Request, JsonMapperRequest] {

  def transform[A](request: Request[A]) = Future.successful {
    val jsonMapper = new JsonMapper
    val json = request.body.asInstanceOf[AnyContentAsJson].asJson.get.toString
    val rootNode = jsonMapper.getJsonNode(json)
    new JsonMapperRequest(rootNode, json, request)
  }
}
