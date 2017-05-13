package ru.tander.sprint.excel.counter;

import java.math.BigDecimal;

import org.jooq.Field;
import org.jooq.Sequence;
import org.jooq.impl.DSL;
import org.jooq.impl.SequenceImpl;
import org.jooq.util.oracle.OracleDataType;


public class SequenceCounter implements ICounter{
	private  Sequence<Number> seq;
	private  Field<BigDecimal> counterField;
	
	public SequenceCounter(String fieldName, String schemaName, String sequenceName){

		//this.seq = new SequenceImpl(sequenceName, schema, OracleDataType.NUMBER);
		this.counterField =  DSL.fieldByName(OracleDataType.NUMBER, fieldName);
	    this.seq = new SequenceImpl(sequenceName, DSL.schemaByName(schemaName), OracleDataType.NUMBER);
	}
	
	@Override
	public Field<Number> getNextValue() {
		// TODO Auto-generated method stub
		return this.seq.nextval();
	}
	public Field<BigDecimal> getField() {
		// TODO Auto-generated method stub
		return this.counterField;
	}
}
