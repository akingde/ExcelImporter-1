package my.lq.xlsimp.excel.processor;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.hssf.eventusermodel.AbortableHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.HSSFUserException;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import my.lq.xlsimp.excel.exception.XlsParserException;
import my.lq.xlsimp.excel.listener.FTHSSFListener;
import my.lq.xlsimp.excel.parser.IParser;

public class XlsProcessor extends AbortableHSSFListener implements IProcessor  {

	private ArrayList<String> rowCells;
	private HSSFRequest req;
	private FTHSSFListener ft;
	private SSTRecord sstrec;
	private boolean halt = false;
	private HSSFEventFactory factory;
	private InputStream  din;
	private int prevRowIndex = -1;
	private int prevColumnIndex = 1;
	private int maxColumnIndex = 0;
	private boolean rowOpened = false;
	private int sheetCounter = 0;
	private boolean skipEmptyCells;
	private int columnRangeHigh = -1;
	private int rowRangeLow = -1;
	private int rowRangeHigh =  -1;
	private File f;
	
	private String dateFormat = "dd.MM.yyyy HH:mm:ss";
	
	private InputStream inputStream;

	private IParser parser;
	
	String fileName;
	
	public IProcessor setInputStream(InputStream in){
		this.inputStream = in;
		return this;
	}

	public IProcessor setFileName(String fileName){
		this.fileName = fileName;
		return this;
	}


	public IProcessor setParser(IParser parser){  //crosslink! danger
		this.parser = parser;
		return this;
	}
	
	
	
	public XlsProcessor setDateFormat(String dateFormat){  //crosslink! danger
		this.dateFormat = dateFormat;
		return this;
	}
	
	private void checkOpenRow() throws XMLStreamException{
	  int newRowIndex = this.prevRowIndex + 1;
	  if(this.allowAddRow(newRowIndex)){
		_openRow(newRowIndex);
	  }
	}

	
	private void _openRow(int rowIndex) throws XMLStreamException{
		  this.rowCells.clear();
		  this.rowOpened = true;
	}

	
	private void checkCloseRow(int columnIndex, int rowIndex) throws Exception{
	    if(this.allowAddRow(rowIndex)){
			if(rowIndex != this.prevRowIndex && this.prevRowIndex >= 0 ){
				_closeRow();
			}
	    }
	}
	

	
	private void _closeRow() throws Exception{
		//String[] values = rowCells.toArray(new String[0]);
		if(this.rowCells != null && this.rowCells.size() > 0){
			this.parser.notifyWriteRow(this.rowCells);
		}
		this.rowOpened = false;
	}
	
		
	private boolean allowAddRow(int rowIndex){
		boolean allow = 
			((this.rowRangeLow >= 0 && (rowIndex >= this.rowRangeLow))|| this.rowRangeLow < 0)
			&&
			((this.rowRangeHigh >= 0 && (rowIndex <= this.rowRangeHigh )) || this.rowRangeHigh < 0);
		return	allow;
	}

	private boolean allowAddColumn(int columnIndex){
		return	this.columnRangeHigh <= 0 || 
			   (this.columnRangeHigh >= 0 && columnIndex < columnRangeHigh);
	}

	
	private void appendCellAfter(int columnIndex, int rowIndex,  String data) throws XMLStreamException, XlsParserException{
			
		if(prevColumnIndex > columnIndex && rowIndex == this.prevRowIndex){
			throw new XlsParserException("Index error" + "col: "+this.prevColumnIndex + " " + columnIndex + ", row: "+ this.prevRowIndex +" " + rowIndex);
		}
		if( rowIndex < this.prevRowIndex){
			throw new XlsParserException("Index error");
		}

		int nextColumnIndex = columnIndex + 1;
		
		if(this.maxColumnIndex < nextColumnIndex 
			&& (nextColumnIndex <= this.columnRangeHigh 
					|| this.columnRangeHigh < 0)) {
			this.maxColumnIndex = nextColumnIndex;
		}

		boolean allowAdd = 
			true
			&&   this.allowAddRow(rowIndex) 	
			&& this.allowAddColumn(columnIndex)
			;

		if(allowAdd){
			_writeCell(nextColumnIndex, data);
		}

		this.prevColumnIndex = nextColumnIndex;
	    this.prevRowIndex = rowIndex;
	}
	
	private void _writeCell(int columnIndex, String value) throws XMLStreamException{
		this.rowCells.add(value);
	}
	
	private void fillEmptyCellsBefore(int currentColumnIndex, int currentRowIndex) throws XMLStreamException, XlsParserException{
		if(!this.skipEmptyCells){
			int lastColumnNumber = this.prevColumnIndex; //fixing column number, it will be changed
			if(currentRowIndex > this.prevRowIndex){
				lastColumnNumber = 0;
			}
			for(int i = lastColumnNumber; i < currentColumnIndex; i ++){ //fill empty cells 
				appendCellAfter(i, currentRowIndex, "");	
			}
		}
	}
	
	
	private void fixLastEmptyCells(int currentColumnIndex, int currentRowIndex) throws XMLStreamException, XlsParserException{

		if(!this.skipEmptyCells){
			while (this.prevColumnIndex < this.maxColumnIndex){
	         //  	System.out.println("fix "+ this.prevCellColumnNumber);
				appendCellAfter(this.prevColumnIndex, this.prevRowIndex, "");
			}
		}
	}

	
	public XlsProcessor setRowRangeLow(Integer value){
		this.rowRangeLow = value;
		return this;
	}


	public XlsProcessor  setMaxColumnLimit(int count){
		this.columnRangeHigh = count;
		return this;
	}
	
	public XlsProcessor  setMaxColumnLimit(Integer count){
		this.columnRangeHigh = count == null? -1: count.intValue();
		return this;
	}

