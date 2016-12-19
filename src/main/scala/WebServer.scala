/**
  * Created by snudurupati on 12/16/16.
  */

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.Http

case class WebhookResponse(speech: String, displayText: String, data: Option[Array[String]], contextOut: Option[List[String]], source: String)

case class Metadata(intentId: String, webhookUsed: String, intentName: String)
case class Fulfillment(speech: String, messages: Array[String])
case class Parameter(dummy: String)
case class Context(name: String, parameters: String, lifespan: Int)
case class Status(code: Int, errorType: String)
case class Result(source: String, resolvedQuery: String, action: String, actionIncomplete: Boolean, parameters:
Parameter, contexts: Array[Context], fulfillment: Fulfillment, metadata: Metadata, score: Int)

case class Webhook(id: String, timestamp: String, result: Result, status: Status, sessionId:
String, originalRequest: Option[String])

case object WebhookResponse extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val webhookRespFormat = jsonFormat5(WebhookResponse.apply)
}

object WebServer {

  // formats for unmarshalling and marshalling
  implicit val metadataFormat = jsonFormat3(Metadata.apply)
  implicit val fullfillmentFormat = jsonFormat2(Fulfillment.apply)
  implicit val ststusFormat = jsonFormat2(Status.apply)
  implicit val parameterFormat = jsonFormat1(Parameter.apply)
  implicit val contextFormat = jsonFormat3(Context.apply)
  implicit val resultFormat = jsonFormat9(Result.apply)
  implicit val webhookFormat = jsonFormat6(Webhook.apply)
  implicit val webhookRespFormat = jsonFormat5(WebhookResponse.apply)

  val config = ConfigFactory.load()

  def fetchQueryAnswer(query: String): WebhookResponse = {

    val data = Option(Array(""))
    val context = Option(List(""))
    val source = "diwo"
    val speech = query match {
      case "how are my sales today" => "excellent! very good sales today."
      case "what is my sales forecast for next month" => "great forecast, it will be very good"
    }
    val displayText = speech
    WebhookResponse(speech, speech, data, context, source)

  }

  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    /* needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher*/

    val route =
      path("webhook") {
        post {
          entity(as[Webhook]) {
            webhook => complete {
              // generate unique response for every matched query
              fetchQueryAnswer(webhook.result.resolvedQuery)
            }
            /*decodeRequest{ //unmarshalls query JSON as a string
              entity(as[String]){ str =>
                println(str)
                complete{
                  WebhookResponse("hey man response!", "hey man response!",Option(Array("")),Option(List("")),
                  "diwo")
                }*/
          }
        } ~ get {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http based webservice for " +
                "Google Assistant" + ".</h1>"))
            }
      }

    val bindingFuture = Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))

  }

}
