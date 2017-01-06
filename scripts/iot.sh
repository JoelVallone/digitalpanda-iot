#!/usr/bin/env bash
export CONFIGURATION_FILE="/home/pi/sense/configuration.properties"
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
