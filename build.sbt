libraryDependencies ++= List(
  "org.slf4j" % "slf4j-nop" % "2.0.13",
  "com.newrelic.agent.java" %% "newrelic-scala-api" % "8.14.0",
)

scalaVersion := "2.13.14"
scalacOptions += "-deprecation"
run / fork := true

// Comment out the line below to run without the New Relic agent
run / javaOptions ++= Seq(
  "-javaagent:opt/newrelic/newrelic-agent-8.14.0.jar",
)
