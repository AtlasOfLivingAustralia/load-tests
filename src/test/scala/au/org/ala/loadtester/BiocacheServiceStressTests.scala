package au.org.ala.loadtester

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class SolrStressTests extends Simulation {

  object Search {

    val logFileLocation = System.getProperty("au.org.ala.loadtester.solr.logfile")

    val feeder = tsv(logFileLocation).circular

    val search =
      feed(feeder)
        .exec(http("/")
          .get("${params}")
        )
        .pause(1)
  }

  val biocacheServiceServers = System.getProperty("au.org.ala.loadtester.biocacheservice.servers").trim().stripPrefix("\"").stripSuffix("\"").split(" ")

  val constantUsersPerSecond = System.getProperty("au.org.ala.loadtester.biocacheservice.constantuserspersecond", "100").trim().stripPrefix("\"").stripSuffix("\"").toInt

  val peakRequestsPerSecond = System.getProperty("au.org.ala.loadtester.biocacheservice.peakrequestspersecond", "100").trim().stripPrefix("\"").stripSuffix("\"").toInt

  val latterRequestsPerSecond = System.getProperty("au.org.ala.loadtester.biocacheservice.latterrequestspersecond", "50").trim().stripPrefix("\"").stripSuffix("\"").toInt

  println("Biocache Service Servers: " + biocacheServiceServers.mkString(","))
  println("Constant users per second: " + constantUsersPerSecond)
  println("Peak requests per second: " + peakRequestsPerSecond)
  println("Latter requests per second: " + latterRequestsPerSecond)

  // Scala magic incantation ":_*" to convert the array from above to match the varargs method
  val httpProtocol = http
    .baseURLs(
        biocacheServiceServers:_*
    )
    .inferHtmlResources(BlackList( """.*\.js""", """.*\.css""", """.*\.css.*=.*""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""",
      """.*\.(t|o)tf""", """.*\.png"""),
      WhiteList()).disableWarmUp

  val biocacheServiceTests = scenario("Users").exec(Search.search)

  setUp(
    biocacheServiceTests.inject(constantUsersPerSec(constantUsersPerSecond) during (60 minutes))).throttle(
    reachRps(peakRequestsPerSecond) in (45 minutes),
    jumpToRps(latterRequestsPerSecond),
    holdFor(15 minutes)).maxDuration(60 minutes).protocols(httpProtocol)

}
