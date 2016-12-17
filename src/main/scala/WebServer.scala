/**
  * Created by snudurupati on 12/16/16.
  */

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import scala.io.StdIn
import akka.http.scaladsl.Http


object WebServer {

  case class Webhook(name: String)

  // formats for unmarshalling and marshalling
  implicit val webhookFormat = jsonFormat1(Webhook)

  //def saveOrder(order: Order): Future[Done] = ???

  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path("webhook") {
        post {
          entity(as[Webhook]) {
            webhook => complete {
              // do some function like -- list.add(customer)
              s"got webhook with name ${webhook.name}"
            }
          }
        } ~ get {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http based webservice for " +
                "Google Assistant" +
                "webhook.</h1>"))
            }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}
