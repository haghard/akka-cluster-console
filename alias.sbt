
/*
export ENV=development
export CONFIG=./server/conf

export ENV=production
export CONFIG=./server/conf

*/

addCommandAlias(
  "console-0",
  "runMain console.Application " +
  "-DENV=development " +
  "-DCONFIG=./server/conf " +
  "-Dakka.remote.netty.tcp.port=2551 " +
  "-Dakka.http.port=9443 " +
  "-DHOSTNAME=192.168.10.98 " +
  "-Dakka.cluster.roles.0=console " +
  "-Dakka.cluster.seed-nodes.0=akka.tcp://cluster-console@192.168.10.98:2551 " +
  "-Dakka.cluster.seed-nodes.1=akka.tcp://cluster-console@192.168.10.98:2552 "
)

addCommandAlias(
  "console-1",
  "runMain console.Application " +
  "-DENV=development " +
  "-DCONFIG=./server/conf " +
  "-Dakka.remote.netty.tcp.port=2552 " +
  "-Dakka.http.port=9444 " +
  "-DHOSTNAME=192.168.10.98 " +
  "-Dakka.cluster.roles.0=console " +
  "-Dakka.cluster.seed-nodes.0=akka.tcp://linguistics@192.168.10.98:2551 " +
  "-Dakka.cluster.seed-nodes.1=akka.tcp://linguistics@192.168.10.98:2552 "
)