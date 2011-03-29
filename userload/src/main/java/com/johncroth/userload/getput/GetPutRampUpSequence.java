package com.johncroth.userload.getput;

import com.johncroth.userload.ActionSequence;
import com.johncroth.userload.ActionSequenceExecutor;
import com.johncroth.userload.RampUpActionSequence;

public class GetPutRampUpSequence extends RampUpActionSequence {

	public GetPutRampUpSequence( ActionSequenceExecutor exec, 
			IdSequence idSeq, GetPutActionSequence template ) {
		super( exec );
		this.idSeq = idSeq;
		this.template = template;
	}
	IdSequence idSeq;
	GetPutActionSequence template;
	@Override
	protected ActionSequence create() {
		GetPutActionSequence result = new GetPutActionSequence();
		result.setDb( template.getDb() );
		result.setValueGenerator( template.getValueGenerator() );
		result.setId( idSeq.next() );
		return result;
	}

}
