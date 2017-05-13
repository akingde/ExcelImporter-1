package ru.tander.sprint.excel.parser;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import ru.tander.sprint.excel.listener.IParserListener;
import ru.tander.sprint.excel.processor.IProcessor;
import ru.tander.sprint.excel.writer.IParserWriter;


public class Parser implements IParser {

	private Callable<Object> callback;
	private IProcessor processor;
	private IParserWriter parserWriter;
	private boolean skipEmptyRows = true;
	private List<IParserListener> parserListeners = new ArrayList<IParserListener>();
	private int maxColumn = -1;
	
	private int minRow = -1;
//	private int maxRow = -1;

	private int rowCounter = 0;
	
	public Parser addParserListener(IParserListener listener){
		this.parserListeners.add(listener);
		return this;
	}
	
	public Parser setMaxColumn(int maxColumn ){
		this.maxColumn = maxColumn;
		return this;
	}

	public Parser setMinRow(int minRow){
		this.minRow = minRow;
		return this;
	}
	
//	public Parser setMaxRow(int maxRow){
//		this.maxRow = maxRow;
//		return this;
//	}
	
	public Parser setProcessor(IProcessor processor){
		this.processor = processor;
		this.processor.setParser(this);
		return this;
	}

	
	public Parser setSkipEmptyRows(boolean skipEmptyRows){
		this.skipEmptyRows = skipEmptyRows;
		return this;
	}

	
	public Parser setParserWriter(IParserWriter parserWriter){
		this.parserWriter = parserWriter;
		return this;
	}
	
	
	public void  notifyInit() throws Exception{
		this.rowCounter = 0;
		this.parserWriter.init();
        Iterator<IParserListener> iterInit = this.parserListeners.iterator();
        while(iterInit.hasNext()){
	        IParserListener l = iterInit.next();
        	l.notifyInit();
        }
	}

	
	public void  notifyFlush() throws Exception{

		this.parserWriter.flush();
        Iterator<IParserListener> iterFlush = this.parserListeners.iterator();
        IParserListener l = null;
        while(iterFlush.hasNext()){
	        l = iterFlush.next();
        	l.notifyRowsWritten(this.parserWriter.getRecordsWritten());
        	l.notifyFinish();
        }
         
   		if(this.callback != null){
			this.callback.call();
		}

	}
	
	
	private boolean notEmptyRow(List<String> values){
		boolean r = false;
		Iterator<String> iter = values.iterator();
		while(iter.hasNext()){
			String s = iter.next();
			if(s != null && !s.isEmpty()){
				r = true;
				break;
			}
		}
		return r;
	}
	
	
	private List<String> restrict(List<String> values){
		List<String> restrictedValues = values;
		if(this.maxColumn > 0 && values.size() > this.maxColumn){
			restrictedValues = values.subList(0, this.maxColumn);
		}
		return restrictedValues;
	}
	
	public void notifyWriteRow(List<String> values) throws Exception{
		List<String> restricted = restrict(values);
		boolean allowWriteRow = !this.skipEmptyRows || notEmptyRow(values);
		allowWriteRow = allowWriteRow && (this.minRow <= 0  || (this.minRow > 0 && this.rowCounter >= this.minRow));
		if(allowWriteRow ){
			this.parserWriter.writeRow(restricted);
			Iterator<IParserListener> iterWrite = this.parserListeners.iterator();
	        while(iterWrite.hasNext()){
		        IParserListener l = iterWrite.next();
		       	l.notifyRowsWritten(this.parserWriter.getRecordsWritten());
	        }
		}
		this.rowCounter ++;
	}

	
	public String getResult() throws Exception{
		return this.parserWriter.asString();
	}

	
	@Override
	public IParser transform() throws Exception {
		this.processor.transform();
		return this;
	}

}
