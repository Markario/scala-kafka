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

trait ServicePathComponent[L <: HList] {
  val servicePath: ServicePath
  trait ServicePath extends Directives{
    def path: Directive[L]
  }
}

trait ServiceRouteComponent[L] {
  val serviceRoute: ServiceRoute
  trait ServiceRoute extends RouteDirectives{
    def route: (L => Route)
  }
}

trait ServiceActorComponent[L <: HList] {
  trait ServiceActor extends Actor with HttpService{
    this: ServiceRouteComponent[L] with ServicePathComponent[L] =>
    override def receive: Actor.Receive = runRoute(servicePath.path.happly(serviceRoute.route))
    override def actorRefFactory = context
  }
}

trait ServiceRouteImpl extends ServiceRouteComponent[shapeless.::[Int, shapeless.::[Int, HNil]]]{
  class IntRoute extends ServiceRoute{
    override def route = { (list) =>
      val intsTo = list.head to list.tail.head
      val json = intsTo.toList.toJson.toString
      complete(json).asInstanceOf[Route]
    }
  }
}

trait ServicePathImpl extends ServicePathComponent[shapeless.::[Int, shapeless.::[Int, HNil]]] {
  class IntPath extends ServicePath {
    override def path = path("api" / "int" / IntNumber / IntNumber)
  }
}

object IntService extends ServiceActorComponent[shapeless.::[Int, shapeless.::[Int, HNil]]]{
  class IntServiceActor extends ServiceActor with ServiceRouteImpl with ServicePathImpl{
    override val serviceRoute: ServiceRoute = new IntRoute
    override val servicePath: ServicePath = new IntPath
  }
}