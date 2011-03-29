package com.johncroth.userload;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.userload.RollingMeanDelayGovernor;


public class RollingMeanDelayGovernorTest extends Assert {

	
	RollingMeanDelayGovernor g = new RollingMeanDelayGovernor();
	
	@Test
	public void testRecommendedActualDelay() {
		assertEquals( 50, g.recommendActualDelay( 50 ));
		for( int i = 0; i < g.rollThreshold - 1; i++ ) {
			g.reportDelay( 100, 100, 109 + 2 * i % 2 );
		}
		assertEquals( 39, g.recommendActualDelay( 39 ) );
		g.reportDelay( 50, 50, 110 );
		assertEquals( 15, g.recommendActualDelay( 25 ));
	}

}
