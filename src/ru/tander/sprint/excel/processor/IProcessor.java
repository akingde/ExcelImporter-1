package ru.tander.sprint.excel.processor;

import java.io.File;
import java.io.InputStream;

import ru.tander.sprint.excel.parser.IParser;


public interface IProcessor {

	public IProcessor setParser(IParser parser);
	public IProcessor setInputStream(InputStream in);
	public IProcessor setFileName(String fileName);
	
	public IProcessor transform() throws Exception;
}
