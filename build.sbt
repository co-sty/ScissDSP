name := "ScissDSP"

version := "1.0.0"

organization := "de.sciss"

description := "Collection of DSP algorithms and components for Scala"

homepage := Some( url( "https://github.com/Sciss/ScissDSP" ))

licenses := Seq( "LGPL v2+" -> url( "http://www.gnu.org/licenses/lgpl.txt" ))

scalaVersion := "2.9.2"

crossScalaVersions := Seq( "2.10.0-M7", "2.9.2" )

libraryDependencies += "net.sourceforge.jtransforms" % "jtransforms" % "2.4.0"

retrieveManaged := true

scalacOptions ++= Seq( "-deprecation", "-unchecked" )

initialCommands in console := """import de.sciss.dsp._
def randomSignal( size: Int = 128 ) = Array.fill( size )( util.Random.nextFloat() * 2 - 1 )"""

// ---- build info ----

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq( name, organization, version, scalaVersion, description,
   BuildInfoKey.map( homepage ) { case (k, opt) => k -> opt.get },
   BuildInfoKey.map( licenses ) { case (_, Seq( (lic, _) )) => "license" -> lic }
)

buildInfoPackage := "de.sciss.dsp"

// ---- publishing ----

publishMavenStyle := true

publishTo <<= version { (v: String) =>
   Some( if( v.endsWith( "-SNAPSHOT" ))
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
   else
      "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
   )
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra :=
<scm>
  <url>git@github.com:Sciss/ScissDSP.git</url>
  <connection>scm:git:git@github.com:Sciss/ScissDSP.git</connection>
</scm>
<developers>
   <developer>
      <id>sciss</id>
      <name>Hanns Holger Rutz</name>
      <url>http://www.sciss.de</url>
   </developer>
</developers>

// ---- ls.implicit.ly ----

seq( lsSettings :_* )

(LsKeys.tags in LsKeys.lsync) := Seq( "audio", "spectrum", "dsp", "signal" )

(LsKeys.ghUser in LsKeys.lsync) := Some( "Sciss" )

(LsKeys.ghRepo in LsKeys.lsync) := Some( "ScissDSP" )

// bug in ls -- doesn't find the licenses from global scope
(licenses in LsKeys.lsync) := Seq( "LGPL v2+" -> url( "http://www.gnu.org/licenses/lgpl.txt" ))
