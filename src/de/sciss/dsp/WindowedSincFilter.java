/*
 *  WindowedSincFilter.java
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
 *  	27-Jan-10	created from EisK Filter
 */

package de.sciss.dsp;

/**
 *	@version	0.72, 27-Jan-10
 */
public class WindowedSincFilter
{
// -------- public Variablen --------

	public static final int	FLTSMPPERCROSSING	= 256;

	public static final int WIN_HAMMING			= 0;
	public static final int WIN_BLACKMAN		= 1;
	public static final int WIN_KAISER4			= 2;
	public static final int WIN_KAISER5			= 3;
	public static final int WIN_KAISER6			= 4;
	public static final int WIN_KAISER8			= 5;
	public static final int WIN_RECT			= 6;
	public static final int WIN_HANNING			= 7;
	public static final int WIN_TRI				= 8;
	
	public static final int WIN_MAX				= 8;

// -------- private Variablen --------

	private static final String[] windowNames = {
		"Hamming", "Blackman", "Kaiser §=4", "Kaiser §=5", "Kaiser §=6", "Kaiser §=8", "Rectangle",
		"von Hann", "Triangle"
	};
		
// -------- public Methoden --------

	/**
	 *	@param	impResp				Ziel-Array der Groesse 'halfWinSize' fuer Impulsantwort
	 *	@param	freq				Grenzfrequenz
	 *	@param	halfWinSize			Groesse des Kaiser-Fensters geteilt durch zwei
	 *	@param	kaiserBeta			Parameter fuer Kaiser-Fenster
	 *	@param	fltSmpPerCrossing	Zahl der Koeffizienten pro Periode
	 */
	public static void createLPF( float impResp[], float freq,
								  int halfWinSize, float kaiserBeta, int fltSmpPerCrossing )
	{
		double	dNum		= fltSmpPerCrossing;
		double	dBeta		= kaiserBeta;
		double	d;
		double	smpRate		= freq * 2.0;
		double	iBeta;
		double	normFactor	= 1.0 / (halfWinSize - 1);
	
		// ideal lpf = infinite sinc-function; create truncated version	
		impResp[ 0 ] = (float) smpRate;
		for( int i = 1; i < halfWinSize; i++ ) {
			d				= Math.PI * i / dNum;
			impResp[ i ]	= (float) (Math.sin( smpRate * d ) / d);
		}
		
		// apply Kaiser window
		iBeta = 1.0 / calcBesselZero( dBeta );
		for( int i = 1; i < halfWinSize; i++ ) {
			d				= i * normFactor;
			impResp[ i ]   *= calcBesselZero( dBeta * Math.sqrt( 1.0 - d*d )) * iBeta;
		}
	}
	
	public static float createAntiAliasFilter( float impResp[], float impRespD[], int Nwing,
											   float rollOff, float kaiserBeta )
	{
		return WindowedSincFilter.createAntiAliasFilter( impResp, impRespD, Nwing, FLTSMPPERCROSSING,
											 rollOff, kaiserBeta );
	}

	/**
	 *	@param	impResp			wird mit Impulsantworten gefuellt
	 *	@param	impRespD		Differenzen : null erlaubt, dann keine Interpolation
	 *							von Resample etc. moeglich
	 *	@param	Nwing			Zahl d. Koeffizienten; => smpPerCrossing * ZahlDerNulldurchlaeufe
	 *	@param	smpPerCrossing	bezogen auf den sinc
	 *	@param	rollOff			0...1 CutOff
	 *	@param	kaiserBeta		Parameter fuer Kaiser-Fenster
	 *
	 *	@return	Gain-Wert (abs amp), der den LPF bedingten Lautstaerkeverlust ausgleichen wuerde
	 */
	public static float createAntiAliasFilter( float impResp[], float impRespD[], int Nwing,
											   int smpPerCrossing, float rollOff, float kaiserBeta )
	{
		int		i;
		float	DCgain	= 0.0f;
	
		createLPF( impResp, 0.5f * rollOff, Nwing, kaiserBeta, smpPerCrossing );

		if( impRespD != null ) {
			for( i = 0; i < Nwing - 1; i++ ) {
				impRespD[ i ]	= impResp[ i + 1 ] - impResp[ i ];
			}
			impRespD[ i ]	= -impResp[ i ];
		}
		for( i = smpPerCrossing; i < Nwing; i+= smpPerCrossing ) {
			DCgain	+= impResp[ i ];
		}
		DCgain = 2 * DCgain + impResp[ 0 ];

/*		Frame f = new Frame( "Yo chuck" );
		f.setSize( 440, 440 );
		f.setVisible( true );
		Graphics g = f.getGraphics();
		int lastX = 0;
		int lastY = 0;
		int x;
		int y;
		for( i = 0; i < Nwing; i++ ) {
			x = (int) ((float) i / (float) Nwing * 200);
			y = (int) (impResp[ i ] / impResp[ 0 ] * 200);
			g.drawLine( lastX + 220, 220 - lastY, x + 220, 220 - y );
			g.drawLine( 220 - lastX, 220 - lastY, 220 - x, 220 - y );
			lastX = x;
			lastY = y;
		}
		g.dispose();
*/		
		return( 1.0f / Math.abs( DCgain ));
	}
	
