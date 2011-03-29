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
package com.johncroth.userload;

import com.johncroth.histo.core.LogHistogramCalculator;

import java.util.ArrayList;

public class BigDumbTesterSeries {

	public static void main(String[] args) {
		BigDumbTesterSeries x = new BigDumbTesterSeries();
		x.template.setSecondsToRamp(20);
		x.template.setSecondsToHold(20);
		// x.template.setBusyTime( 100 );
		// x.template.setBusyDev( 10 );
		x.template.setDelayTime(100);
		x.template.setDelayDev(10);
		x.runAll();
	}

	BigDumbTester template = new BigDumbTester();

	int[] threadCounts = new int[] { 120 };
	int[] actionsPerSecond = new int[] { 5000, 15000 };
	int[] delayTimes = new int[] { 100, 100 };
	int[] deviationRatios = new int[] { 10 };

	int[] busyTimes = new int[] { 10, 2 };

	BigDumbTester inProgress;

	ArrayList<BigDumbTester> finishedTesters = new ArrayList<BigDumbTester>();

	public void runAll() {
		for (int threadCount : threadCounts) {
			for (int devRatio : deviationRatios) {
				for (int delayTime : delayTimes) {
					int delayDev = delayTime / devRatio;
					for (int actionRate : actionsPerSecond) {
						int peakCount = (int) (actionRate * delayTime / 1000);
						for (int k : busyTimes) {
							BigDumbTester t = (BigDumbTester) template.clone();
							t.setThreadCount(threadCount);
							t.setPeakCount(peakCount);
							t.setDelayTime(delayTime);
							t.setDelayDev(delayDev);
							t.setBusyTime(k);
							t.setBusyDev( 0 );
							inProgress = t;
							try {
								t.run();
								LogHistogramCalculator calc = t
										.getResultHistogram();
								LogHistogramCalculator diff = t.getDiffHistogram();
								System.out.println("Finished threads=" + threadCount
										+ " peak=" + peakCount + " delay=" + delayTime + " busy=" + k);
								System.out.println("  Count "
										+ calc.getTotalCount() + " (expected "
										+ t.getExpectedCount() + ")");
								double perc= ((double) calc.getTotalCount() )/ t.getExpectedCount();
								System.out.println( "  " + perc );
								System.out.println("  Range: "
										+ calc.getUpperBound() + " "
										+ calc.getLowerBound());
								System.out.println("  Diff range: "
										+ diff.getUpperBound() + " "
										+ diff.getLowerBound());
								System.out.println("  "
										+ diff.calculateDefaultQuantileBuckets());
								System.out.println("------");
								finishedTesters.add(t);
								inProgress = null;
							} catch (Exception e) {
								e.printStackTrace();
							}
							// System.gc(); // FIXME not a bad idea but
							// callback!
						}
					}
				}
			}
		}
	}

}
