## Akka cluster console

Collects data from an existing akka cluster and draw it.    

###  How to build
      
    `sbt -Denv=development docker && docker push haghard/cluster-console:0.1.0`

### How to run locally

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.11.jdk/Contents/Home

```

sbt a
sbt b
sbt c

http://127.0.0.1:8080/console

```

### How to run

* `docker run --net=host -d -p 8081:8081 -e HOSTNAME=192.168.77.10 -e HTTP_PORT=8081 -e URL=... -e PASSWORD=... -m 250MB haghard/cluster-console:0.1.0`
*  Open http://192.168.77.10:8080/console
*  Click on "Cluster" tab

Where:

    *  URL points out on an existing akka cluster that runs `akka-management-cluster-http` module. For example https://.../cluster/members
    *  PASSWORD passwords that protects URL.


### Live demo link
https://codelfsolutions.com/console  
	  

### Links
    
https://japgolly.github.io/scalajs-react/#examples/timer

http://blockbuilder.org/search

http://bl.ocks.org/sathomas/11550728

https://github.com/spaced/scala-js-d3/issues/6

https://runkit.com/npm/d3-force
    
http://blockbuilder.org/mattykuch/40ba19de703632ea2afbbc5156b9471f
    
http://dimplejs.org
        
http://bl.ocks.org/enjalot/raw/211bd42857358a60a936/

https://bost.ocks.org/mike/example/

https://blog.csanchez.org/2017/05/31/running-a-jvm-in-a-container-without-getting-killed/

https://github.com/japgolly/scalajs-react/blob/0e984d1fd57fb35106bc8c3ec5d2566800a7e9a8/gh-pages/src/main/scala/ghpages/ExtrasExamples.scala