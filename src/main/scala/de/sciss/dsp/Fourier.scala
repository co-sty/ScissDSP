/*
 * Fourier.scala
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

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D

object Fourier {
   def apply( size: Int, threading: Threading = Threading.Multi ) : Fourier = new Impl( size, threading )

   sealed trait Direction
   case object Forward extends Direction
   case object Inverse extends Direction

   private final class Impl( val size: Int, val threading: Threading ) extends Fourier {
      private val fft = new FloatFFT_1D( size )

      override def toString = "Fourier(size = " + size + ")@" + hashCode.toHexString

      def complexTransform( a: Array[ Float ], dir: Direction ) {
         if( dir eq Forward ) {
            complexForward( a )
         } else {
            complexInverse( a )
         }
      }

      def complexForward( a: Array[ Float ]) {
         threading.setJTransforms() // not nice... would be better if this was an option in FloatFFT_1D directly...
         fft.complexForward( a )
      }

      def complexInverse( a: Array[ Float ]) {
         threading.setJTransforms()
         fft.complexInverse( a, true )
      }

      def realTransform( a: Array[ Float ], dir: Direction ) {
         if( dir eq Forward ) {
            realForward( a )
         } else {
            realInverse( a )
         }
      }

      def realForward( a: Array[ Float ]) {
         threading.setJTransforms()
         fft.realForward( a )
         a( size )      = a( 1 )
         a( 1 )         = 0f
         a( size + 1 )  = 0f
//         val h1Re       = a( 0 )
//         val h1Im       = a( 1 )
//         a( 0 )         = h1Re + h1Im				// Squeeze the first and last data together
//         a( size )      = h1Re - h1Im				//   to get them all within the original array.
      }

      def realInverse( a: Array[ Float ]) {
//         val h1Re       = a( 0 )
//         val hhRe       = a( size )
//         a( 0 )         = 0.5f * (h1Re + hhRe)
//         a( 1 )         = 0.5f * (h1Re - hhRe)
         a( 1 )         = a( size )
         a( size )      = 0f
         a( size + 1 )  = 0f
         threading.setJTransforms()
         fft.realInverse( a, true )
      }
   }
}
trait Fourier {
   import Fourier._

   def size: Int
   def threading: Threading

   /**
  	 *	One-dimensional discrete complex fourier transform.
  	 *	Replaces `a[ 0...2*len ]` by its discrete Fourier transform.
  	 *	In the inverse operation, a gain normalisierung by 1/len is
  	 * applied automatically.
    *
  	 *	@param   a     complex array with real part in a[ 0, 2, 4, ... 2*len - 2 ],
  	 *                imaginary part in a[ 1, 3, ... 2 * len -1 ]
  	 * @param   dir   either `Fourier.Inverse` or `Fourier.Forward`
  	 */
   def complexTransform( a: Array[ Float ], dir: Direction ) : Unit
   def complexForward( a: Array[ Float ]) : Unit
   def complexInverse( a: Array[ Float ]) : Unit

   /**
  	 *	One-dimensional discrete real fourier transform.
  	 *	Replaces `a[ 0...len ]` by its discrete Fourier transform
  	 *	(positive freq. half of the complex spectrum).
  	 *
  	 * __Warning__: a actually has len + 2 elements! in forward operation these
  	 *	last two elements must be zero.
    *
  	 *	@param   a     real array; output is complex with real part in a[ 0, 2, 4, ... len ],
  	 *                imaginary part in a[ 1, 3, ... len + 1 ].
  	 * @param   dir   use <code>INVERSE</code> or <code>FORWARD</code>
  	 */
   def realTransform( a: Array[ Float ], dir: Direction ) : Unit
   def realForward( a: Array[ Float ]) : Unit
   def realInverse( a: Array[ Float ]) : Unit
}