package com.s8.io.bohr.beryllium.fields.arrays;

import com.s8.io.bohr.beryllium.fields.BeField;
import com.s8.io.bohr.beryllium.fields.BeFieldDelta;
import com.s8.io.bohr.beryllium.object.BeObject;
import com.s8.io.bytes.alpha.MemoryFootprint;



/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class LongArrayBeFieldDelta extends BeFieldDelta {

	public final LongArrayBeField field;
	
	public final long[] value;

	public LongArrayBeFieldDelta(LongArrayBeField field, long[] array) {
		super();
		this.field = field;
		this.value = array;
	}


	@Override
	public BeField getField() { 
		return field;
	}


	@Override
	public void consume(BeObject object) throws IllegalArgumentException, IllegalAccessException {
		field.field.set(object, value);
	}


	@Override
	public void computeFootprint(MemoryFootprint weight) {
		if(value!=null) {
			weight.reportInstance(); // the array object itself	
			weight.reportBytes(value.length*8);
		}
	}

}
