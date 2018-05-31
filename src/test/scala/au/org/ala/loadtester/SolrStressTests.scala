package au.org.ala.loadtester

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class SolrStressTests extends Simulation {

  object Search {

    val logFileLocation = System.getProperty("au.org.ala.loadtester.solr.logfile")

    val maxFacetCount = System.getProperty("au.org.ala.loadtester.solr.maxfacetcount", "1000").trim().stripPrefix("\"").stripSuffix("\"").toInt

    val feeder = tsv(logFileLocation).circular

    def applyFacetLimit(query:String, facetMax:Int): String ={
      val params = query.split("&").sorted
      var result = ""
      var i =0
      var j =0
      for (param <- params){
        if (param.startsWith("facet.field=") && i < facetMax) {
          result += param + "&"
          i += 1
        }else if (param.matches("f\\.\\w+\\.facet\\.sort=.*") && j < facetMax) {
          result += param + "&"
          j += 1
        }else
          result += param + "&"
      }
      result.dropRight(1)
    }
    val search =
      feed(feeder)
        .exec(http("/solr/biocache/select")
          .post("/solr/biocache/select")
          .body(StringBody( applyFacetLimit("${params}", maxFacetCount))).header("Content-Type", "application/x-www-form-urlencoded")
        )
        .pause(1)
  }

  val solrServers = System.getProperty("au.org.ala.loadtester.solr.servers").trim().stripPrefix("\"").stripSuffix("\"").split(" ")

  val constantUsersPerSecond = System.getProperty("au.org.ala.loadtester.solr.constantuserspersecond", "100").trim().stripPrefix("\"").stripSuffix("\"").toInt

  val peakRequestsPerSecond = System.getProperty("au.org.ala.loadtester.solr.peakrequestspersecond", "100").trim().stripPrefix("\"").stripSuffix("\"").toInt

  val latterRequestsPerSecond = System.getProperty("au.org.ala.loadtester.solr.latterrequestspersecond", "50").trim().stripPrefix("\"").stripSuffix("\"").toInt

  println("Solr servers: " + solrServers.mkString(","))
  println("Constant users per second: " + constantUsersPerSecond)
  println("Peak requests per second: " + peakRequestsPerSecond)
  println("Latter requests per second: " + latterRequestsPerSecond)

  // Scala magic incantation ":_*" to convert the array from above to match the varargs method
  val httpProtocol = http
    .baseURLs(
        solrServers:_*
    )
    .inferHtmlResources(BlackList( """.*\.js""", """.*\.css""", """.*\.css.*=.*""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""",
      """.*\.(t|o)tf""", """.*\.png"""),
      WhiteList()).disableWarmUp

  val solrTests = scenario("Users").exec(Search.search)

  setUp(
    solrTests.inject(constantUsersPerSec(constantUsersPerSecond) during (60 minutes))).throttle(
    reachRps(peakRequestsPerSecond) in (45 minutes),
    jumpToRps(latterRequestsPerSecond),
    holdFor(15 minutes)).maxDuration(60 minutes).protocols(httpProtocol)

}
