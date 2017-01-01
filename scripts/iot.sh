#!/usr/bin/env bash
export TARGET_REST_ENDPOINT="http://192.168.0.234:8080/sensor"
export SENSOR_LOCATION="indoor"

case "$1" in
  start)
    echo "Starting iot"
    java -jar /home/pi/sense/iot*.jar
    ;;
  stop)
    echo "Stopping iot"
    pkill -f ".*iot.*.jar";
    ;;
  *)
    echo "Usage: /etc/init.d/iot {start|stop}"
    exit 1
    ;;
esac

exit 0;
