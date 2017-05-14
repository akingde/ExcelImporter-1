package my.lq.xlsimp.excel.listener;

public interface IParserListener{

	public void notifyRowsWritten(int count) throws Exception; //called for every parsed row (with all parsed cell values).
	
	public void notifyInit() throws Exception;; //start of parse process
	
	public void notifyFinish() throws Exception; //designated end of parse process
	
}
