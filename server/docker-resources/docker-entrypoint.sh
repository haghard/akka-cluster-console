#!/bin/bash

set -e
set -x

APP_OPTS="-server \
          -XX:+UseParallelGC \
          -XX:+UseContainerSupport \
          -XX:+PreferContainerQuotaForCPUCount \
          -XX:MaxRAMFraction=1 \
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