	/**
	 *	@param Nwing	Number of Points (the window is only a half wing!)
	 */
	public static float[] createKaiserWindow( int Nwing, float kaiserBeta )
	{
		float	win[]		= new float[ Nwing ];
		double	dBeta		= kaiserBeta;
		double	d;
		double	iBeta;
		double	normFactor	= 1.0 / (Nwing - 1);
		
		iBeta		= 1.0 / calcBesselZero( dBeta );
		win[ 0 ]	= 1.0f;
		for( int i = 1; i < Nwing; i++ ) {
			d			= i * normFactor;
			win[ i ]	= (float) (calcBesselZero( dBeta * Math.sqrt( 1.0 - d*d )) * iBeta);
		}
		
		return win;
	}

	/**
	 *	Berechnet volles Fenster d.h. linken und rechten Fluegel;
	 *	sollte wenn moeglich statt der einseitigen Version verwendet werden,
	 *	weil nur hier die Periodizitaet wirklich korrekt ist;
	 *
	 *	@param length	Number of Points (should be even)
	 *	@return			Maximum liegt bei sample length/2!
	 */
	public static float[] createFullKaiserWindow( int length, float kaiserBeta )
	{
		float	win[]		= new float[ length ];
		int		Nwing		= length >> 1;
		double	dBeta		= kaiserBeta;
		double	d;
		double	iBeta;
		double	normFactor	= 1.0 / Nwing;
		
		iBeta		= 1.0 / calcBesselZero( dBeta );
		for( int i = 0, j = -Nwing; i < length; i++, j++ ) {
			d			= j * normFactor;
			win[ i ]	= (float) (calcBesselZero( dBeta * Math.sqrt( 1.0 - d*d )) * iBeta);
		}
		
		return win;
	}

	/**
	 *	@param Nwing	Number of Points (the window is only a half wing!)
	 *	@param type		WIN_....
	 */
	public static float[] createWindow( int Nwing, int type )
	{
		float	win[]		= new float[ Nwing ];
		double	normFactor	= Math.PI / (Nwing - 1);
		double	d;
		float	f;
		int		i;
		
		switch( type ) {
		case WIN_BLACKMAN:
			for( i = 0; i < Nwing; i++ ) {
				d			= i * normFactor;
				win[ i ]	= (float) (0.42 + 0.5 * Math.cos( d ) + 0.08 * Math.cos( 2 * d ));
			}
			break;
		case WIN_HANNING:
			for( i = 0; i < Nwing; i++ ) {
				d			= i * normFactor;
				win[ i ]	= (float) (0.5 + 0.5 * Math.cos( d ));
			}
			break;
		case WIN_KAISER4:
			return createKaiserWindow( Nwing, 4.0f );
		case WIN_KAISER5:
			return createKaiserWindow( Nwing, 5.0f );
		case WIN_KAISER6:
			return createKaiserWindow( Nwing, 6.0f );
		case WIN_KAISER8:
			return createKaiserWindow( Nwing, 8.0f );
		case WIN_RECT:
			for( i = 0; i < Nwing; i++ ) {
				win[ i ]	= 1.0f;
			}
			break;
		case WIN_TRI:
			f = 1.0f / (Nwing - 1);
			for( i = 0; i < Nwing; i++ ) {
				win[ i ]	= (Nwing - 1 - i) * f;
			}
			break;
		default:	// WIN_HAMMING
			for( i = 0; i < Nwing; i++ ) {
				d			= i * normFactor;
				win[ i ]	= (float) (0.54 + 0.46 * Math.cos( d ));
			}
			break;
		}

		return win;
	}

