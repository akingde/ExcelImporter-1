package ru.tander.sprint.excel.parser;

import java.util.List;

public interface IParser {
	public void  notifyInit() throws Exception;

	public void  notifyFlush() throws Exception;
	
	public void notifyWriteRow(List<String> values) throws Exception;
	
	public IParser transform() throws Exception;
	
}
