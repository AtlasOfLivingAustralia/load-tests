package au.org.ala.loadtester

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class SolrStressTests extends Simulation {


  object Search {

    val maxFacetCount = 1000
    val feeder = tsv("./solr.q.all.log").circular


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

  val httpProtocol = http
    .baseURLs(
      "http://server1:8983",
      "http://server2:8983",
    )
    .inferHtmlResources(BlackList( """.*\.js""", """.*\.css""", """.*\.css.*=.*""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""",
      """.*\.(t|o)tf""", """.*\.png"""),
      WhiteList()).disableWarmUp


  val solrTests = scenario("Users").exec(Search.search)

  setUp(
    solrTests.inject(constantUsersPerSec(100) during (60 minutes))).throttle(
    reachRps(100) in (45 minutes),
    jumpToRps(50),
    holdFor(15 minutes)).maxDuration(60 minutes).protocols(httpProtocol)

}

