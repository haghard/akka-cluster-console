
Docker build

    sbt docker
    sbt -DENV="development" && docker
    
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
	  -file haghard.crt \
	  -keypass ... \
	  -storepass ... \
	  -keystore server/src/main/resources/haghard.jks \
	  -rfc`
	  
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
