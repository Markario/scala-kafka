package com.markario

/**
 * Created by markzepeda on 10/27/15.
 */
package com.markario

import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import kafka.producer.KafkaProducer
import kafka.serializer.StringDecoder
import spray.can.Http

import scala.concurrent.duration._

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[IntService.IntServiceActor], "demo-service")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)

  def test = {
    val BROKERS = "192.168.86.10:9092"
    val ZOOKEEPER_CONNECT = "192.168.86.5:2181"

    val messages = 100
    val timeout = 10.seconds
    val batchTimeout = 5.seconds

    val testMessage = UUID.randomUUID().toString

    val testTopic = UUID.randomUUID().toString
    val testGroupId = UUID.randomUUID().toString

//    info(">>> starting sample broker testing")
//    val producer = new KafkaProducer(testTopic, BROKERS)
//    producer.send(testMessage)
//    info(">>> message sent")
//
//    var testStatus = false
//
//    val actor = system.actorOf(Props(new ConsumerActor(testActor, (message: String) => {
//      if (message == testMessage) testStatus = true
//    })))
//    val consumerProps = AkkaConsumerProps.forSystem(system, ZOOKEEPER_CONNECT, testTopic, testGroupId, 1, new StringDecoder(), new StringDecoder(), actor)
//    val consumer = new AkkaConsumer(consumerProps)
//    info(">>> starting consumer")
//    consumer.start()
//    expectMsg(testMessage)
//    testStatus must beTrue
//    consumer.stop()
  }
}