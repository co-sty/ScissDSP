package de.sciss.dsp

import de.sciss.serial.{DataInput, DataOutput}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class SerializationSpec extends FlatSpec with ShouldMatchers {
  "ConstQ.Config" should "serialize forth and back" in {
    val cfg     = ConstQ.Config()
    cfg.sampleRate  = 96000.0
    cfg.minFreq     = 99.9f
    cfg.maxFreq     = 6666.6f
    cfg.maxTimeRes  = 8.8f
    cfg.bandsPerOct = 123
    cfg.maxFFTSize  = 8192
    cfg.threading   = Threading.Custom(3)

    val out         = DataOutput()
    ConstQ.Config.Serializer.write(cfg, out)
    val in          = DataInput(out.toByteArray)
    val houdini     = ConstQ.Config.Serializer.read(in)

    assert(cfg.build === houdini)
  }
}