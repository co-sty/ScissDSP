package de.sciss.dsp

object FourierTest2 extends App with Runnable {
   run()

   def run(): Unit = {
      val rnd  = new util.Random( 0L )
      val in   = Array.fill( 64 )( rnd.nextFloat() * 2 - 1 )
//      val in   = Array.fill( 3 )( 1f ) ++ Array.fill( 64 - 24 )( 0f )
      val fft  = Fourier( 32 )
      val out  = in.clone()
      fft.complexForward( out )
//      Complex.rect2Polar( out, 0, out, 0, out.length )
//      val test = out.zipWithIndex.collect { case (f, i) if i % 2 == 0 => f }
//      println( test.mkString( "[ ", ", ", " ]" ))
      println( out.mkString( "[ ", ", ", " ]" ))
   }
}
