package my.lq.xlsimp.excel.test;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import my.lq.xlsimp.excel.parser.Parser;
import my.lq.xlsimp.excel.processor.ProcessorFactory;
import my.lq.xlsimp.excel.writer.XmlParserWriter;

class XmlOutputTest{
	
	public static void main(String[] args) throws Exception {
		String fileName = "./files/input/input1.xls";
		InputStream istr = new FileInputStream(new File(fileName));
		
		/*
		 * Этот xml-вывод затем можно сохранить в файл и открывать в Excel.
		 */
		
		XmlParserWriter xmlParserWriter = new XmlParserWriter();
		new Parser()
		.setMinRow(1)
		.setMaxColumn(5)
			.setParserWriter(
					xmlParserWriter)
			.setProcessor(
					ProcessorFactory.create(fileName)  //or new XlsProcessor() or new XslxProcessor()
					.setInputStream(istr)
			)
			.transform();
		System.out.println(xmlParserWriter.asString());
	}
	
}