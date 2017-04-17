import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class GatlingTestWithoutThrottle extends Simulation {

  val scn = scenario("Without throttle")
    .exec(http("Without throttle")
      .get("http://localhost:8080/service_without_throttle"))

  setUp {
    scn.inject(constantUsersPerSec(200) during (1 minute))
  }
}
