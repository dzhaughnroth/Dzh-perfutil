package com.johncroth.histo.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Listeners {
	
	public Listeners() {
	};

	static abstract class AbstractWritingListener implements LogHistogramLogger.Listener {
		LogHistogramWriter writer = new LogHistogramWriter();
		protected String format( RecordedInterval<LogHistogramRecorder> ri ) {
			return writer.formatMessage(ri);
		}
		
	}
	
	public static class JavaLoggingListener extends AbstractWritingListener {
		
		public JavaLoggingListener( Logger logger ) {
			this.logger = logger;
		}
		
		public JavaLoggingListener( ) {
			this( Logger.getLogger( JavaLoggingListener.class.getPackage().getName() ));
		}


		Logger logger;
		@Override
		public void intervalRecorded(RecordedInterval<LogHistogramRecorder> i) {
			String x = format( i );
			logger.info( x );
		}
		
	}
	
	/**
	 * Logs to files in the temporary directory
	 * @author jroth
	 */
	public static class InstantLoggingListener extends JavaLoggingListener {
		
		static String dirName = "histograms"; // for unit testing only.
		
		public InstantLoggingListener( ) throws IOException {
			this( "default" );
		}
		
		public InstantLoggingListener(String appname) throws IOException {
			super();
			File tmpdir = new File(System.getProperty("java.io.tmpdir"));
			File histdir = new File(tmpdir, dirName);
			File appdir = new File(histdir, appname);
			if (!appdir.exists()) {
				appdir.mkdirs();
			}

			FileHandler fh = new FileHandler("%t/" + dirName + "/" + appname
					+ "/histograms%g.log", true);
			fh.setFormatter(LogHistogramWriter.createJavaLoggingFormatter());
			fh.setLevel(Level.INFO);
			logger.setLevel(Level.INFO);
			logger.setUseParentHandlers(false);
			logger.addHandler(fh);
		}
	}

	public static class AccumulatingListener implements LogHistogramLogger.Listener {

		LogHistogramRecorder accumulated = new LogHistogramRecorder();
		long start = Long.MAX_VALUE, end = Long.MIN_VALUE;
		
		@Override
		public synchronized void intervalRecorded(RecordedInterval<LogHistogramRecorder> i) {
			accumulated.absorb( i.getRecorder() );
			start = Math.min( start, i.getStartMillis() );
			end = Math.max( end,i.getEndMillis() );
		}
		
		/**
		 * Returns a snapshot of the accumulated data.
		 */
		public synchronized RecordedInterval<LogHistogramRecorder> getAccumulationView() {
			LogHistogramRecorder result = new LogHistogramRecorder();
			result.absorb( accumulated );
			return new RecordedInterval<LogHistogramRecorder>( result, start, end );
		}

	}
	

	
}
