addCommandAlias(
  "a",
  "server/runMain console.Application " +
  "-DENV=development " +
  //"-DCONFIG=./server/conf " +
  "-DCONFIG=./conf " +
  "-Dakka.remote.artery.canonical.port=2550\n" +
  "-Dakka.remote.artery.canonical.hostname=127.0.0.1\n"+
  "-DHTTP_PORT=8080 " +
  "-DURL=http://127.0.0.1:8080/cluster/members"
)

//sudo ifconfig lo0 127.0.0.2 add
addCommandAlias(
  "b",
  "server/runMain console.Application " +
    "-DENV=development " +
    "-DCONFIG=./conf " +
    "-Dakka.remote.artery.canonical.port=2550\n" +
    "-Dakka.remote.artery.canonical.hostname=127.0.0.2\n"+
    "-DHTTP_PORT=8080 " +
    "-DURL=http://127.0.0.2:8080/cluster/members"
)

//sudo ifconfig lo0 127.0.0.3 add
addCommandAlias(
  "d",
  "server/runMain console.Application " +
    "-DENV=development " +
    "-DCONFIG=./conf " +
    "-Dakka.remote.artery.canonical.port=2550\n" +
    "-Dakka.remote.artery.canonical.hostname=127.0.0.3\n"+
    "-DHTTP_PORT=8080 " +
    "-DURL=http://127.0.0.3:8080/cluster/members"
)