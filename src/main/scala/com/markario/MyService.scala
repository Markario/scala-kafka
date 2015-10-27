package com.markario

/**
 * Created by markzepeda on 10/27/15.
 */
package com.markario

import akka.actor.{ActorRefFactory, Actor}
import shapeless.{HNil, HList}
import spray.json.DefaultJsonProtocol._
import spray.json._
import spray.routing._
import spray.routing.directives.RouteDirectives

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
      val intsTo = num to num2
      val json = intsTo.toList.toJson
      complete(json.toString)
    }
}

trait ServicePathComponent {
  val servicePath: ServicePath
  trait ServicePath extends Directives{
    def path[L <: HList]: Directive[L]
  }
}

trait ServiceRouteComponent {
  val serviceRoute: ServiceRoute
  trait ServiceRoute extends RouteDirectives{
    def route[L <: HList]: (L => Route)
  }
}

trait ServiceActorComponent {
  trait ServiceActor extends Actor with HttpService{
    this: ServiceRouteComponent with ServicePathComponent=>
    override def receive: Actor.Receive = runRoute(servicePath.path.happly(serviceRoute.route))
    override def actorRefFactory = context
  }
}

trait ServiceRouteImpl extends ServiceRouteComponent{
  class IntRoute extends ServiceRoute{
    override def route[L <: shapeless.::[Int, shapeless.::[Int, HNil]]] = { (list: L) =>
      val intsTo = list.head to list.tail.head
      val json = intsTo.toList.toJson.toString
      complete(json)
    }
  }
}

trait ServicePathImpl extends ServicePathComponent {
  class IntPath extends ServicePath {
    override def path[L <: shapeless.::[Int, shapeless.::[Int, HNil]]]: Directive[shapeless.::[Int, shapeless.::[Int, HNil]]] = path("api" / "int" / IntNumber / IntNumber)
  }
}

object IntService extends ServiceActorComponent{
  class IntServiceActor extends ServiceActor with ServiceRouteImpl with ServicePathImpl{
    override val serviceRoute: ServiceRoute = new IntRoute
    override val servicePath: ServicePath = new IntPath
  }
}