	/**
	 *	Berechnet volles Fenster d.h. linken und rechten Fluegel;
	 *	sollte wenn moeglich statt der einseitigen Version verwendet werden,
	 *	weil nur hier die Periodizitaet wirklich korrekt ist;
	 *
	 *	@param length	Number of Points (should be even)
	 *	@param type		WIN_....
	 *	@return			Maximum liegt bei sample length/2!
	 */
	public static float[] createFullWindow( int length, int type )
	{
		float	win[]		= new float[ length ];
		int		Nwing		= length >> 1;
		double	normFactor	= Math.PI / Nwing;
		double	d;
		float	f;
		int		i, j;
		
		switch( type ) {
		case WIN_BLACKMAN:
			for( i = 0, j = -Nwing; i < length; i++, j++ ) {
				d			= j * normFactor;
				win[ i ]	= (float) (0.42 + 0.5 * Math.cos( d ) + 0.08 * Math.cos( 2 * d ));
			}
			break;
		case WIN_HANNING:
			for( i = 0, j = -Nwing; i < length; i++, j++ ) {
				d			= j * normFactor;
				win[ i ]	= (float) (0.5 + 0.5 * Math.cos( d ));
			}
			break;
		case WIN_KAISER4:
			return createFullKaiserWindow( length, 4.0f );
		case WIN_KAISER5:
			return createFullKaiserWindow( length, 5.0f );
		case WIN_KAISER6:
			return createFullKaiserWindow( length, 6.0f );
		case WIN_KAISER8:
			return createFullKaiserWindow( length, 8.0f );
		case WIN_RECT:
			for( i = 0; i < length; i++ ) {
				win[ i ]	= 1.0f;
			}
			break;
		case WIN_TRI:
			f = 1.0f / Nwing;
			for( i = 0; i <= Nwing; i++ ) {
				win[ i ]	= i * f;
			}
			for( j = i; i < length; ) {
				win[ i++ ]	= win[ --j ];
			}
			break;
		default:	// WIN_HAMMING
			for( i = 0, j = -Nwing; i < length; i++, j++ ) {
				d			= j * normFactor;
				win[ i ]	= (float) (0.54 + 0.46 * Math.cos( d ));
			}
			break;
		}

		return win;
	}

	/**
	 *	Array mit den Namen der vorhandenen Fenster besorgen
	 */	
	public static String[] getWindowNames()
	{
		return windowNames;
	}
	
	/**
	 *	Type zu WindowNamen besorgen; kann als Parameter fuer createWindow benutzt werden
	 *	wenn Name unbekannt, wird Hamming-Fenster zurueckgegeben
	 */
	public static int getWindowType( String windowName )
	{
		for( int i = 0; i < windowNames.length; i++ ) {
			if( windowNames[ i ].equals( windowName )) return i;
		}
		return WIN_HAMMING;
	}

	/**
	 *	Daten resamplen; "bandlimited interpolation"
	 *
	 *	@param	srcOff		erlaubt verschiebung unterhalb sample-laenge!
	 *	@param	length		bzgl. dest!! src muss laenge 'length/factor' aufgerundet haben!!
	 *	@param	factor		dest-smpRate/src-smpRate
	 *	@param	quality		0 = low, 1 = medium, 2 = high
	 */
	public static void resample( float src[], double srcOff, float dest[], int destOff, int length,
								 double factor, int quality )
	{
		int		fltSmpPerCrossing	= 4096;
		int		fltCrossings;
		float	fltRolloff, fltWin;
		switch( quality ) {
		case 0:
			fltRolloff				= 0.70f;
			fltWin					= 6.5f;
			fltCrossings			= 5;
			break;
		case 1:
			fltRolloff				= 0.80f;
			fltWin					= 7.0f;
			fltCrossings			= 9;
			break;
		default:
			fltRolloff				= 0.86f;
			fltWin					= 7.5f;
			fltCrossings			= 15;
			break;
		}
		int		fltLen				= (int) ((fltSmpPerCrossing * fltCrossings) / fltRolloff + 0.5f);
		float	flt[]				= new float[ fltLen ];
		float	fltD[]				= null;	// new float[ fltLen ];
		float	gain				= WindowedSincFilter.createAntiAliasFilter( flt, fltD, fltLen, fltSmpPerCrossing,
																	fltRolloff, fltWin );
		float	filter[][]			= new float[ 4 ][];
		
		filter[ 0 ] = flt;
		filter[ 1 ] = fltD;
		filter[ 2 ] = new float[ 2 ];
		filter[ 2 ][ 0 ] = fltSmpPerCrossing;
		filter[ 2 ][ 1 ] = gain;

		WindowedSincFilter.resample( src, srcOff, dest, destOff, length, factor, filter );
	}
			
