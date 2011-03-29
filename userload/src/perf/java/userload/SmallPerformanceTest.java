package userload;

import histo.core.BucketSummary;
import histo.core.LogHistogramCalculator;
import junit.framework.TestCase;

public class SmallPerformanceTest extends TestCase {
	
	BigDumbTester tester = new BigDumbTester();
	
	public void setUp() throws Exception {
		tester.setBusyTime( 10 );
		tester.setDelayDev( 10 );
		tester.setDelayTime( 90 );
		tester.setThreadCount( 50 );
		tester.setSecondsToHold( 10 );
		tester.setSecondsToRamp( 10 );
		tester.setPeakCount( 300 );
	}

	LogHistogramCalculator calc, sleepCalc, intCalc, scheduled;
	
	
	void printHistogram( String name, LogHistogramCalculator calc ) {
		System.out.println( name + " " + calc.getTotalCount() + " " + calc.getMean() );
		System.out.println( bucketBounds( calc ) );
	}
	
	void runTester() throws Exception {
		tester.run();
		calc = tester.getResultHistogram();	
		sleepCalc = tester.getSleepHistogram();
		intCalc = tester.getIntendedHistogram();
		printHistogram( "calc", calc );
		printHistogram( "scheduled", tester.getScheduledHistogram() );
		printHistogram( "sleep", sleepCalc);
		printHistogram( "intended", intCalc );
		System.out.println( calc.getTotalCount() + " out of expected " + tester.getExpectedCount() + " resource bound at " + tester.getTheoreticalMax() );
	}

	
	StringBuilder bucketBounds( LogHistogramCalculator c ) {
		StringBuilder sb = new StringBuilder();
		for( BucketSummary b : c.bucketsFor( c.calculateDefaultQuantileBuckets() ) ) {
			sb.append( b.getUpperBound() );
			sb.append( "," );
		}
		return sb;
	}
	
	public void testPerformanceLightly() throws Exception {
		runTester();
		assertTrue( calc.getTotalCount() + " too small, should be close to " + tester.getExpectedCount(),
				calc.getTotalCount() > tester.getExpectedCount() * .9 );		
	}
	
	public void testPerformanceMoreSequencesLongerDelay() throws Exception {
		tester.setDelayTime( (tester.getDelayTime() + tester.getBusyTime() ) * 10 - tester.getBusyTime() );
		tester.setDelayDev( tester.getDelayDev() * 10 );
		tester.setPeakCount( tester.getPeakCount() * 10 );
		runTester();
		assertTrue( calc.getTotalCount() + " too small, should be close to " + tester.getExpectedCount(),
				calc.getTotalCount() > tester.getExpectedCount() * .9 );		
	}
	
	public void testPerformanceHarder() throws Exception {
		tester.setDelayTime( (tester.getDelayTime() + tester.getBusyTime() ) * 10 - tester.getBusyTime() );
		tester.setDelayDev( tester.getDelayDev() * 10 );
		tester.setPeakCount( tester.getPeakCount() * 30 );
		tester.setThreadCount( tester.getThreadCount() * 3 );
		runTester();
		assertTrue( calc.getTotalCount() + " too small, should be close to " + tester.getExpectedCount(),
				calc.getTotalCount() > tester.getExpectedCount() * .9 );		
	}
	
}