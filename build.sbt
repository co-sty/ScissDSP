name := "ScissDSP"

version := "1.0.0-SNAPSHOT"

organization := "de.sciss"

scalaVersion := "2.9.2"

libraryDependencies += "net.sourceforge.jtransforms" % "jtransforms" % "2.4.0"

retrieveManaged := true

scalacOptions ++= Seq( "-deprecation", "-unchecked" )
