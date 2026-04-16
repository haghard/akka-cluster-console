#!/bin/bash

set -e
set -x

APP_OPTS="-server \
          -XX:+UseParallelGC \
          -XX:MaxRAMPercentage=75.0 \
          -XX:InitialRAMPercentage=75.0 \
          -XX:+ExitOnOutOfMemoryError \
          -XshowSettings:system \
          -DHTTP_PORT=${HTTP_PORT} \
          -DENV=${ENV} \
          -DHOSTNAME=${HOSTNAME} \
          -DURL=${URL} \
          -DPSW=${PSW} \
          -DCONFIG="${CONFIG}

#production, development
#

java ${APP_OPTS} -cp ${APP_BASE}/conf -jar ${APP_BASE}/akka-cluster-console-${VERSION}.jar