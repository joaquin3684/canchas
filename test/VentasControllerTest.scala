import actions.{AuthenticatedAction, GetAuthenticatedAction}
import akka.actor.ActorSystem
import controllers.{AsyncController, CountController, VentaController}
import org.scalatestplus.play._
import play.api.mvc.{AnyContent, BodyParsers, Results}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import services.{Counter, JsonMapper}

/**
  * Unit tests can run without a full Play application.
  */
class VentasControllerTest extends PlaySpec with Results {

  "CountController" should {

    "return a valid result with action" in {
      val counter: Counter = new Counter {
        override def nextCount(): Int = 49
      }
      val controller = new CountController(stubControllerComponents(), counter)
      val result = controller.count(FakeRequest())
      contentAsString(result) must equal("49")
    }
  }

 /* "VentasController" should {
    "crear una venta nueva" in {
      val jsonMapper = new JsonMapper()
      val stubParser = Helpers.stubBodyParser(AnyContent("hello"))
      val authAction = new AuthenticatedAction(stubParser)
      val getAuthAction = new GetAuthenticatedAction
      val controller = new VentaController(Helpers.stubControllerComponents(bodyParser = stubParser), jsonMapper, authAction = )
    }
  }
*/

  "AsyncController" should {

    "return a valid result on action.async" in {
      // actor system will create threads that must be cleaned up even if test fails
      val actorSystem = ActorSystem("test")
      try {
        implicit val ec = actorSystem.dispatcher
        val controller = new AsyncController(stubControllerComponents(), actorSystem)
        val resultFuture = controller.message(FakeRequest())
        contentAsString(resultFuture) must be("Hi!")
      } finally {
        // always shut down actor system at the end of the test.
        actorSystem.terminate()
      }
    }

  }

}

