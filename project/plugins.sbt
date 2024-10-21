addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.1.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs"     % "1.17.0")

addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.2")

addSbtPlugin("com.eed3si9n"   % "sbt-assembly"        % "2.1.5")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker"   % "1.5.0")

addSbtPlugin("com.eed3si9n"   % "sbt-buildinfo" % "0.12.0")
addSbtPlugin("com.scalapenos" % "sbt-prompt"    % "1.0.2")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.13.0")

addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "4.10.2" cross CrossVersion.full)