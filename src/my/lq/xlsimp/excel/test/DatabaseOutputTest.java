package my.lq.xlsimp.excel.test;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;

import my.lq.xlsimp.excel.counter.SimpleCounter;
import my.lq.xlsimp.excel.parser.Parser;
import my.lq.xlsimp.excel.processor.ProcessorFactory;
import my.lq.xlsimp.excel.writer.DatabaseParserWriter;

class DatabaseOutputTest{
	
	public static void main(String[] args) throws Exception {
		Connection connection = TestDbExport.getConnection();
		String fileName = "./files/input/input1.xls";
		InputStream istr = new FileInputStream(new File(fileName));
		
		new Parser()
			.setMinRow(1)
			.setMaxColumn(5)
				.setParserWriter(
						new DatabaseParserWriter()
								.setConnection(connection)
								.setSchemaName("XLSIMPTEST")
								.setTableName("IMP1")
								.setCounter(
										new SimpleCounter("ID"))
								.addRule(0, "COL1")
								.addRule(1, "COL2")
								.addRule(2, "COL3")
								.addRule(3, "COL4")
								)
				
				.setProcessor(
						ProcessorFactory.create(fileName)  //or new XlsProcessor() or new XslxProcessor()
						.setInputStream(istr)
				)
				.transform();
		
	}
	
}