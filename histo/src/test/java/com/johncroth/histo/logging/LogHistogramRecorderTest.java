/**
 * Perfutil -- https://github.com/dzhaughnroth/Dzh-perfutil 
 * (C) 2011 John Charles Roth
 *
 * Perfutil is free software, licensed under the terms of the GNU GPL 
 * Version 2 or, at your option, any later version. You should have 
 * received a copy of the license with this file. See the above web address
 * for more information, or contact the Free Software Foundation, Boston, MA. 
 * It is distributed WITHOUT WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.johncroth.histo.logging;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.histo.core.LogHistogramCalculator;
import com.johncroth.histo.logging.LogHistogramRecorder;

public class LogHistogramRecorderTest extends Assert {
	
	@Test
	public void testBasics() {
		LogHistogramRecorder rec = new LogHistogramRecorder();
		rec.recordEvent( "foo", 210 );
		rec.recordEvent( "foo", 21 );
		rec.recordEvent( "bar", 21 );
		assertEquals( 2, rec.getHistogramMap().size() );
		LogHistogramCalculator fooCalc = new LogHistogramCalculator( rec.getHistogram( "foo" ));
		LogHistogramCalculator barCalc = new LogHistogramCalculator( rec.getHistogram( "bar" ));
		assertEquals( 2, fooCalc.getTotalCount() );
		assertEquals( 1, barCalc.getTotalCount() );
		
		LogHistogramRecorder rec2 = new LogHistogramRecorder();
		rec2.recordEvent( "baz", 1 );
		rec2.recordEvent( "bar", 21 );
		
		rec2.absorb( rec );
		LogHistogramCalculator fooCalc2 = new LogHistogramCalculator( rec2.getHistogram( "foo" ));
		LogHistogramCalculator barCalc2 = new LogHistogramCalculator( rec2.getHistogram( "bar" ));
		LogHistogramCalculator bazCalc2 = new LogHistogramCalculator( rec2.getHistogram( "baz" ));
		assertEquals( fooCalc.getTotalCount(), fooCalc2.getTotalCount() );
		assertEquals( barCalc.getTotalCount() + 1, barCalc2.getTotalCount() );
		assertEquals( 1, bazCalc2.getTotalCount() );
		
		
	}

}