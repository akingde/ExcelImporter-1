package ru.tander.sprint.excel.counter;

import java.math.BigDecimal;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.util.oracle.OracleDataType;

public class SimpleCounter implements ICounter {

	private static int counter = 0;
	private  Field<BigDecimal> counterField;

	public SimpleCounter(String fieldName){
		this.counterField =  DSL.fieldByName(OracleDataType.NUMBER, fieldName);
	}
	
	@Override
	public Object getNextValue() {
		return counter++;
	}
	public Field<BigDecimal> getField() {
		return this.counterField;
	}
}
