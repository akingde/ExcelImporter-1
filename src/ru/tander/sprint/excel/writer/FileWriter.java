package ru.tander.sprint.excel.writer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;


public class FileWriter extends XmlParserWriter{

	private String fileName;
	
	
	public FileWriter setOutputFileName(String fileName){
		this.fileName = fileName;
		return this;
	}
	
	@Override
	public void flush() throws Exception{
		super.flush();
		saveToFile(this.asString(), this.fileName);		
	}
	
	
	// вывод в файл на диске
	public FileWriter saveToFile(String xml, String fileName)
			throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName), "UTF-8"));
		try {
			out.write(xml);
		} finally {
			out.close();
		}
		return this;
	}
	
}
