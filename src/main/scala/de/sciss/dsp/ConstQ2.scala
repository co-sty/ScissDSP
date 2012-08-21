/*
 * ConstQ.scala
 * (ScissDSP)
 *
 * Copyright (c) 2001-2012 Hanns Holger Rutz. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *
 * For further information, please contact Hanns Holger Rutz at
 * contact@sciss.de
 */

package de.sciss.dsp

object ConstQ {
   sealed trait ConfigLike {
      def sampleRate: Double
      def minFreq: Float
      def maxFreq: Float
      def maxTimeRes: Float
      def maxFFTSize: Int
      def bandsPerOct: Int
      def threading: Threading
   }
   object Config {
      implicit def build( b: ConfigBuilder ) : Config = b.build
      def apply() : ConfigBuilder = new ConfigBuilderImpl
   }
   sealed trait Config extends ConfigLike
   object ConfigBuilder {
      def apply( config: Config ) : ConfigBuilder = {
         import config._
         val b = new ConfigBuilderImpl
         b.sampleRate   = sampleRate
         b.minFreq      = minFreq
         b.maxFreq      = maxFreq
         b.maxTimeRes   = maxTimeRes
         b.maxFFTSize   = maxFFTSize
         b.bandsPerOct  = bandsPerOct
         b.threading    = threading
         b
      }
   }
   sealed trait ConfigBuilder extends ConfigLike {
      def sampleRate: Double
      def sampleRate_=( value: Double ) : Unit
      def minFreq: Float
      def minFreq_=( value: Float ) : Unit
      def maxFreq: Float
      def maxFreq_=( value: Float ) : Unit
      def maxTimeRes: Float
      def maxTimeRes_=( value: Float ) : Unit
      def maxFFTSize: Int
      def maxFFTSize_=( value: Int ) : Unit
      def bandsPerOct: Int
      def bandsPerOct_=( value: Int ) : Unit
      def threading: Threading
      def threading_=( value: Threading ) : Unit

      def build : Config
   }

   private final class ConfigBuilderImpl extends ConfigBuilder {
      override def toString = "ConstQ.ConfigBuilder@" + hashCode.toHexString

   	// rather moderate defaults with 55 Hz, 8ms spacing, 4096 FFT...
      var sampleRate    = 44100.0
      var minFreq       = 55f
     	var maxFreq       = 20000f
     	var maxTimeRes    = 8f
     	var bandsPerOct   = 24
     	var maxFFTSize    = 4096
      var threading: Threading = Threading.Multi

      def build : Config = ConfigImpl( sampleRate, minFreq, maxFreq, maxTimeRes, maxFFTSize, bandsPerOct, threading )
   }

   private final case class ConfigImpl( sampleRate: Double, minFreq: Float, maxFreq: Float, maxTimeRes: Float,
                                        maxFFTSize: Int, bandsPerOct: Int, threading: Threading )
   extends Config {
      override def toString = "ConstQ.Config@" + hashCode.toHexString
   }

   /**
    * Calculates the number of kernels resulting from a given setting
    */
   def getNumKernels( bandsPerOct: Int, maxFreq: Float, minFreq: Float ) : Int =
      math.ceil( bandsPerOct * MathUtil.log2( maxFreq / minFreq )).toInt

   def apply( config: Config = Config().build ) : ConstQ = createInstance( config )

