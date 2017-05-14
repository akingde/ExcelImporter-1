package my.lq.xlsimp.excel.listener;

import java.sql.Connection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

import my.lq.xlsimp.excel.counter.SequenceCounter;

public class XlsParserDatabaseLogger implements IParserListener{
	private static final long UNDEFINED_VALUE = -9999;
	private Connection con = null;

	private String schemaName = "MY_SCHEMA";
	private String logTableName = "EXPORT_TABLE";
	private String sequenceName = "SEQ";
	private String idFieldName = "ID";
	private String userFieldName = "USER";
	private String roleFieldName = "TAG";
	private String countFieldName = "DFCOUNT";
	private String lastDateTimeFieldName = "DFLASTUPDATETIME";
	private String startDateTimeFieldName = "DFSTARTDATETIME";
	private String completedFieldName = "DFCOMPLETED";
	private String importTableFieldName = "DFIMPORTTABLENAME";
	private String importTableName;
			
	private long _user = UNDEFINED_VALUE;
	private long _role = UNDEFINED_VALUE;
	
	private long currentId = -1;
	
	private DSLContext dslContext;
	private Table<?> table;
	private int rowsInserted = 0;
	
	private int updateInterval = 1000; //every 1000 rows update time and count

	
	public	XlsParserDatabaseLogger setConnection(Connection value){
		this.con = value;
		return this;
	}


	public	XlsParserDatabaseLogger setImportTableFieldName(String value){
		this.importTableFieldName = value;
		return this;
	}

	public	XlsParserDatabaseLogger setIdFieldName(String value){
		this.idFieldName = value;
		return this;
	}

	public	XlsParserDatabaseLogger setUser(long value){
		this._user = value;
		return this;
	}


	public	XlsParserDatabaseLogger setRole(long value){
		this._role = value;
		return this;
	}


	
	public	XlsParserDatabaseLogger setUserFieldName(String value){
		this.userFieldName = value;
		return this;
	}

	public	XlsParserDatabaseLogger setRoleFieldName(String value){
		this.roleFieldName = value;
		return this;
	}

	public	XlsParserDatabaseLogger setSequenceName(String value){
		this.sequenceName = value;
		return this;
	}

	public	XlsParserDatabaseLogger setLastDateTimeFieldName(String value){
		this.lastDateTimeFieldName = value;
		return this;
	}

	public	XlsParserDatabaseLogger setCompletedFieldName(String value){
		this.completedFieldName = value;
		return this;
	}


	public	XlsParserDatabaseLogger setStartDateTimeFieldName(String value){
		this.startDateTimeFieldName = value;
		return this;
	}

	public	XlsParserDatabaseLogger setCountFieldName(String value){
		this.countFieldName = value;
		return this;
	}


	public	XlsParserDatabaseLogger setSchemaName(String schemaName){
		this.schemaName = schemaName;
		return this;
	}

	
	public	XlsParserDatabaseLogger setLogTableName(String tableName){
		this.logTableName  = tableName;
		return this;
	}

	private long getIdUser(){
		if(this._user == UNDEFINED_VALUE ){
			String userExpression = "SELECT unicore.pack_context.get_context('PFUSER') FROM DUAL";
			Record userRec= this.dslContext.fetchOne(userExpression);
			String userRaw = String.valueOf(userRec.getValue(0));
			this._user = userRaw!=null&&!"null".equalsIgnoreCase(userRaw)?Long.parseLong(userRaw):-1;
		}
		return this._user;
	}
	
	private long getIdRole(){
		if(this._role == UNDEFINED_VALUE  ){
			String roleExpression = "SELECT unicore.pack_context.get_context('PFROLE') FROM DUAL";
			
			Record roleRec = this.dslContext.fetchOne(roleExpression );
			String roleRaw = String.valueOf(roleRec.getValue(0));
			this._role = roleRaw!=null&&!"null".equalsIgnoreCase(roleRaw)?Long.parseLong(roleRaw):-1;
		}
		return this._role;
		
	}
	
