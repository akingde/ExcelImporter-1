package my.lq.xlsimp.excel.counter;

import org.jooq.Field;
import org.jooq.impl.DSL;

public class SimpleCounter implements ICounter {

	private static int counter = 0;
	private  Field<Object> counterField;

	public SimpleCounter(String fieldName){
		this.counterField =  DSL.field(fieldName);
	}
	
	@Override
	public Object getNextValue() {
		return counter++;
	}
	public Field<Object> getField() {
		return this.counterField;
	}
}
