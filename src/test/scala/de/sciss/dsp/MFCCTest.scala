package de.sciss.dsp

import de.sciss.synth.io.AudioFile

object MFCCTest extends App {
  val c         = MFCC.Config()
  val af        = AudioFile.openRead("/home/hhrutz/Documents/devel/MutagenTx/audio_work/mfcc_input.aif")
  c.sampleRate  = af.sampleRate
  c.fftSize     = 1024

  val t         = MFCC(c)
  val buf       = af.buffer(1024)
  af.read(buf)
  af.close()
  val res       = t.process(buf(0))
  println(res.mkString("[", ", ", "]"))


}
