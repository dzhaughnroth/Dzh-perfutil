package com.johncroth.userload;

import java.util.concurrent.Executors;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.johncroth.testutil.BeanPropertiesTester;

public class RampUpActionSequenceTest extends Assert {
	
	TestActionSequenceFactory factory = new TestActionSequenceFactory();
	
	class MyRamp extends RampUpActionSequence {

		protected MyRamp(ActionSequenceExecutor exec) {
			super(exec);
		}

		@Override
		protected ActionSequence create() {
			return factory.create();			
		}
		
	}

	ActionSequenceExecutor exec = new JavaUtilActionSequenceExecutor( Executors.newScheduledThreadPool(10));
	MyRamp ramp;

	@BeforeMethod
	public void setUp() throws Exception {
		ramp = new MyRamp(exec);
		ramp.setPeakCount( 10 );
		ramp.setSecondsToRampUp( 1 );
		ramp.setSecondsToHold( 1 );
	}

	@Test
	public void testBean() {
		BeanPropertiesTester t = new BeanPropertiesTester( ramp );
		t.testProperties();
		assertEquals( t.getFailures().size(), 0, String.valueOf( t.getFailures() ));
	}
	
	@Test
	public void testRamping() throws Exception {
		exec.addActionSequence( ramp );
		for( int i = 0; i < 22; i++ ) {
			Thread.sleep( 100 );
			assertEquals( Math.min( (i+1), ramp.getPeakCount() ), factory.getSequences().size(), 1 );
		}
		assertEquals( factory.getSequences().size(), 10 );
		assertTrue( exec.isShutdown() );
	}
	
}
