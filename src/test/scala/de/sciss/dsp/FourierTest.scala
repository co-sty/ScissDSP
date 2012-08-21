//package de.sciss.dsp
//
//object FourierTest extends App {
//   test()
//
//   def calcError( a: Array[ Float ], b: Array[ Float ]) : (Float, Float) = {
//      var mean = 0.0
//      val sz = a.length
//      var max = 0f
//      require( b.length == sz )
//      var i = 0; while( i < sz ) {
//         val diff = math.abs( a( i ) - b( i ))
//         max = math.max( max, diff )
//         mean += diff * diff
//      i += 1 }
//      math.sqrt( mean / sz ).toFloat -> max
//   }
//
//   def prettyPrint( a: Array[ Float ]) {
//      var i = 0
//      val sz = a.length
//      println( "[" )
//      while( i < sz ) {
//         print( a( i ).toString )
//         i += 1
//         if( i < sz ) {
//            print( if( (i % 16) == 0 ) ",\n" else ", " )
//         }
//      }
//      println( if( (i % 16) == 0 ) " ]" else "\n]" )
//   }
//
//   def invertPhases( a: Array[ Float ]) {
//      val sz = a.length
//      var i = 1; while( i < sz ) {
//         a( i ) *= -1
//      i += 2 }
//   }
//
//   def test() {
//      val rnd  = new util.Random( 0L )
//      val n    = 64
//
//      val data1   = Array.fill( n + 2 )( rnd.nextFloat() )
//      data1( n )  = 0f
//      data1( n + 1 ) = 0f
//      val data2   = data1.clone()
//      val copy    = data1.clone()
//
//      FourierOld.realTransform( data1, n, FourierOld.FORWARD )
//      invertPhases( data1 )
//      val f = Fourier( n, Threading.Single )
//      f.realForward( data2 )
//
//      val err1 = calcError( data1, data2 )
//      println( "Error between two algorithms: " + err1 )
//
//      invertPhases( data1 )
//      FourierOld.realTransform( data1, n, FourierOld.INVERSE )
//      f.realInverse( data2 )
//
//      val err2 = calcError( data1, copy )
//      println( "Fwd/Inv error for old algorithm: " + err2 )
//
//      val err3 = calcError( data2, copy )
//      println( "Fwd/Inv error for new algorithm: " + err3 )
//
////      println( "\ninput:" )
////      prettyPrint( copy )
////      println( "\nold algo:" )
////      prettyPrint( data1 )
////      println( "\nnew algo:" )
////      prettyPrint( data2 )
//   }
//}
