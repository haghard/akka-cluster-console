addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.3.2")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.1.0")


//https://repo1.maven.org/maven2/org/scala-js/scalajs-scalalib_2.13/2.13.18+1.20.2/
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.20.2")

addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.2")

addSbtPlugin("com.eed3si9n"   % "sbt-assembly"        % "2.3.1")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker"   % "1.5.0")

addSbtPlugin("com.eed3si9n"   % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("com.scalapenos" % "sbt-prompt"    % "1.0.2")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.6")

addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "4.15.2" cross CrossVersion.full)