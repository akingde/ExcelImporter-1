package my.lq.xlsimp.excel.writer;

import java.util.List;



public interface IParserWriter {
	public String asString() throws Exception; //for output
	
	public void writeRow(List<String> values) throws Exception; //called for every parsed row (with all parsed cell values).
	
	public void init() throws Exception;; //start of parse process
	
	public void flush() throws Exception; //designated end of parse process
	
	public int getRecordsWritten() throws Exception;

}
