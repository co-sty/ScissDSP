/*
 * Util.scala
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

/**
 *  This is a helper object containing utility functions
 *  for common math operations and constants
 */
object Util {
	/**
	 *  2 * PI (Outline of the unit circle)
	 */
	val Pi2  = math.Pi * 2
	/**
	 *  logarithmus naturalis of 2
	 */
	val Ln2  = math.log( 2 )
	/**
	 *  logarithmus naturalis of 10
	 */
	val Ln10 = math.log( 10 )

	/**
	 *  Decibel-to-Linear conversion.
	 *
	 *  @param  dB  volume in decibels
	 *  @return volume linear, such that
	 *			dBToLinear( -6 ) returns c. 0.5
	 */
	def dbamp( dB: Double ) : Double =
      math.exp( dB / 20 * Ln10 )

	/**
	 *  Linear-to-Decibel conversion
	 *
	 *  @param  linear  volume linear
	 *  @return volume in decibals, such that
	 *			linearToDB( 2.0 ) returns c. +6
	 */
	def ampdb( linear: Double ) : Double =
		math.log10( linear ) * 20

	/**
	 *  Calculate the logarithm with base 2.
	 *
	 *  @param  value the input value
	 *  @return the log2 of the value
	 */
	def log2( value: Double ) : Double =
	   math.log( value ) / Ln2

	/**
	 *  Calculates an integer that is a power
	 *	of two and is equal or greater than a given integer
	 *
	 *	@param	value the minimum value to return
	 *	@return		an integer 2^n which is equal or greater than x
	 */
	def nextPowerOfTwo( value: Int ) : Int =  {
		var y = 1; while( y < value ) y <<= 1
		y
	}

   /**
  	 *	Energie berechnen (sum(x^2))
  	 */
  	def calcEnergy( a: Array[ Float ], off: Int, length: Int ) : Double = {
  		var energy  = 0.0
  		val stop    = off + length

  		var i = off; while( i < stop ) {
           val f = a( i )
           energy += f * f
  		i += 1 }

  		energy
  	}

//   def printPrint( a: Array[ Float ], off: Int, length: Int, columns: Int = 8 ) : String =
}