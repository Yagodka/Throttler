import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class GatlingTestWithThrottle extends Simulation {

  val scn = scenario("With throttle")
    .exec(http("Authorized user")
      .get("http://localhost:8080/service")
      .basicAuth("user01", "psw01"))

  setUp {
    scn.inject(constantUsersPerSec(200) during (1 minute))
  }
}
