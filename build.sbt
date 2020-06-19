name := "kopi-seller"

version := "0.1"

scalaVersion := "2.13.2"

organization := "com.kopiseller"

libraryDependencies ++= {
  val akkaVersion = "2.6.5"
  val akkaHttp = "10.1.12"
  Seq (
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttp,
    "com.typesafe.akka" %% "akka-http" % akkaHttp,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,

    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.1.2" % Test
  )
}