	public XlsProcessor  setRowRangeHigh(int count){
		this.rowRangeHigh = count;
		return this;
	}
	
	public XlsProcessor  setRowRangeHigh(Integer count){
		this.rowRangeHigh = count == null? -1: count.intValue();
		return this;
	}

	private void fixCloseLastRow() throws Exception{
        if(this.rowOpened){
        	this._closeRow();
        }
	}

	
	public XlsProcessor  setSkipEmptyCells(boolean b){
		this.skipEmptyCells = b;
		return this;
	}
	
	
	
	private void initTransformation() throws Exception{
		
		
		if(this.inputStream == null){
			if(this.fileName != null){
				this.inputStream = new FileInputStream(this.fileName);
			}
			if(this.inputStream == null){
				throw new NullPointerException("������� ����� ���������� ������� xlsParserWriter.setInputStream(...)");
			}
		}

     	this.rowCells = new ArrayList<String>();

 		//this.ft =  new FormatTrackingHSSFListener(this);
	 	
        // create a new org.apache.poi.poifs.filesystem.Filesystem
        POIFSFileSystem poifs = new POIFSFileSystem(this.inputStream);
        // get the Workbook (excel part) stream in a InputStream
        this.din = poifs.createDocumentInputStream("Workbook");

//        Assert.assertNotNull(din);
        // construct out HSSFRequest object
        this.req = new HSSFRequest();
//        Assert.assertNotNull(req);


        // lazy listen for ALL records with the listener shown above
        this.req.addListenerForAllRecords(this);

        this.ft = new FTHSSFListener(this);
        if(this.dateFormat != null) {
        	this.ft.setDateFormat(this.dateFormat);
        	
        }
	    this.req.addListenerForAllRecords(this.ft);

        // create our event factory
        factory = new HSSFEventFactory();
        // process our events based on the document input stream

        
        this.parser.notifyInit();
		
	}

	
	private void doTransformation() throws Exception{
        factory.abortableProcessEvents(this.req, this.din);
	
	}
	
	
	private void finishTransformation() throws Exception{
			// once all the events are processed close our file input stream
        fixLastEmptyCells(0, this.prevRowIndex);
         
		fixCloseLastRow();        
        
        
        this.inputStream.close();
		this.inputStream = null; //������ ��� ��� �� ����������. ����� ������ ������������� inputStream
        
        // and our document input stream (don't want to leak these!)
        this.din.close();

        this.parser.notifyFlush();
	}
	
	
	public IProcessor transform () throws Exception {
 		 
		initTransformation();
		doTransformation();
		finishTransformation();

		return this;
	
	}
	
	/**
     * This method listens for incoming records and handles them as required.
     * @param record    The record that was found while reading.
     */
	@Override
	public short abortableProcessRecord(Record record) throws HSSFUserException {
    {
    	
    	if(sheetCounter > 1) {
    		halt = true;
    	}
		
        try {

        	switch (record.getSid()) {
	            case BOFRecord.sid:
	                BOFRecord bof = (BOFRecord) record;
	                if (bof.getType() == bof.TYPE_WORKBOOK)
	                {
	                } else if (bof.getType() == bof.TYPE_WORKSHEET)
	                {
	                    this.sheetCounter ++;
	                }
	                break;
	
	            case NumberRecord.sid:
	                NumberRecord numrec = (NumberRecord) record;
	                boolean needFixLastEmptyCells = numrec.getColumn() < this.prevColumnIndex 
	                							  || numrec.getRow() > this.prevRowIndex;
	                if(needFixLastEmptyCells) {//in every first column 
							fixLastEmptyCells(numrec.getColumn(), numrec.getRow());
	                }
	                checkCloseRow(numrec.getColumn(),numrec.getRow());
	                
	                boolean appendRowCondition = numrec.getRow() > this.prevRowIndex;
	                if(appendRowCondition) {
	                	checkOpenRow();
	                }
	                fillEmptyCellsBefore(numrec.getColumn(), numrec.getRow());
	                String value = String.valueOf(this.ft.formatNumberDateCell(numrec));
	               	appendCellAfter(numrec.getColumn(),numrec.getRow(),  value);
	                break;
	            case SSTRecord.sid:
	              sstrec = (SSTRecord) record;
	                break;
			 	case LabelSSTRecord.sid:
	                LabelSSTRecord lrec = (LabelSSTRecord) record;
	                String s = String.valueOf(sstrec.getString(lrec.getSSTIndex()));
	                boolean needFixLastEmptyCells2 = lrec.getColumn() < this.prevColumnIndex || lrec.getRow() > this.prevRowIndex;
	                if(needFixLastEmptyCells2) {
		                fixLastEmptyCells(lrec.getColumn(), lrec.getRow());
	                }
   	                checkCloseRow(lrec.getColumn(),lrec.getRow());
	                boolean appendRowCondition2 = lrec.getRow() > this.prevRowIndex;
	                if(appendRowCondition2) { //next row => append row
	                	checkOpenRow();
	                } 
	                fillEmptyCellsBefore(lrec.getColumn(), lrec.getRow());
	            	appendCellAfter(lrec.getColumn(),lrec.getRow(), s);
	                break;
	        	}
	        	
	        } catch (XMLStreamException e) {
				throw new HSSFUserException(ExceptionUtils.getStackTrace(e));
			} catch (XlsParserException e) {
				throw new HSSFUserException(ExceptionUtils.getStackTrace(e));
			} catch (Exception e) {
				throw new HSSFUserException(ExceptionUtils.getStackTrace(e));
			}

    	}
    
		return (short) (this.halt?-1:0);
	}
	
	



}
