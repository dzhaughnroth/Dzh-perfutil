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
package com.johncroth.testutil;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.johncroth.testutil.BeanPropertiesTester;

public class BeanPropertiesTesterTest extends Assert {

	static class SimpleBean {
		String foo;

		public String getFoo() {
			return foo;
		}

		public void setFoo(String foo) {
			this.foo = foo;
		}

		int count;

		public int getCount() {
			return count;
		}

		public void setCount(int x) {
			count = x;
		}

	}

	static class Bean extends SimpleBean {

		public String getOnly() {
			return "only";
		}

		public void setFoo(String foo, String auxilliary) {
			throw new RuntimeException("Don't call me, bro");
		}

		public void setUnreadable(String what) {
			new Object();
		}

		public String getBrokenBar() {
			return "broken";
		}

		public void setBrokenBar(String x) {
		}

		String nullIllegal = "default";

		public String getNullIllegal() {
			return nullIllegal;
		}

		public void setNullIllegal(String val) {
			if (val == null) {
				throw new RuntimeException("No.");
			}
			new Object();
		}

		String alwaysNull = "notso";

		public String getAlwaysNull() {
			return alwaysNull;
		}

		public void setAlwaysNull(String x) {
			alwaysNull = null;
		}

	}

	@Test
	public void testTester() throws Exception {
		BeanPropertiesTester t = new BeanPropertiesTester(new Bean());
		t.testProperties();
		Map<String, String> s = t.getSuccesses();
		assertEquals(s.size(), 4);
		assertEquals("", s.get("getClass"));
		assertEquals("", s.get("getOnly"));
		assertEquals("setFoo", s.get("getFoo"));
		assertEquals("setCount", s.get("getCount"));

		Map<String, String> f = t.getFailures();
		assertEquals(f.size(), 3);
		assertTrue(f.get("getNullIllegal").indexOf("Exception") >= 0);
		assertEquals(BeanPropertiesTester.SET_TO_NULL_FAILED,
				f.get("getBrokenBar"));
		assertEquals(BeanPropertiesTester.RESET_FAILED, f.get("getAlwaysNull"));

	}

	@Test
	public void testUncovered() throws Exception {
		try {
			new Bean().setFoo("", "");
			fail();
		} catch (RuntimeException e) {

		}
		Bean b = new Bean();
		b.setCount(12);
		b.setUnreadable(null);
		b.setNullIllegal("");
	}
}