   private final class Impl( val config: Config, kernels: Array[ Kernel ], val fft: Fourier, val fftBuffer: Array[ Float ])
   extends ConstQ {
//      private Kernel[]	kernels;
//     	private int			numKernels;
//     	private int			fftSize;
//     	private float[]		fftBuffer;
//         private Fourier     fft;

      val numKernels = kernels.length
      val fftSize    = fft.size

      def getFrequency( kernel: Int ) : Float = kernels( kernel ).freq

      def transform( input: Array[ Float ], inOff: Int, inLen: Int, out0: Array[ Float ], outOff: Int ) : Array[ Float ] = {
         val output = if( out0 == null ) new Array[ Float ]( numKernels ) else out0

   //		gain *= TENBYLOG10;
   		val off = fftSize >> 1
   		val num = math.min( fftSize - off, inLen )

   //System.out.println( "inOff " + inOff + "; num  " + num + " ; inOff + num " + (inOff + num) + "; input.length " + input.length + "; fftBuffer.length " + fftBuffer.length );

   		System.arraycopy( input, inOff, fftBuffer, off, num )
         var i = off + num; while( i < fftSize ) {
   			fftBuffer( i ) = 0f
   		i += 1 }
   		val num2 = math.min( fftSize - num, inLen - num )
   		System.arraycopy( input, inOff + num, fftBuffer, 0, num2 )
   		i = num2; while( i < off ) {
   			fftBuffer( i ) = 0f
   		i += 1 }

   		// XXX evtl., wenn inpWin weggelassen werden kann,
   		// optimierte overlap-add fft
//   		Fourier.realTransform( fftBuffer, fftSize, Fourier.FORWARD );
         fft.realForward( fftBuffer )

   		convolve( output, outOff )
   	}

     	def convolve( out0: Array[ Float ], outOff0: Int ) : Array[ Float ] = {
         val output = if( out0 == null ) new Array[ Float ]( numKernels ) else out0

     		var k = 0; var outOff = outOff0; while( k < numKernels ) {
            val kern = kernels( k )
     			val data = kern.data
     			var f1	= 0f
            var f2   = 0f
     			var i = kern.offset; var j = 0; while( j < data.length ) {
     				// complex mult: a * b =
     				// (re(a)re(b)-im(a)im(b))+i(re(a)im(b)+im(a)re(b))
     				// ; since we left out the conjugation of the kernel(!!)
     				// this becomes (assume a is input and b is kernel):
     				// (re(a)re(b)+im(a)im(b))+i(im(a)re(b)-re(a)im(b))
     				// ; in fact this conjugation is unimportant for the
     				// calculation of the magnitudes...
               val re1 = fftBuffer( i )
               val im1 = fftBuffer( i+1 )
               val re2 = data( j )
               val im2 = data( j + 1 )
     				f1 += re1 * re2 - im1 * im2
     				f2 += re1 * im2 + im1 * re2
            i += 2; j += 2 }
     //			cBuf[ k ] = ;  // squared magnitude
     //			f1 = (float) ((Math.log( f1 * f1 + f2 * f2 ) + LNKORR_ADD) * gain);
     //f1 = Math.max( -160, f1 + 90 );

     			// since we use constQ to decimate spectra, we actually
     			// are going to store a "mean square" of the amplitudes
     			// so at the end of the decimation we'll have one squareroot,
     			// or even easier, when calculating the logarithm, the
     			// multiplicator of the log just has to be divided by two.
     			// hence we don't calc abs( f ) but abs( f )^2 !

     			output( outOff ) = (f1 * f1 + f2 * f2)

         k += 1; outOff += 1 }

     		output
     	}
   }