	public	XlsParserDatabaseLogger setImportTableName(String importTableName){
		this.importTableName = importTableName;
		return this;
	}
	
	@Override
	public void notifyRowsWritten(int count) throws Exception {
		// TODO Auto-generated method stub
		if(this.rowsInserted % this.updateInterval == 0){
			updateDateAndCount();
		}
		this.rowsInserted = count;
	}

	private void updateDateAndCount(){
		
		this.dslContext
			.update(this.table)
			.set(DSL.fieldByName(this.lastDateTimeFieldName), DSL.currentDate())
			.set(DSL.fieldByName(this.countFieldName), this.rowsInserted)
			.set(DSL.fieldByName(this.userFieldName), getIdUser())
			.set(DSL.fieldByName(this.roleFieldName), getIdRole())
			.where(DSL.fieldByName(this.idFieldName).equal(this.currentId) )
				.execute();
	}
	
	private void validate(){
		if(this.con == null){
			throw new NullPointerException("Connection is null for database logger (setConnection)");
		}
		if(this.importTableName == null){
			throw new NullPointerException("Please specify importTableName parameter (setImportTableName)");
		}
		if(this.importTableFieldName == null){
			throw new NullPointerException("Please specify importTableFieldName ");
		}
		if(this.completedFieldName == null){
			throw new NullPointerException("Please set completedFieldName");
		}

		if(this.schemaName == null){
			throw new NullPointerException("Please set schemaName)");
		}

		if(this.logTableName == null){
			throw new NullPointerException("Please set logTableName");
		}

		if(this.sequenceName == null){
			throw new NullPointerException("setSequenceName");
		}

		if(this.countFieldName == null){
			throw new NullPointerException("setCountFieldName");
		}
		if(this.lastDateTimeFieldName == null){
			throw new NullPointerException("setLastDateTimeFieldName");
		}
		if(this.startDateTimeFieldName == null){
			throw new NullPointerException("setStartDateTimeFieldName");
		}
	}
	
	@Override
	public void notifyInit() throws Exception {
		validate();
		
		this.dslContext = DSL.using(this.con, SQLDialect.MYSQL);
		this.table = DSL.tableByName(this.schemaName, this.logTableName );

		// TODO Auto-generated method stub
		//log start of processing
		this.rowsInserted = 0;
		
//		String seqExpression = "SELECT "+this.schemaName+"."+this.sequenceName +".NEXTVAL FROM DUAL";
//		Record seqRec = this.dslContext.fetchOne(seqExpression );
//		this.currentId = Integer.parseInt(String.valueOf(seqRec .getValue(0)));
		

		SequenceCounter counter = new SequenceCounter(this.countFieldName, this.schemaName, this.sequenceName);
		Field<Number> f = counter.getNextValue();
		Record nextval = dslContext.select(f).from(DSL.tableByName("DUAL")).fetchOne();
		this.currentId = Long.parseLong(String.valueOf(nextval.getValue(0)));

		this.dslContext
			.insertInto(this.table)
			.set(DSL.fieldByName(this.idFieldName), currentId)
			.set(DSL.fieldByName(this.userFieldName), this.getIdUser())
			.set(DSL.fieldByName(this.roleFieldName), this.getIdRole())
			.set(DSL.fieldByName(this.importTableFieldName), this.importTableName)
			.set(DSL.fieldByName(this.lastDateTimeFieldName), DSL.currentTimestamp())
			.set(DSL.fieldByName(this.startDateTimeFieldName), DSL.currentTimestamp())
			.execute();
		
	}

	@Override
	public void notifyFinish() throws Exception {
		// TODO Auto-generated method stub
		//log end of processing		
		updateDateAndCount();
				
		logFinalize();
	}

	private void logFinalize(){
			this.dslContext
			.update(this.table)
			.set(DSL.fieldByName(this.completedFieldName), "T")
			.where(DSL.fieldByName(this.idFieldName).equal(this.currentId) )
				.execute();

	}
	
}
