package com.johncroth.userload.getput;

import com.johncroth.histo.logging.UnitTestEventRecorder;
import com.johncroth.userload.ActionSequenceExecutor;
import com.johncroth.userload.JavaUtilActionSequenceExecutor;
import com.johncroth.userload.getput.GaussianIntegerSequence;
import com.johncroth.userload.getput.GetPutActionSequence;
import com.johncroth.userload.getput.GetPutRampUpSequence;
import com.johncroth.userload.getput.IdSequence;
import com.johncroth.userload.getput.InMemoryTable;
import com.johncroth.userload.getput.ValueGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class GetPutRampUpTest extends Assert {
	
	ActionSequenceExecutor exec;
	
	@BeforeMethod
	public void setUp() throws Exception {
		exec = new JavaUtilActionSequenceExecutor( Executors.newScheduledThreadPool( 10 ));
		template = new GetPutActionSequence();
		template.setDb( tdb );
		template.setValueGenerator( vg );
		ramp = new GetPutRampUpSequence(exec, idSeq, template);
		ramp.setPeakCount( 5 );
		ramp.setSecondsToRampUp( 10 );
		ramp.setSecondsToHold( 10 );
	}

	IdSequence idSeq = new IdSequence( new GaussianIntegerSequence( 500000000, 5000000, 1000000 ));
	InMemoryTable db = new InMemoryTable();
	ValueGenerator vg = new ValueGenerator();
	List<GetPutActionSequence> seqs = new ArrayList<GetPutActionSequence>();
	UnitTestEventRecorder rec = new UnitTestEventRecorder();
	DistributedHashtableForTesting tdb = new DistributedHashtableForTesting( db );
	GetPutActionSequence template;
	GetPutRampUpSequence ramp;

	@Test
	public void testCreate() throws Exception {
		GetPutActionSequence seq = (GetPutActionSequence) ramp.create();
		assertSame( seq.getDb(), template.getDb());
		assertSame( seq.getValueGenerator(), template.getValueGenerator() );
		assertNotNull( seq.getId() );
	}
	
}
