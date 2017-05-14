package my.lq.xlsimp.excel.processor;

import java.io.File;
import java.io.InputStream;

import my.lq.xlsimp.excel.parser.IParser;


public interface IProcessor {

	public IProcessor setParser(IParser parser);
	public IProcessor setInputStream(InputStream in);
	public IProcessor setFileName(String fileName);
	
	public IProcessor transform() throws Exception;
}
