package de.sciss.dsp

object ConstQTest extends App with Runnable {
   run()

   def run() {
      val q    = ConstQ()
      val in   = Array.fill( 1024 )( util.Random.nextFloat() * 2 - 1 )
      val out  = q.transform( in, in.length, null )
      println( out.take( 10 ).toIndexedSeq )
   }
}
