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
package com.johncroth.userload.largetest;

import org.testng.Assert;
import org.testng.annotations.Test;


public class BigDumbTesterSmallTest extends Assert {

	@Test
	public void testAbit() throws Exception {
		BigDumbTester t = new BigDumbTester();
		t.setThreadCount(10);
		t.setPeakCount(50);
		t.setSecondsToHold(3);
		t.setSecondsToRamp(3);
		t.setBusyTime(5);
		t.setBusyDev(2);
		t.setDelayTime(45);
		t.setDelayDev(5);
		t.run();

		assertTrue(
				Math.abs(t.getExpectedCount()
						- t.getResultHistogram().getTotalCount()) < t
						.getExpectedCount() * .02,
				t.getExpectedCount() + " vs. "
						+ t.getResultHistogram().getTotalCount());

	}
}
