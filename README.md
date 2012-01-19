## ScissDSP

### statement

ScissDSP is a collection of Digital Signal Processing (DSP) components for the Java language. Compiles against Java SE 1.5. It is (C)opyright 2001–2012 by Hanns Holger Rutz. All rights reserved.

ScissDSP is released under the [GNU General Public License](http://github.com/Sciss/ScissDSP/blob/master/licenses/ScissDSP-License.txt) and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`.

For project status, API and current version, visit [github.com/Sciss/ScissDSP](http://github.com/Sciss/ScissDSP).

### building

ScissDSP will eventually be translated into Scala. As an anticipating step, it now builds with the Simple Build Tool (sbt 0.11). Simply run `sbt package` to compile and package into a `.jar` file.

You can still check out the older 0.10 version which builds with ant.

### linking

If you develop a Scala project using sbt and want it to depend on ScissDSP, you can now add the following line to your sbt build file:

    libraryDependencies += "de.sciss" % "scissdsp" % "0.11" from "https://github.com/downloads/Sciss/ScissDSP/scissdsp-0.11.jar"
