package my.lq.xlsimp.excel.counter;

import org.jooq.Field;
import org.jooq.Sequence;
import org.jooq.impl.DSL;
import org.jooq.impl.SequenceImpl;
import org.jooq.util.mysql.MySQLDataType;


public class SequenceCounter implements ICounter{
	private  Sequence<Number> seq;
	private  Field<Object> counterField;
	
	public SequenceCounter(String fieldName, String schemaName, String sequenceName){

		//this.seq = new SequenceImpl(sequenceName, schema, OracleDataType.NUMBER);
		this.counterField =  DSL.field(fieldName);
	    this.seq = new SequenceImpl(sequenceName, DSL.schemaByName(schemaName), MySQLDataType.BIGINT);
	}
	
	@Override
	public Field<Number> getNextValue() {
		// TODO Auto-generated method stub
		return this.seq.nextval();
	}
	public Field<Object> getField() {
		// TODO Auto-generated method stub
		return this.counterField;
	}
}
