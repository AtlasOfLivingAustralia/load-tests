package au.org.ala.loadtester

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.language.postfixOps

class ALAServiceStressTests extends Simulation {

  object Search {

    val logFileLocation: String = System.getProperty("au.org.ala.loadtester.alaservice.logfile")

    val feeder: RecordSeqFeederBuilder[String] = tsv(logFileLocation).circular

    // Biocache-service has some methods that return 406 for the default "*/*", so customisation is necessary
    val sentHeaders: Map[String, String] = Map("Accept" -> "application/json,image/png,application/xml;q=0.2,text/html;q=0.2,*/*;q=0.1", "User-Agent" -> "ALA load-test https://github.com/AtlasOfLivingAustralia/load-tests")
    // the ${params}" should match the column name in the logfile
    val search: ChainBuilder =
      feed(feeder)
        .exec(http("Biocache Service Load Tests")
          .get("${params}")
          .headers(sentHeaders)
        )
        .pause(1)
  }

  val alaServiceServers: Array[String] = System.getProperty("au.org.ala.loadtester.alaservice.servers").trim().stripPrefix("\"").stripSuffix("\"").split(" ")

  val constantUsersPerSecond: Int = System.getProperty("au.org.ala.loadtester.alaservice.constantuserspersecond", "100").trim().stripPrefix("\"").stripSuffix("\"").toInt

  val peakRequestsPerSecond: Int = System.getProperty("au.org.ala.loadtester.alaservice.peakrequestspersecond", "100").trim().stripPrefix("\"").stripSuffix("\"").toInt

  val latterRequestsPerSecond: Int = System.getProperty("au.org.ala.loadtester.alaservice.latterrequestspersecond", "50").trim().stripPrefix("\"").stripSuffix("\"").toInt

  val peakDuration: Int = System.getProperty("au.org.ala.loadtester.alaservice.peakduration", "45").trim().stripPrefix("\"").stripSuffix("\"").toInt

  val latterDuration: Int = System.getProperty("au.org.ala.loadtester.alaservice.latterduration", "15").trim().stripPrefix("\"").stripSuffix("\"").toInt

  val maxDuration: Int = System.getProperty("au.org.ala.loadtester.alaservice.maxduration", "60").trim().stripPrefix("\"").stripSuffix("\"").toInt

  println("ALA Service Servers: " + alaServiceServers.mkString(","))
  println("Constant users per second: " + constantUsersPerSecond)
  println("Peak requests per second: " + peakRequestsPerSecond)
  println("Latter requests per second: " + latterRequestsPerSecond)
  println("Peak duration (in minutes): " + peakDuration)
  println("Latter duration (in minutes): " + latterDuration)
  println("Max duration (in minutes): " + maxDuration)

  // Scala magic incantation ":_*" to convert the array from above to match the varargs method
  val httpProtocol: HttpProtocolBuilder = http
    .baseURLs(
        alaServiceServers:_*
    )
    .inferHtmlResources(BlackList( """.*\.js""", """.*\.css""", """.*\.css.*=.*""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""",
      """.*\.(t|o)tf""", """.*\.png"""),
      WhiteList()).disableWarmUp

  val alaServiceTests: ScenarioBuilder = scenario("Users").exec(Search.search)

  setUp(
    alaServiceTests.inject(constantUsersPerSec(constantUsersPerSecond) during (maxDuration minutes))).throttle(
    reachRps(peakRequestsPerSecond) in (peakDuration minutes),
    jumpToRps(latterRequestsPerSecond),
    holdFor(latterDuration minutes)).maxDuration(maxDuration minutes).protocols(httpProtocol)

}
