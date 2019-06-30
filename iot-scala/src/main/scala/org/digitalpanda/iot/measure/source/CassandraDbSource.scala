package org.digitalpanda.iot.measure.source

import akka.actor.{ActorRef, Props}
import com.datastax.driver.core.{Cluster, Session}
import org.digitalpanda.iot.{MeasureType, NewMeasure}

import scala.concurrent.duration.FiniteDuration

object CassandraDbSource {

  def props(cassandraContactPoint: String, cassandraPort : Int,
            samplingPeriod: FiniteDuration, measureLocation: String,
            aggregator: ActorRef) : Props =
    Props(new CassandraDbSource(
      getCassandraDbSession(cassandraContactPoint, cassandraPort),
      samplingPeriod, measureLocation, aggregator)
    )

  def getCassandraDbSession(contactPoint: String, port: Int) : Session =
    Cluster.builder()
      .addContactPoint(contactPoint)
      .withPort(port)
      .build()
      .connect("iot")
}

class CassandraDbSource(dbSession: Session, samplingPeriod: FiniteDuration, measureLocation : String, aggregator: ActorRef)
  extends SourceActor(samplingPeriod, measureLocation, aggregator) {

  override def sampleNewMeasure(requestId: Long): NewMeasure = {
    val row = dbSession
      .execute(
        s"SELECT * from sensor_measure_latest" +
          s"WHERE location = $measureLocation AND measure_type = ${MeasureType.TEMPERATURE}")
      .one()
    NewMeasure(
      requestId,
      measureLocation,
      MeasureType.TEMPERATURE,
      row.getLong("timestamp"),
      row.getDouble("value")
    )
  }



}
