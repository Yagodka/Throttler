import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.trottler._
import org.scalatest._

import scala.concurrent.duration._

trait RestApiTest extends FunSuite
  with BeforeAndAfterAll
  with Matchers
  with ScalatestRouteTest {

  val host = "localhost"
  val port = 8080
  val graceRps = 50
  val slaDb = Map("dXNlcjAxOnBzdzAx" -> Sla("user01", 80))

  protected val slaService: ActorRef =
    system.actorOf(Props(new SlaService(slaDb)), "SlaService")
  protected val throttlingService: ActorRef =
    system.actorOf(ThrottlingService.props(graceRps, slaService), "ThrottlingService")
  val ticker: ActorRef = system.actorOf(TickActor.props(throttlingService), "Ticker")
  system.scheduler.schedule(0 second, 1 second, ticker, ResetCounters)
  val server = Server(slaService, throttlingService, ticker)

  override protected def beforeAll(): Unit = {
    server.start("localhost", 8080)
  }
}
