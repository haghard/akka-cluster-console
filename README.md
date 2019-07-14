Docker build
    
    sbt -Denv=development docker
    sbt -Denv=development docker && docker push haghard/cluster-console:0.1.0

Initial ubuntu server setup in cloud

    https://www.youtube.com/watch?v=EuIYabZS3ow

Initial sbt on ubuntu 
    
    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
    sudo apt-get update
    sudo apt-get install sbt

Initial java on ubuntu

    https://www.digitalocean.com/community/tutorials/java-ubuntu-apt-get-ru
    sudo apt-get install default-jre
    sudo apt-get install default-jdk

How to run on YC 
  
  `docker run --net=host -d -p 8081:8081 -e HOSTNAME=192.168.0.13 -e HTTP_PORT=8081 -e PASSWORD=... -m 250MB haghard/cluster-console:0.1.0`
  `docker run --net=host -d -p 8081:8081 -e HOSTNAME=192.168.0.21 -e HTTP_PORT=8081 -e PASSWORD=... -m 250MB haghard/cluster-console:0.1.0`
 
  https://codelfsolutions.com/console  
	  
Links
    
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