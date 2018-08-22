
/*
export ENV=development
export CONFIG=./server/conf

export ENV=production
export CONFIG=./server/conf

*/

addCommandAlias(
  "console-0",
  "server/runMain console.Application " +
  "-DENV=development " +
  "-DCONFIG=./conf " +
  "-Dakka.remote.netty.tcp.port=2551 " +
  "-Dakka.http.port=8080 " +
  "-DHOSTNAME=192.168.77.10 " +
  "-Dakka.cluster.roles.0=console " +
  "-Dakka.cluster.seed-nodes.0=akka.tcp://cluster-console@192.168.77.10:2551 " +
  "-Dakka.cluster.seed-nodes.1=akka.tcp://cluster-console@192.168.77.11:2552 "
)