## ScissDSP

### statement

ScissDSP is a collection of Digital Signal Processing (DSP) components for the Scala programming language. It is (C)opyright 2001–2012 by Hanns Holger Rutz. All rights reserved.

ScissDSP is released under the [GNU Lesser General Public License](http://github.com/Sciss/ScissDSP/blob/master/licenses/ScissDSP-License.txt) and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`.

For project status, API and current version, visit [github.com/Sciss/ScissDSP](http://github.com/Sciss/ScissDSP).

### building

ScissDSP compiles with sbt 0.12.0 against Scala 2.9.2. It depends on [JTransforms](https://sites.google.com/site/piotrwendykier/software/jtransforms) for the FFT now, implying Java 1.6 SE. JTransforms is also licensed under the GNU LGPL.

### linking

If you develop a Scala project using sbt and want it to depend on ScissDSP, you can now add the following line to your sbt build file:

    libraryDependencies += "de.sciss" %% "scissdsp" % "1.1.+"

### notes

As of v1.0.0, the FFT algorithm has changed. It seems that in the previous version the phases of the real transform were inverted. The new algorithm seems consistent with other FFT algorithms tested. The floating point rounding noise of the new algorithm has not changed (in fact is a tiny bit smaller).

For an example of the Constant-Q transform, see the [SonogramOverview project](http://github.com/Sciss/SonogramOverview).
