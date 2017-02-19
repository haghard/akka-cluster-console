
Docker build

    sbt docker
    sbt -DENV="development" && docker

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
    
    
Generating self-signed certificates

    http://typesafehub.github.io/ssl-config/CertificateGeneration.html

The first step is to create a certificate authority that will sign the haghard.com certificate. The root CA certificate has a couple of additional attributes (ca:true, keyCertSign) that mark it explicitly as a CA certificate, and will be kept in a trust store.        
    
    `keytool -genkeypair -v \
        -alias haghard.com \
        -dname "CN=haghard.com, O=Haghard ent, L=Erlangen, ST=Erlangen, C=En" \
        -keystore server/src/main/resources/haghard.jks  \
        -keypass ...  \
        -storepass ... \
        -keyalg RSA \
        -keysize 4096 \
        -ext KeyUsage:critical="keyCertSign" \
        -ext BasicConstraints:critical="ca:true" \
        -validity 665`

Export the haghard.com public certificate as haghard.crt so that it can be used in trust stores.

	`keytool -export -v \
         -alias haghard.com \
         -file ./haghard.crt \
         -keypass ... \
         -storepass ... \
         -keystore server/src/main/resources/haghard.jks \
         -rfc`	  
	  
How to run
  `docker run --net=host -it -p 2551:2551 -e HOSTNAME=80.93.177.136 -e AKKA_PORT=2551 -e HTTP_PORT=9443 -e JMX_PORT=1089 -e TZ="Europe/Moscow" haghard/cluster-console:0.0.1`
	  
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
