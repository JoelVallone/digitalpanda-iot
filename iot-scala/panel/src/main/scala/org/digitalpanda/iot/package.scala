package org.digitalpanda

import org.digitalpanda.iot.MeasureType.MeasureType

package object iot {

  type Location = String
  type Timestamp = Long
  type Measure = Double

  object MeasureType extends Enumeration {
    type MeasureType = Value
    val PRESSURE, TEMPERATURE, HUMIDITY = Value
  }

  case class NewMeasure(requestId : Long, location: Location, measureType: MeasureType, timestamp: Timestamp,  value: Measure)

  case class MeasureQuery(requestId : Long, location: Location, measureType: MeasureType)
  case class MeasureQueryResponse(requestId : Long, location: Location, measureType: MeasureType, timestamp: Option[Timestamp],  value: Option[Measure])
}