	/**
	 *	Daten resamplen; "bandlimited interpolation"
	 *
	 *	@param	srcOff		erlaubt verschiebung unterhalb sample-laenge!
	 *	@param	length		bzgl. dest!! src muss laenge 'length/factor' aufgerundet haben!!
	 *	@param	factor		dest-smpRate/src-smpRate
	 *	@param	filter		Dimension 0: flt (erstes createAntiAliasFilter Argument), 1: fltD;
	 *						2: fltSmpPerCrossing; 3: fltGain (createAAF result)
	 *
	 *	wenn fltD == null => keine lineare Interpolation (wesentlich schneller!)
	 */
	public static void resample( float src[], double srcOff, float dest[], int destOff, int length,
								 double factor, float[][] filter )
	{
		double	smpIncr				= 1.0 / factor;
		int		i, fltOffI, srcOffI;
		double	q, r, val, fltIncr, fltOff;
		double	phase				= srcOff;

		float	flt[]				= filter[ 0 ];
		float	fltD[]				= filter[ 1 ];
		float	fltSmpPerCrossing	= filter[ 2 ][ 0 ];
		double	gain				= filter[ 2 ][ 1 ];
		double  rsmpGain;
		int		fltLen				= flt.length;
		int		srcLen				= src.length;
		
		if( smpIncr > 1.0 ) {
			fltIncr		= fltSmpPerCrossing * factor;
			rsmpGain	= gain;
		} else {
			fltIncr		= fltSmpPerCrossing;
			rsmpGain	= gain * smpIncr;
		}
		
		if( fltD == null ) {	// -------------------- without interpolation --------------------

			for( i = 0; i < length; i++, phase = srcOff + i * smpIncr ) {

				q		= phase % 1.0;
				val		= 0.0;
//				k		= -1;					// src-smpIncr

				srcOffI	= (int) phase;
				fltOff	= q * fltIncr + 0.5f;	// wenn wir schon keine interpolation mehr benutzen...
				fltOffI	= (int) fltOff;
				while( (fltOffI < fltLen) && (srcOffI >= 0) ) {
					val	   += (double) src[ srcOffI ] * flt[ fltOffI ];
					srcOffI--;
					fltOff += fltIncr;
					fltOffI	= (int) fltOff;
				}
	
				srcOffI	= (int) phase + 1;
				fltOff	= (1.0 - q) * fltIncr;
				fltOffI	= (int) fltOff;
				while( (fltOffI < fltLen) && (srcOffI < srcLen) ) {
					val	   += (double) src[ srcOffI ] * flt[ fltOffI ];
					srcOffI++;
					fltOff += fltIncr;
					fltOffI	= (int) fltOff;
				}
	
				dest[ destOff++ ] = (float) (val * rsmpGain);
			}
		
		} else {	// -------------------- linear interpolation --------------------

			for( i = 0; i < length; i++, phase = srcOff + i * smpIncr ) {

				q		= phase % 1.0;
				val		= 0.0;
//				k		= -1;					// src-smpIncr

				srcOffI	= (int) phase;
				fltOff	= q * fltIncr;
				fltOffI	= (int) fltOff;
				while( (fltOffI < fltLen) && (srcOffI >= 0) ) {
					r		= fltOff % 1.0;		// 0...1 for interpol.
					val	   += src[ srcOffI ] * (flt[ fltOffI ] + fltD[ fltOffI ] * r);
					srcOffI--;
					fltOff += fltIncr;
					fltOffI	= (int) fltOff;
				}
	
				srcOffI	= (int) phase + 1;
				fltOff	= (1.0 - q) * fltIncr;
				fltOffI	= (int) fltOff;
				while( (fltOffI < fltLen) && (srcOffI < srcLen) ) {
					r		= fltOff % 1.0;		// 0...1 for interpol.
					val	   += src[ srcOffI ] * (flt[ fltOffI ] + fltD[ fltOffI ] * r);
					srcOffI++;
					fltOff += fltIncr;
					fltOffI	= (int) fltOff;
				}
	
				dest[ destOff++ ] = (float) (val * rsmpGain);
			}
		}
	}
	
	/**
	 *	Energie berechnen (sum(x^2))
	 */
	public static double calcEnergy( float a[], int off, int length )
	{
		double	energy	= 0.0;
		int		stop	= off + length;
		
		while( off < stop ) {
			energy += a[ off ]*a[ off++ ];
		}
		
		return energy;
	}

// -------- private Methoden --------

	protected static double calcBesselZero( double x )
	{
		double	d1;
		double	d2	= 1.0;
		double	sum	= 1.0;
		int		n	= 1;
	
		x /= 2.0;
	
		do {
			d1	 = x / n;
			n++;
			d2	*= d1*d1;
			sum	+= d2;
		} while( d2 >= sum*1e-21 );	// auf 20 Nachkommastellen genau
		
		return sum;
	}
}
// class Filter
