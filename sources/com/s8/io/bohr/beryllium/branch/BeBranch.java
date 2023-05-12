package com.s8.io.bohr.beryllium.branch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.s8.io.bohr.beryllium.codebase.BeCodebase;
import com.s8.io.bohr.beryllium.exception.BeIOException;
import com.s8.io.bohr.beryllium.fields.BeField;
import com.s8.io.bohr.beryllium.fields.BeFieldDelta;
import com.s8.io.bohr.beryllium.object.BeObject;
import com.s8.io.bohr.beryllium.object.CreateBeObjectDelta;
import com.s8.io.bohr.beryllium.object.RemoveBeObjectDelta;
import com.s8.io.bohr.beryllium.object.UpdateBeObjectDelta;
import com.s8.io.bohr.beryllium.types.BeType;


/**
 * 
 * @author pierreconvert
 *
 */
public class BeBranch {


	public final BeCodebase codebase;


	/**
	 * last known state of table
	 */
	public final BeTable table = new BeTable();


	private long version;

	private BeBranchDelta branchDelta = null;



	/**
	 * 
	 * @param codebase
	 */
	public BeBranch(BeCodebase codebase) {
		super();
		this.codebase = codebase;
	}


	/**
	 * 
	 * @param delta
	 * @throws BeIOException
	 */
	public void pushDelta(BeBranchDelta delta) throws BeIOException {
		delta.consume(table);
	}


	/**
	 * 
	 * @param object
	 * @throws BeIOException
	 */
	public void set(BeObject object) throws BeIOException {
		if(branchDelta == null) {
			branchDelta = new BeBranchDelta(version);
		}


		String id = object.S8_key;


		BeType type = codebase.getType(object);

		BeObject previous = table.objects.get(id);
		if(previous != null) {
			BeType previousType = codebase.getType(previous);


			/* REMOVE and CREATE */
			if(type != previousType) {
				publishRemove(id);
				publishCreate(id, type, object);
			}

			/* UPDATE object */
			else {
				publishUpdate(id, type, previous, object);
				
				
			}
		}
		else { /* only CREATE */
			publishCreate(id, type, object);
		}
		
		BeObject objectClone = type.deepClone(object);
		
		table.objects.put(id, objectClone);
	}


	private void publishCreate(String id, BeType type, BeObject object) throws BeIOException {
		List<BeFieldDelta> fieldDeltas = new ArrayList<BeFieldDelta>();
		BeField[] fields = type.fields;
		int n = fields.length;
		for(int i=0; i<n; i++) {
			try {
				fieldDeltas.add(fields[i].produceDiff(object));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new BeIOException(e.getMessage());
			}
		}
		branchDelta.objectDeltas.add(new CreateBeObjectDelta(id, type, fieldDeltas));
	}



	private void publishUpdate(String id, BeType type, BeObject previous, BeObject object) throws BeIOException {
		boolean hasDelta = false;

		List<BeFieldDelta> fieldDeltas = new ArrayList<BeFieldDelta>();
		BeField[] fields = type.fields;
		int n = fields.length;
		BeField field;
		for(int i=0; i<n; i++) {

			try {
				if((field = fields[i]).hasDiff(previous, object)) {
					if(!hasDelta) {
						hasDelta = true;
					}
					fieldDeltas.add(field.produceDiff(object));
				}
			} 
			catch (IllegalArgumentException | IllegalAccessException e) {
				throw new BeIOException(e.getMessage());
			}
		}

		if(hasDelta) {
			branchDelta.objectDeltas.add(new UpdateBeObjectDelta(id, type, fieldDeltas));
		}
	}

	private void publishRemove(String id) {
		branchDelta.objectDeltas.add(new RemoveBeObjectDelta(id));
	}
	
	
	/**
	 * 
	 * @return
	 */
	public BeBranchDelta pullDelta() {
		BeBranchDelta delta  = branchDelta;
		branchDelta = null;
		return delta;
	}
	
	
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws BeIOException
	 */
	public BeObject get(String id) throws BeIOException {
		BeObject origin = table.objects.get(id);
		
		
		if(origin != null) {
			BeType type = codebase.getType(origin);
			
			BeObject object = type.deepClone(origin);
			
			return object;
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * 
	 * @param consumer
	 */
	public void forEach(Consumer<BeObject> consumer) {
		table.objects.forEach((key, object) -> {
		
			BeType type = codebase.getType(object);
			
			try {
				consumer.accept(type.deepClone(object));
			} 
			catch (BeIOException e) {
				e.printStackTrace();
			}
		});
	}
	
	
	public Set<String> getKeySet(){
		return table.objects.keySet();
	}

}
