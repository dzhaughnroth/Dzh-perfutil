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

import java.util.Iterator;



public class ActionSeq extends SeqWithDelays<Runnable> implements ActionSequence {

	static <T extends Runnable> Iterator<Runnable> duh( final Iterator<T> x ) {
		return new Iterator<Runnable>() {

			@Override
			public boolean hasNext() {
				return x.hasNext();
			}

			@Override
			public Runnable next() {
				return x.next();
			}

			@Override
			public void remove() {
				x.remove();				
			}
		};
	}
	
	public ActionSeq( Iterator<? extends Runnable> actions, Iterator<Long> delays ) {
		super( duh( actions ), delays );
	}
	
	/**
	 * Convenient constant-delay constructor
	 */
	public <T extends Runnable> ActionSeq( Iterator<T> actions, Long constantDelay ) {
		super( duh( actions ), Seq.constant( constantDelay ));
	}
	
	@Override
	public Runnable nextAction() {
		return next();
	}
	
	/**
	 * Use to gently start a given number of {@link ActionSequence} on the given
	 * executor over the given period of time.
	 */
	public static ActionSeq ramper(int count, long totalMillisToRamp,
			final Iterator<? extends ActionSequence> actions,
			final ActionSequenceExecutor exec) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					if ( actions.hasNext() ) {
						exec.addActionSequence( actions.next());
					}
				} catch (Exception e) {
					throw new RuntimeException( e );
				}
			}
		};
		return new ActionSeq(Seq.finite(Seq.constant(r), count),
				totalMillisToRamp / count);
	}

	/**
	 * Special case of
	 * {@link #ramper(int, long, Iterator, ActionSequenceExecutor)} where the
	 * instances are obtained simply by {@link Class#newInstance()}.
	 */
	public static ActionSeq ramper(int count, long totalMillisToRamp,
			final Class<? extends ActionSequence> klass,
			final ActionSequenceExecutor exec) {
		return ramper(count, totalMillisToRamp, Seq.generator( klass ), exec);
	}


	
}
