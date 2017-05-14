package my.lq.xlsimp.excel.test;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import my.lq.xlsimp.excel.parser.Parser;
import my.lq.xlsimp.excel.processor.ProcessorFactory;
import my.lq.xlsimp.excel.writer.FileWriter;

class FileOutputTest{
	
	public static void main(String[] args) throws Exception {
		String fileName = "./files/input/input1.xls";
		InputStream istr = new FileInputStream(new File(fileName));
		
		new Parser()
		.setMinRow(1)
		.setMaxColumn(5)
			.setParserWriter(
					new FileWriter().setOutputFileName("./files/output/output.xml"))
			.setProcessor(
					ProcessorFactory.create(fileName)  
					.setInputStream(istr)
			)
			.transform();
		
	}
	
}