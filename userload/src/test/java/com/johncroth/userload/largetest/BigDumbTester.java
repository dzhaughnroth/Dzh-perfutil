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

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.johncroth.histo.client.EventRecorder;
import com.johncroth.histo.core.LogHistogramCalculator;
import com.johncroth.histo.logging.LogHistogramRecorder;
import com.johncroth.userload.ActionSeq;
import com.johncroth.userload.ActionSequence;
import com.johncroth.userload.ActionSequenceExecutor;
import com.johncroth.userload.JavaUtilActionSequenceExecutor;
import com.johncroth.userload.Seq;


public class BigDumbTester implements Cloneable {

	private static final String INTENDED = "intended";
	public static final String DIFF_METRIC_NAME = "diff";
	private static final String DELAY_METRIC_NAME = "measuredDelay";
	private static final String SLEEP_METRIC_NAME = "sleepTime";
	public Object clone() {
		try {
			BigDumbTester result = (BigDumbTester) super.clone();
			result.recorder = new LogHistogramRecorder();
			return result;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException( e );
		}
	}
	

	int threadCount = 20;
	int peakCount = 40;
	int secondsToRamp = 10;
	int secondsToHold = 10;
	int busyTime = 10;
	int busyDev = 4;
	int delayTime = 90;
	int delayDev = 10;
	
	LogHistogramRecorder recorder = new LogHistogramRecorder();
	
	public int getExpectedCount() {
		return (int) (peakCount * ( secondsToHold * 1000. + secondsToRamp * 500. ) / (busyTime + delayTime ));
	}
	
	public int getTheoreticalMax() {
		return (threadCount * 1000 * (secondsToHold + secondsToRamp)) / busyTime;
	}
	
	public LogHistogramCalculator getResultHistogram() {
		return new LogHistogramCalculator( recorder.getHistogramMap().get( DELAY_METRIC_NAME ) );
	}
	
	public LogHistogramCalculator getDiffHistogram() {
		return new LogHistogramCalculator( recorder.getHistogramMap().get( DIFF_METRIC_NAME ) );
	}
	public LogHistogramCalculator getSleepHistogram() {
		return new LogHistogramCalculator( recorder.getHistogramMap().get( SLEEP_METRIC_NAME ));
	}
	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public int getPeakCount() {
		return peakCount;
	}

	public void setPeakCount(int peakCount) {
		this.peakCount = peakCount;
	}

	public int getSecondsToRamp() {
		return secondsToRamp;
	}

	public void setSecondsToRamp(int secondsToRamp) {
		this.secondsToRamp = secondsToRamp;
	}

	public int getSecondsToHold() {
		return secondsToHold;
	}

	public void setSecondsToHold(int secondsToHold) {
		this.secondsToHold = secondsToHold;
	}

	public int getBusyTime() {
		return busyTime;
	}

	public void setBusyTime(int busyTime) {
		this.busyTime = busyTime;
	}

	public int getBusyDev() {
		return busyDev;
	}

	public void setBusyDev(int busyDev) {
		this.busyDev = busyDev;
	}

	public int getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(int delayTime) {
		this.delayTime = delayTime;
	}

	public int getDelayDev() {
		return delayDev;
	}

	public void setDelayDev(int delayDev) {
		this.delayDev = delayDev;
	}

	public EventRecorder getRecorder() {
		return recorder;
	}
	

	public void run() throws Exception {
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(threadCount);
//		int threadRatio = 10;
//		ScheduledExecutorService ses = new RoundRobinScheduledExecutorService(
//				threadCount / threadRatio, threadRatio);
		final JavaUtilActionSequenceExecutor exec = new JavaUtilActionSequenceExecutor(
				ses);
		exec.setMonitor( new ActionSequenceExecutor.Monitor() {
			
			@Override
			public void slept(ActionSequence seq, long intendedAmount, long actualAmount) {
			}
			
			@Override
			public void failed(ActionSequence seq, Runnable action, Throwable e,
					long elpasedMillis) {
				e.printStackTrace();				
			}
			
			@Override
			public void completed(ActionSequence seq, Runnable action,
					long elapsedMillis) {
			}
		});
		Iterator<ActionSequence> factory = new Seq<ActionSequence>() {

			@Override
			public ActionSequence next() {
				return new DumbSeq();
			}
		};
		ActionSeq ramper = ActionSeq.ramper(peakCount, secondsToRamp * 1000L,
				factory, exec);
		exec.addActionSequence(ramper);
		
		Thread.sleep(1000L * (secondsToHold + secondsToRamp));
		ses.shutdown();
		ses.awaitTermination(5000, TimeUnit.MILLISECONDS);
	}

	public class DumbSeq implements ActionSequence {

		int count = 0;
		long lastRun = System.nanoTime();
		long expectedSleep = -1;
		Random rand = new Random( 0 );

		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public Runnable nextAction() {
			return new Runnable() {
				
				@Override
				public void run() {
					long now = System.nanoTime();
					long elapsed = now - lastRun;
					recorder.recordEvent( DELAY_METRIC_NAME, elapsed / 1000000L );
					if ( expectedSleep >= 0 ) {
						recorder.recordEvent( DIFF_METRIC_NAME, Math.abs( (elapsed - expectedSleep) / 1000000L ) );
					}
					++count;
					long sleepTime = busyTime;
					if ( busyDev > 0 ) {
						sleepTime = sleepTime - busyDev + rand.nextInt( 2* busyDev );
					}
					recorder.recordEvent( INTENDED, sleepTime );
					try {
						Thread.sleep( sleepTime );
					}
					catch( Exception e ) {
						
					}
					lastRun = System.nanoTime();
					recorder.recordEvent( SLEEP_METRIC_NAME, Math.max( 0, sleepTime ) );
				}
			};
		}

		@Override
		public Long nextDelay() {
			long result = delayTime;
			if ( delayDev > 0 ) {
				result += rand.nextInt( 2 * delayDev) - delayDev;
			}
			recorder.recordEvent( "scheduled", result );
			expectedSleep = result * 1000000L;
			return result;
		}
		
	}

	public LogHistogramCalculator getIntendedHistogram() {
		return new LogHistogramCalculator( recorder.getHistogram( INTENDED ));
	}
	public LogHistogramCalculator getScheduledHistogram() {
		return new LogHistogramCalculator( recorder.getHistogram( "scheduled" ));
	}

}
