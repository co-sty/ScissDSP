/*
 *  Perceptive.java
 *  (ScissDSP)
 *
 *  Copyright (c) 2001-2010 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation; either
 *	version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	This software is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public
 *	License (gpl.txt) along with this software; if not, write to the Free Software
 *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 *
 *
 *  Changelog:
 *  	27-Jan-10	created from EisK Filter. Uses Java 1.5 now
 */

package de.sciss.dsp;

public class Perceptive
{
	private static final float[] dBAweight = {
		//		1 Hz		1.25		1.6			2			2.5			3.15
			0.0f,		1.000e-7f,	2.512e-7f,	6.310e-7f,	1.567e-6f,	3.890e-6f,
		//		4			5			6.3			8			10			12
			9.661e-6f,	2.371e-5f,	5.370e-5f,	1.365e-4f,	3.162e-4f,	7.161e-4f,
		//		16			20			25			31.5		40			50
			1.531e-3f,	3.126e-3f,	6.026e-3f,	1.096e-2f,	1.905e-2f,	3.090e-2f,
		//		63			80			100			125			160			200	
			4.955e-2f,	7.586e-2f,	1.109e-1f,	1.585e-1f,	2.163e-1f,	2.884e-1f,
		//		250			315			400			500			630			800
			3.715e-1f,	4.677e-1f,	5.754e-1f,	6.918e-1f,	8.035e-1f,	9.120e-1f,
		//		1k			1.25k		1.6k		2k			2.5k		3.15k
			1.0f,		1.072f,		1.122f,		1.148f,		1.161f,		1.148f,
		//		4k			5k		6.3k		8k			10k			12.5k
			1.122f,		1.059f,		9.886e-1f,	8.810e-1f,	7.499e-1f,	6.095e-1f,
		//		16k			20k			25k			31.5k
			4.677e-1f,	3.428e-1f,	2.399e-1f,	0.0f
		};
	
	/**
	 *	Fuellt Array mit dB(A)-Gewichtungs-Faktoren
	 *
	 *	@param	weights				Array der Groesse 'num', wird beschrieben
	 *	@param	freq				Center-Frequenzen in Hertz; Objekt darf mit 'weights'
	 *								identisch sein, d.h. die Frequenzen werden durch
	 *								die zugehoerigen Gewichtungen ueberschrieben!
	 *	@param	num					Zahl der Gewichte resp. Frequenzen
	 */
	public static void getDBAweights( float[] weights, float[] freq, int num )
	{
		float f, f2;
		int	i, j;

		for( i = 0; i < num; i++ ) {
			f = freq[i];
			if( f < 1.0f ) {
				weights[i]	= dBAweight[0];
			} else if( f > 31622.7f ) {
				weights[i]	= dBAweight[dBAweight.length-1];
			} else {
				f2	= (float) (10 * Math.log10( f ));
				j	= (int) f2;
				f2 %= 1.0f;
				weights[i]	= dBAweight[j] * (1.0f - f2) + dBAweight[j+1] * f2;	// ? XXX
			}
		}
	}

}
