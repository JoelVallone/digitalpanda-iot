package org.digitalpanda.iot.measure.source

import akka.actor.{ActorRef, Props}
import com.datastax.driver.core.{Cluster, Session}
import org.digitalpanda.iot.MeasureType.MeasureType
import org.digitalpanda.iot._

import scala.concurrent.duration.FiniteDuration

object CassandraDbSource {

  def props(cassandraContactPoint: String, cassandraPort : Int,
            samplingPeriod: FiniteDuration,
            aggregator: ActorRef) : Props =
    props(cassandraContactPoint, cassandraPort, samplingPeriod, List.empty ,aggregator)

  def props(cassandraContactPoint: String, cassandraPort : Int,
            samplingPeriod: FiniteDuration, targetMeasures : List[(Location, MeasureType)],
            aggregator: ActorRef) : Props =
    Props(new CassandraDbSource(
      getCassandraDbSession(cassandraContactPoint, cassandraPort),
      samplingPeriod, targetMeasures, aggregator)
    )

  def getCassandraDbSession(contactPoint: String, port: Int) : Session =
    Cluster.builder()
      .addContactPoint(contactPoint)
      .withPort(port)
      .build()
      .connect("iot")
}

class CassandraDbSource(dbSession: Session,
                        samplingPeriod: FiniteDuration,
                        targetMeasures : List[(Location, MeasureType)],
                        aggregator: ActorRef) extends SourceActor(samplingPeriod, targetMeasures, aggregator) {

  override def sampleNewMeasures(requestId: Long): NewMeasures = {
    import collection.JavaConverters._
    val measuresMap = dbSession
      .execute("SELECT * from sensor_measure_latest")
      .asScala
      .map( row => {
        //log.info(row.toString)
        (row.getString("location"), MeasureType.withName(row.getString("measure_type"))) -> (row.getTimestamp("timestamp").getTime, row.getDouble("value"))
      })
      .toMap
    NewMeasures(requestId, measuresMap)
  }



}
