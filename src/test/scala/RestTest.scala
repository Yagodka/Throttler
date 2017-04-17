import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.scaladsl.model.StatusCodes

class RestTest extends RestApiTest {

  test("Request for unauthorized user") {
    Get(s"/service") ~> server.route ~> check {
      response.status shouldBe StatusCodes.OK
    }
  }

  test("Request for authorized user") {
    Get(s"/service")
      .addCredentials(HttpCredentials.createBasicHttpCredentials("user01", "psw01")) ~> server.route ~> check {
      response.status shouldBe StatusCodes.OK
    }
  }
}