	private def createInstance( config0: Config ) : Impl = {
      val fs = config0.sampleRate
      val config = if( config0.maxFreq <= fs/2 ) config0 else {
         val c = ConfigBuilder( config0 )
         c.maxFreq = (fs/2).toFloat
         c.build
      }

		val q			   = (1 / (math.pow( 2, 1.0/config.bandsPerOct ) - 1)).toFloat
		val numKernels	= getNumKernels( config.bandsPerOct, config.maxFreq, config.minFreq )
		val kernels		= new Array[ Kernel ]( numKernels )
//		cqKernels	= new float[ cqKernelNum ][];
//		cqKernelOffs= new int[ cqKernelNum ];
		val maxKernLen	= q * config.sampleRate / config.minFreq

//		System.out.println( "ceil " + ((int) Math.ceil( maxKernLen )) + "; nextPower " + (MathUtil.nextPowerOfTwo( (int) Math.ceil( maxKernLen )))) ;

		val fftSize		= math.min( config.maxFFTSize, MathUtil.nextPowerOfTwo( math.ceil( maxKernLen ).toInt ))
//		LNKORR_ADD	= -2 * Math.log( fftSize );
		val fftSizeC	= fftSize << 1
		val fftBuf		= new Array[ Float ]( fftSizeC )
      val fft        = Fourier( fftSize, config.threading )

//		thresh		= 0.0054f / fftLen; // for Hamming window
		// weird observation : lowering the threshold will _increase_ the
		// spectral noise, not improve analysis! so the truncating of the
		// kernel is essential for a clean analysis (why??). the 0.0054
		// seems to be a good choice, so don't touch it.
//		threshSqr	= 2.916e-05f / (fftSize * fftSize); // for Hamming window (squared!)
//		threshSqr	= 2.916e-05f / fftSize; // for Hamming window (squared!)
		// (0.0054 * 3).squared
		val threshSqr	= 2.6244e-4f / (fftSize * fftSize)  // for Hamming window (squared!)
		// tempKernel= zeros(fftLen, 1);
//		sparKernel= [];

//		System.out.println( "cqKernelNum = " + cqKernelNum + "; maxKernLen = " + maxKernLen + "; fftSize = " + fftSize + "; threshSqr " + threshSqr );

      var k = 0; while( k < numKernels ) {
			val theorKernLen  = maxKernLen * math.pow( 2, (-k).toDouble / config.bandsPerOct ) // toFloat
         val kernelLen	   = math.min( fftSize, math.ceil( theorKernLen ).toInt )
         val kernelLenE	   = kernelLen & ~1
         val win			   = Window.Hamming.create( kernelLen )

         val centerFreq	   = config.minFreq * math.pow( 2, k.toDouble / config.bandsPerOct )
         val centerFreqN	= centerFreq * -MathUtil.Pi2 / fs

//			weight		= 1.0f / (kernelLen * fftSize);
			// this is a good approximation in the kernel len truncation case
			// (tested with pink noise, where it will appear pretty much
			// with the same brightness over all frequencies)
         val weight        = 6 / ((theorKernLen + kernelLen) * fftSize)
//			weight = 2 / ((theorKernLen + kernelLen) * Math.sqrt( fftSize ));

			var m = kernelLenE; val n = fftSizeC - kernelLenE; while( m < n ) {
				fftBuf( m ) = 0f
			m += 1 }

			// note that we calculate the complex conjugation of
			// the temporalKernal and reverse its time, so the resulting
			// FFT can be immediately used for the convolution and does not
			// need to be conjugated; this is due to the Fourier property
			// h*(-x) <-> H*(f). time reversal is accomplished by
			// having iteration variable j run....

			// XXX NO! we don't take the reversed conjugate since
			// there seems to be a bug with Fourier.complexTransform
			// that already computes the conjugate spectrum (why??)
//			for( int i = kernelLen - 1, j = fftSizeC - kernelLenE; i >= 0; i-- ) { ... }
//			for( int i = 0, j = 0; /* fftSizeC - kernelLenE; */ i < kernelLen; i++ ) { ... }
			var i = 0; var j = fftSizeC - kernelLenE; while( i < kernelLen ) {
				// complex exponential of a purely imaginary number
				// is cos( imag( n )) + i sin( imag( n ))
				val d1   = centerFreqN * i
//				d1			= centerFreqN * (i - kernelLenE);
				val cos  = math.cos( d1 )
				val sin  = math.sin( d1 ) // Math.sqrt( 1 - cos*cos );
				val d2   = win( i ) * weight
				fftBuf( j ) = (d2 * cos).toFloat; j += 1
//				fftBuf[ j++ ] = -f1 * sin;  // conj!
				fftBuf( j ) = (d2 * sin).toFloat; j += 1  // NORM!
				if( j == fftSizeC ) j = 0
			i += 1 }

			// XXX to be honest, i don't get the point
			// of calculating the fft here, since we
			// have an analytic description of the kernel
			// function, it should be possible to calculate
			// the spectral coefficients directly
			// (the fft of a hamming is a gaussian,
			// isn't it?)

//			Fourier.complexTransform( fftBuf, fftSize, Fourier.FORWARD )
         fft.complexForward( fftBuf )

			// with a "high" threshold like 0.0054, the
			// point it _not_ to create a sparse matrix by
			// gating the values. in fact we can locate
			// the kernal spectrally, so all we need to do
			// is to find the lower and upper frequency
			// of the transformed kernel! that makes things
			// a lot easier anyway since we don't need
			// to employ a special sparse matrix library.
         val specStart = {
            var i = 0; var break = false; while( !break && i <= fftSize ) {
               val f1 = fftBuf( i )
               val f2 = fftBuf( i+1 )
               if( (f1 * f1 + f2 * f2) > threshSqr ) break = true else i += 2
            }
            i
         }
			// final matrix product:
			// input chunk (fft'ed) is a row vector with n = fftSize
			// kernel is a matrix mxn with m = fftSize, n = numKernels
			// result is a row vector with = numKernels
			// ; note that since the input is real and hence we
			// calculate only the positive frequencies (up to pi),
			// we might need to mirror the input spectrum to the
			// negative frequencies. however it can be observed that
			// for practically all kernels their frequency bounds
			// lie in the positive side of the spectrum (only the
			// high frequencies near nyquest blur accross the pi boundary,
			// and we will cut the overlap off by limiting specStop
			// to fftSize instead of fftSize<<1 ...).

			val specStop = {
            var i = specStart; var break = false; while( !break && i <= fftSize ) {
               val f1 = fftBuf( i )
               val f2 = fftBuf( i+1 )
               if( (f1 * f1 + f2 * f2) <= threshSqr ) break = true else i += 2
            }
            i
			}

//System.out.println( "Kernel k : specStart " + specStart + "; specStop " + specStop + "; centerFreq " + centerFreq );
			kernels( k ) = new Kernel( specStart, new Array[ Float ]( specStop - specStart ), centerFreq.toFloat )
			System.arraycopy( fftBuf, specStart, kernels( k ).data, 0, specStop - specStart )
		k += 1 }

      new Impl( config, kernels, fft, fftBuf )
	}

	private final class Kernel( val offset: Int, val data: Array[ Float ], val freq: Float )
}
trait ConstQ {
   def config: ConstQ.Config
   def numKernels: Int
   def fftSize: Int
   def fftBuffer: Array[ Float ]
   def getFrequency( kernel: Int ) : Float

   def transform( input: Array[ Float ], inOff: Int, inLen: Int, output: Array[ Float ], outOff: Int ) : Array[ Float ]

   /**
  	 * 	Assumes that the input was already successfully transformed
  	 * 	into the Fourier domain (namely into fftBuf as returned by
  	 * 	getFFTBuffer()). From this FFT, the method calculates the
  	 * 	convolutions with the filter kernels, returning the magnitudes
  	 * 	of the filter outputs.
  	 */
  	def convolve( output: Array[ Float ], outOff: Int ) : Array[ Float ]
}
