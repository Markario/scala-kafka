package com.markario

/**
 * Created by markzepeda on 10/27/15.
 */
package com.markario

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRefFactory}
import shapeless.HNil
import spray.httpx.marshalling.ToResponseMarshallable
import spray.routing._
import spray.json._
import DefaultJsonProtocol._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val myRoute =
    path("api" / "int" / IntNumber / IntNumber) { (num, num2) =>
      val intsTo = 1 to num
      val json = intsTo.toList.toJson
      complete(json.toString)
    }
}

trait ServiceActionComponent {
  val serviceAction: ServiceAction
  trait ServiceAction {
    def receiveAction: (_ => ToResponseMarshallable)
  }
}

trait ServiceDirectiveComponent {
  val serviceDirective: ServiceDirective
  trait ServiceDirective {
    def directive: Directive[_]
  }
}

trait ServiceRouteComponent {
  val serviceRoute: ServiceRoute
  trait ServiceRoute extends HttpService {
    def route: (_ => ToResponseMarshallable) => Route
  }
}

trait ServiceActorComponent {
  trait ServiceActor extends Actor with HttpService{
    this: ServiceActionComponent with ServiceRouteComponent with ServiceDirectiveComponent =>
    override def receive: Actor.Receive = runRoute(serviceRoute.route(serviceAction.receiveAction))
  }
}

//trait ServiceActionImpl extends ServiceActionComponent {
//  class IntCounter extends ServiceAction {
//    override def receiveAction: (Int) => ToResponseMarshallable = { num =>
//      val intsTo = 1 to num
//      val json = intsTo.toList.toJson
//      json.toString
//    }
//  }
//}

object IntService extends ServiceActorComponent{

}