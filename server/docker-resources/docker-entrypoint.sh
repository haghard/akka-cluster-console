#!/bin/bash

set -e
set -x

APP_OPTS="-server \
          -XX:MaxGCPauseMillis=400 \
          -XX:+UseStringDeduplication \
          -XX:+UseG1GC \
          -XX:ConcGCThreads=4 -XX:ParallelGCThreads=4 \
          -XX:+UseContainerSupport \
          -XX:+PreferContainerQuotaForCPUCount \
          -XX:MaxRAMFraction=1 \
          -XshowSettings \
          -DHTTP_PORT=${HTTP_PORT} \
          -DENV=${ENV} \
          -DHOSTNAME=${HOSTNAME} \
          -DPASSWORD=${PASSWORD} \
          -DCONFIG="${CONFIG}

#production, development
#

java ${APP_OPTS} -cp ${APP_BASE}/conf -jar ${APP_BASE}/akka-cluster-console-${VERSION}.jar