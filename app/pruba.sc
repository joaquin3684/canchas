import java.time.LocalDateTime

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

val mapper = new ObjectMapper()
mapper.registerModule(DefaultScalaModule)
mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


@JsonCreator
case class Persona(@JsonProperty("nombre") nombre: String, @JsonProperty("edad") edad: Int)

val persona = Persona("pepe", 32)

val string = "{\"nombre\":1,\"edad\": 20\"}"

val j = mapper.readValue(string, classOf[Persona])

val edadNueva = persona.edad * 2

val json = mapper.writeValueAsString(persona)
