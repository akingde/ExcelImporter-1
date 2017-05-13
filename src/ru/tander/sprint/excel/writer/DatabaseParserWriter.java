package ru.tander.sprint.excel.writer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertSetStep;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

import ru.tander.sprint.excel.counter.ICounter;
import ru.tander.sprint.excel.counter.SimpleCounter;
import ru.tander.sprint.excel.exception.XlsParserException;

public class DatabaseParserWriter  implements IParserWriter{
	

	private Connection connection;
	private Map<Integer, String> columnTransformationRules = new HashMap<Integer, String>();
	private String tableName;
	private String schemaName;
	private Table<Record> table;
	private DSLContext dslContext;
	private int batchSize = 1000;
	private int recordIndex = 0;
	private int recordsWritten = 0;
	private boolean skipEmptyDataRows = true;
	
	private Map<String, String> staticFields = new HashMap<String, String>();

	private Map<String, String> staticValues = new HashMap<String, String>();
	
	private ICounter counter;

	private Collection<Query> bulk;
	
	private DSLContext getDslContext(){
		if(this.dslContext == null){
			this.dslContext = DSL.using(this.connection, SQLDialect.MYSQL);
		}
		return this.dslContext;
	}
	

	public DatabaseParserWriter addRule(Integer index, String fieldName){
		this.columnTransformationRules.put(index, fieldName);
		return this;
	}

	
	public DatabaseParserWriter setSkipEmptyDataRows(boolean skip){
		this.skipEmptyDataRows = skip;
		return this;
	}

	private void prepareStatics(){
		if(this.staticFields.size() > 0){
			Iterator<String> iter = this.staticFields.keySet().iterator();
			while(iter.hasNext()){
				String key = iter.next();
				String expression = this.staticFields.get(key);
				Record rec = getDslContext().fetchOne(expression);
				String value = String.valueOf(rec.getValue(0));
				this.staticValues.put(key, value);
			}
		}
	}
	
	public DatabaseParserWriter setConnection(Connection con){
		if(con != null){
			this.connection = con;
		} else {
			throw new NullPointerException("Connection must be defined when initialize DatabaseParserWriter");		
		}
		return this;
	}

	public DatabaseParserWriter addStatic(String fieldName, String fieldExpression){
		this.staticFields.put(fieldName, fieldExpression);
		return this;
	}
	
	public DatabaseParserWriter setCounter(ICounter counter){
		this.counter = counter;
		return this;
	}
	
	public DatabaseParserWriter setBatchSize(int batchSize){
		this.batchSize = batchSize;
		return this;
	}
	
	public DatabaseParserWriter setColumnTransformationRules(Map<Integer, String> rules){
		this.columnTransformationRules = rules;
		return this;
	}
	
	public DatabaseParserWriter setTableName(String tableName){
		this.tableName = tableName;
		return this;
	}

	public DatabaseParserWriter setSchemaName(String schemaName){
		this.schemaName = schemaName;
		return this;
	}

	
		@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		//this.dslContext = DSL.using(connection, SQLDialect.ORACLE);
		this.checkRulesExists();
		
		this.table = DSL.tableByName(this.schemaName, this.tableName);
		this.bulk = new ArrayList<Query>();
		prepareStatics();
			
	}

	
	private ICounter getCounter(){
		if(this.counter != null){
			return this.counter;
		} else {
			return new SimpleCounter("DFOBJ");
		}
	}
	
		
	private void checkRulesExists() throws XlsParserException{
		if(this.columnTransformationRules == null 
		|| this.columnTransformationRules.size() == 0){
			throw new XlsParserException("Please specify column parsing rules");
		}
	}
	
	
	@Override
	public void writeRow(List<String> values) throws Exception {
	// TODO Auto-generated method stub
	// Fetch a SQL string from a jOOQ Query in order to manually execute it with another tool.
		Set<Integer> keys = this.columnTransformationRules.keySet();
		
		InsertSetStep<Record> iss = getDslContext().insertInto(this.table);
		HashMap <Field<?>, Object> fieldValues = new HashMap<Field<?>, Object>();
		boolean hasValues = false;
		for(Integer i: keys){
			if(i < values.size()){
				String fieldName = this.columnTransformationRules.get(i);
				Field<Object> field = DSL.fieldByName(fieldName);
				String value = values.get(i);
				if(value != null && !value.isEmpty()){
					fieldValues.put(field,value);
					hasValues = true;
				}
			}
		}
		if(hasValues || !this.skipEmptyDataRows){ 
			if(this.staticValues.size() > 0){
				Iterator<String> iter = this.staticValues.keySet().iterator();
				while(iter.hasNext()){
					String key = iter.next();
					String value = this.staticValues.get(key);
					if(!"null".equalsIgnoreCase(value)){
						Field<Object> field = DSL.fieldByName(key);
						fieldValues.put(field, value);
						hasValues = true;
					}
				}
			}
			
			if(hasValues){
				ICounter counter = this.getCounter();
				if(counter != null){
					fieldValues.put(counter.getField(), counter.getNextValue());
				}
				this.bulk.add(iss.set(fieldValues));
				
				this.recordIndex ++;
				
				if(this.recordIndex % this.batchSize == 0){
					batchInsert();
				}
			}
		}
	}

	public int getRecordsWritten(){
		return this.recordsWritten;
	}
	
	
	private void batchInsert(){
		int [] counts =  getDslContext().batch(this.bulk).execute();
		for(int i = 0; i < counts.length; i++){
			this.recordsWritten += counts[i];
		}
		this.bulk.clear();
	}
	
	
	@Override
	public void flush() throws Exception {
		batchInsert();
		
	}

	@Override
	public String asString() throws Exception {
		throw new NullPointerException("Method asString don't defined for "+this.getClass().toString());
	}
	

}
