name := "dining"
version := "2.4-RC3"
scalaVersion := "2.11.7"
EclipseKeys.withSource := true
enablePlugins(JavaAppPackaging)
mainClass in (Compile, packageBin) := Some("fsm.Dining")

javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint")
javacOptions in doc ++= Seq("-encoding", "UTF-8", "-source", "1.8")
testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.4.1",
  "org.apache.logging.log4j" % "log4j-api" % "2.4.1",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.4.1",
  "com.lmax" % "disruptor" % "3.3.2",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.0" % "test",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test")