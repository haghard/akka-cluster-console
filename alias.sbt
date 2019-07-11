
/*
export ENV=development
export CONFIG=./server/conf

export ENV=production
export CONFIG=./server/conf

*/

addCommandAlias(
  "cons",
  "server/runMain console.Application " +
  "-DENV=development " +
  "-DCONFIG=./conf " +
  "-Dakka.http.port=8080 " +
  "-DHOSTNAME=192.168.77.10 " +
  "-DPASSWORD=..."
)