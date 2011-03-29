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
