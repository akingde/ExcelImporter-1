package ru.tander.sprint.excel.listener;

import java.sql.CallableStatement;
import java.sql.Connection;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class UserMessageParseEndNotifier implements IParserListener{
	private static final long UNDEFINED_VALUE = -9999;
	private Connection con = null;

	private String userMessage = "Импорта правил завершён. Импортировано {{count}} записей.";
	private String msgTitle = "Импорт завершён";
	
	private int rowsInserted = 0;
	
	private long _idUser = UNDEFINED_VALUE;
	private long _idRole = UNDEFINED_VALUE;
	
	private DSLContext dslContext;


	private DSLContext getDslContext(){
		if(this.dslContext == null){
			this.dslContext = DSL.using(this.con, SQLDialect.ORACLE);
		}
		return this.dslContext;
	}
	
	public	UserMessageParseEndNotifier setConnection(Connection value){
		this.con = value;
		return this;
	}

	private String getUserMessage(){
		if(this.userMessage == null){
			this.userMessage = "<<импорт завершён>>";
		}
		return this.userMessage;

	}

	public	UserMessageParseEndNotifier setMessageTitle(String value){
		this.msgTitle = value;
		return this;
	}
	
	public	UserMessageParseEndNotifier setUserMessage(String value){
		this.userMessage = value;
		return this;
	}

	public	UserMessageParseEndNotifier setUser(long value){
		this._idUser = value;
		return this;
	}


	public	UserMessageParseEndNotifier setRole(long value){
		this._idRole = value;
		return this;
	}

	private long getIdUser(){
		if(this._idUser == UNDEFINED_VALUE ){
			String userExpression = "SELECT unicore.pack_context.get_context('PFUSER') FROM DUAL";
			Record userRec= getDslContext().fetchOne(userExpression);
			String userRaw = String.valueOf(userRec.getValue(0));
			this._idUser = userRaw!=null&&!"null".equalsIgnoreCase(userRaw)?Long.parseLong(userRaw):-1;
		}
		return this._idUser;
	}
	
	private long getIdRole(){
		if(this._idRole == UNDEFINED_VALUE  ){
			String roleExpression = "SELECT unicore.pack_context.get_context('PFROLE') FROM DUAL";
			
			Record roleRec = getDslContext().fetchOne(roleExpression );
			String roleRaw = String.valueOf(roleRec.getValue(0));
			this._idRole = roleRaw!=null&&!"null".equalsIgnoreCase(roleRaw)?Long.parseLong(roleRaw):-1;
		}
		return this._idRole;
		
	}
	
	
	@Override
	public void notifyRowsWritten(int count) throws Exception {
		// TODO Auto-generated method stub
		this.rowsInserted = count;
	}

	
	@Override
	public void notifyInit() throws Exception {
		if(con == null){
			throw new NullPointerException("Необходимо передать идентификатор соединения (setConnection(...))");
		}

		
	}

	@Override
	public void notifyFinish() throws Exception {
		// TODO Auto-generated method stub
		//log end of processing		
		String msg = this.getUserMessage().replace("{{count}}", String.valueOf(this.rowsInserted));
		String userExpression = "DECLARE r VARCHAR2(1000); BEGIN  r := unicore.pack_olap_func.add_user_message(? ,?,?,?, 0, null, null); END; ";
  	    CallableStatement cs = this.con.prepareCall(userExpression);
        cs.setLong(1, this.getIdUser());
        cs.setLong(2, this.getIdRole());
        cs.setString(3, msgTitle);
        cs.setString(4, msg);
        cs.execute();
		cs.close();
	}
	
}
