/**
  * Created by snudurupati on 12/16/16.
  */

import java.util.TimeZone

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.headers.Location
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import scala.io.StdIn
import akka.http.scaladsl.Http
import java.net._

case class Webhook(query: List[String], contexts: List[String], location: Option[String], timezone: Option[String], lang: Option[String], sessionID: Option[Long])

case class WebhookResponse(speech: String, displayText: String, data: Option[Array[String]], contextOut: Option[List[String]], source: String)

case object WebhookResponse extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val webhookFormat = jsonFormat5(WebhookResponse.apply)
}

object WebServer {

  // formats for unmarshalling and marshalling
  //implicit val webhookFormat = jsonFormat6(Webhook)

  //def saveOrder(order: Order): Future[Done] = ???
  val config = ConfigFactory.load()

  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val localhost = InetAddress.getLocalHost
    val interface = localhost.getHostAddress
    val port = 8080

    val route =
      path("webhook") {
        post {
          /*entity(as[Webhook]) {
            webhook => complete {
              // do some function like -- list.add(customer)
              s"got webhook with name ${webhook.location}"
            }*/
            decodeRequest{
              entity(as[String]){ str =>
                println(str)
                complete{
                  WebhookResponse("hey man response!", "hey man response!",Option(Array("")),Option(List("")),
                  "diwo")
                }
              }
            }
        } ~ get {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http based webservice for " +
                "Google Assistant" +
                "webhook.</h1>"))
            }
      }


    val bindingFuture = Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))

    /*println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
      */
  }

}
