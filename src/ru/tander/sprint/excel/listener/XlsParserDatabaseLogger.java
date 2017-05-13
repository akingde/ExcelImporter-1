package ru.tander.sprint.excel.listener;

import java.sql.Connection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

import ru.tander.sprint.excel.counter.SequenceCounter;

public class XlsParserDatabaseLogger implements IParserListener{
	private static final long UNDEFINED_VALUE = -9999;
	private Connection con = null;

	private String schemaName = "UNICORE";
	private String logTableName = "T_DB_EXPORT_LOG";
	private String sequenceName = "SOBJ";
	private String idFieldName = "DFOBJ";
	private String userFieldName = "T_OLAP_USER";
	private String roleFieldName = "T_OLAP_ROLE";
	private String countFieldName = "DFCOUNT";
	private String lastDateTimeFieldName = "DFLASTUPDATETIME";
	private String startDateTimeFieldName = "DFSTARTDATETIME";
	private String completedFieldName = "DFCOMPLETED";
	private String importTableFieldName = "DFIMPORTTABLENAME";
	private String importTableName;
			
	private long _idUser = UNDEFINED_VALUE;
	private long _idRole = UNDEFINED_VALUE;
	
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
		this._idUser = value;
		return this;
	}


	public	XlsParserDatabaseLogger setRole(long value){
		this._idRole = value;
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
		if(this._idUser == UNDEFINED_VALUE ){
			String userExpression = "SELECT unicore.pack_context.get_context('PFUSER') FROM DUAL";
			Record userRec= this.dslContext.fetchOne(userExpression);
			String userRaw = String.valueOf(userRec.getValue(0));
			this._idUser = userRaw!=null&&!"null".equalsIgnoreCase(userRaw)?Long.parseLong(userRaw):-1;
		}
		return this._idUser;
	}
	
	private long getIdRole(){
		if(this._idRole == UNDEFINED_VALUE  ){
			String roleExpression = "SELECT unicore.pack_context.get_context('PFROLE') FROM DUAL";
			
			Record roleRec = this.dslContext.fetchOne(roleExpression );
			String roleRaw = String.valueOf(roleRec.getValue(0));
			this._idRole = roleRaw!=null&&!"null".equalsIgnoreCase(roleRaw)?Long.parseLong(roleRaw):-1;
		}
		return this._idRole;
		
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
			throw new NullPointerException("Необходимо передать идентификатор соединения в database logger (setConnection)");
		}
		if(this.importTableName == null){
			throw new NullPointerException("Необходимо указать имя таблицы для идентификации в логах импорта(setImportTableName)");
		}
		if(this.importTableFieldName == null){
			throw new NullPointerException("Необходимо указать имя столбца таблицы логов, где будет сохранён идентификатор таблицы импорта (setImportTableFieldName)");
		}
		if(this.completedFieldName == null){
			throw new NullPointerException("Необходимо указать имя столбца типа Varchar2, где будет сохранено значение T по завершению импорта (setCompletedFieldName)");
		}

		if(this.schemaName == null){
			throw new NullPointerException("Необходимо указать схему (setSchemaName)");
		}

		if(this.logTableName == null){
			throw new NullPointerException("Необходимо указать имя таблицы логов импорта (setLogTableName)");
		}

		if(this.sequenceName == null){
			throw new NullPointerException("Необходимо указать имя последовательности (setSequenceName)");
		}

		if(this.countFieldName == null){
			throw new NullPointerException("Необходимо имя столбца где будет храниться количество (setCountFieldName)");
		}
		if(this.lastDateTimeFieldName == null){
			throw new NullPointerException("Необходимо имя столбца где будет храниться время обновления (setLastDateTimeFieldName)");
		}
		if(this.startDateTimeFieldName == null){
			throw new NullPointerException("Необходимо имя столбца где будет храниться время начала импорта (setStartDateTimeFieldName)");
		}
	}
	
	@Override
	public void notifyInit() throws Exception {
		validate();
		
		this.dslContext = DSL.using(this.con, SQLDialect.ORACLE);
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
