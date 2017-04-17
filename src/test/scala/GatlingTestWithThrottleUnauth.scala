import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class GatlingTestWithThrottleUnauth extends Simulation {

  val scn = scenario("With throttle")
    .exec(http("Unauthorized user (without token)")
      .get("http://localhost:8080/service"))

  setUp {
    scn.inject(constantUsersPerSec(200) during (1 minute))
  }
}
