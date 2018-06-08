package au.org.ala.loadtester

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import org.apache.commons.lang3.RandomStringUtils

import scala.concurrent.duration._

class AuthSimulation extends Simulation {

	val username = Option(System.getenv("ALA_USERNAME")).orElse(Option(System.getProperty("ala.username"))).getOrElse(throw new IllegalStateException("Username not specified"))
	val password = Option(System.getenv("ALA_PASSWORD")).orElse(Option(System.getProperty("ala.password"))).getOrElse(throw new IllegalStateException("Password not specified"))
	val authBaseUrl = System.getProperty("au.org.ala.loadtester.auth.baseUrl", "https://auth-test.ala.org.au/")
	val totalUsers = System.getProperty("au.org.ala.loadtester.auth.users", "2400").trim().stripPrefix("\"").stripSuffix("\"").toInt
	val totalMinutes = System.getProperty("au.org.ala.loadtester.auth.minutes", "10").trim().stripPrefix("\"").stripSuffix("\"").toInt


	val httpProtocol = http
    .warmUp("https://www.ala.org.au/")
		.baseURL(authBaseUrl)
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png""", """.*\.woff2"""), WhiteList())
  	.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
  	.acceptEncodingHeader("gzip, deflate, br")
  	.acceptLanguageHeader("en-US,en;q=0.9")
  	.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36")
  	.header("Upgrade-Insecure-Requests", "1")

	val headers_3 = Map(
		"cache-control" -> "max-age=0",
	)

	val headers_5 = Map(
		"cache-control" -> "max-age=0",
		)

	val headers_7 = Map(
		"cache-control" -> "max-age=0",
		)

	val headers_9 = Map(
		"cache-control" -> "max-age=0",
		)

	val headers_11 = Map(
		"cache-control" -> "max-age=0",
		)

    val uri2 = "https://licensebuttons.net/l/by/3.0/80x15.png"
    val uri3 = "https://www.ala.org.au"

	val scn = scenario("AuthSimulation")
		.exec(http("userdetails index")
			.get("/userdetails/")
//			.headers(headers_1)
			)
		.pause(1, 5)
		.exec(http("userdetails my profile")
			.get("/userdetails/myprofile")
//			.headers(headers_2)
			.check()
			.check(form("form#fm1").saveAs("loginForm")))
		.pause(5, 10)
		.exec(http("cas login request")
			.post("/cas/login?service=https%3A%2F%2Fauth-test.ala.org.au%2Fuserdetails%2Fmyprofile")
			.headers(headers_3)
			.formParam("username", username)
			.formParam("password", password)
			.formParam("execution", "${loginForm.execution(0)}")
			.formParam("_eventId", "submit")
			.formParam("geolocation", "")
			.check(status.is(200)))
		.pause(1, 10)
		.exec(http("userdetails edit account")
			.get("/userdetails/registration/editAccount")
//			.headers(headers_4)
  			.check(form("form#updateAccountForm").saveAs("updateForm"))
		)
		.pause(4,8)
		.exec(http("userdetails submit profile update")
			.post("/userdetails/registration/update")
			.headers(headers_5)
			.formParam("SYNCHRONIZER_TOKEN", "${updateForm.SYNCHRONIZER_TOKEN(0)}")
			.formParam("SYNCHRONIZER_URI", "${updateForm.SYNCHRONIZER_URI(0)}")
			.formParam("firstName", RandomStringUtils.randomAlphabetic(5,10))
			.formParam("lastName", "${updateForm.lastName(0)}")
			.formParam("email", "${updateForm.email(0)}")
			.formParam("organisation", RandomStringUtils.randomAlphabetic(5,10))
			.formParam("country", "${updateForm.country(0)}")
			.formParam("state", "${updateForm.state(0)}")
			.formParam("city", "${updateForm.city(0)}"))
		.pause(4,8)
		.exec(http("apikey index")
			.get("/apikey")
//			.headers(headers_6)
		)
		.pause(1,10)
		.exec(http("apikey check key")
			.post("/apikey/checkKey/checkKey")
			.headers(headers_7)
			.formParam("apikey", "e7dcfaed-325e-49a8-b6bd-4316e1679a4c")
			.formParam("Check key", "Check key"))

	setUp(scn.inject(rampUsers(totalUsers) over (totalMinutes minutes))).protocols(httpProtocol)
//	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}