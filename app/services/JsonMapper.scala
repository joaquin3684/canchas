package services

import JsonFormats.{DateTimeDeserializer, DateTimeSerializer, LongDeserializer}
import akka.http.scaladsl.model.DateTime
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{JsonMappingException, JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import play.api.libs.json._

@JsonInclude(Include.NON_DEFAULT)
class JsonMapper {

  var mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  val module = new SimpleModule()

  module.addSerializer[DateTime](classOf[DateTime], new DateTimeSerializer())
  module.addDeserializer[DateTime](classOf[DateTime], new DateTimeDeserializer())
  module.addDeserializer[java.lang.Long](classOf[java.lang.Long], new LongDeserializer())
  mapper.registerModule(module)

  def toJsonString[A : Manifest](value: A): String = mapper.writeValueAsString(value)

  def toJson[A : Manifest](value: A): JsValue = Json.parse(toJsonString(value))

  def fromJson[A : Manifest](json: String): A = mapper.readValue[A](json)

  def getJsonNode(json: String): JsonNode = mapper.readTree(json)

  def removeElement(node: JsonNode, valueToRemove: String): JsonNode = {
    if(node.isObject){
      node.asInstanceOf[ObjectNode].remove(valueToRemove)
    } else {
      throw new JsonMappingException("not an object " + node)
    }
  }

  def addNode(fieldName: String, newNode: JsonNode, oldNode: JsonNode) = {
    if(oldNode.isObject){
      oldNode.asInstanceOf[ObjectNode].set(fieldName, newNode)
    } else {
      throw new JsonMappingException("not an object " + oldNode)
    }
  }

  def putElement(node: JsonNode, fieldName: String, value: String) = {
    if(node.isObject){
      node.asInstanceOf[ObjectNode].put(fieldName, value)
    } else {
      throw new JsonMappingException("not an object " + node)
    }
  }


  def getAndRemoveElement(node: JsonNode, valueToRemove: String): String = {
    removeElement(node, valueToRemove).toString
  }




}
