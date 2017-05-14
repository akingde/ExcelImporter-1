package my.lq.xlsimp.excel.writer;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;


public class XmlParserWriter implements IParserWriter{

	
	private XMLStreamWriter xmlStreamWriter;
	private int rowIndex = 1;
	private StringWriter stringWriter; 
	private int recordsWritten;
	
	@Override
	public void init() throws Exception{
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		this.stringWriter = new StringWriter();
	    this.xmlStreamWriter = factory.createXMLStreamWriter(this.stringWriter);
		
//		this.xmlStreamWriter = factory.createXMLStreamWriter(out);
		
        this.xmlStreamWriter.writeStartDocument("1.0");
	    this.xmlStreamWriter.writeStartElement("workbook");
	    this.xmlStreamWriter.writeStartElement("sheet");
		this.xmlStreamWriter.writeAttribute("num", String.valueOf(0));
		this.xmlStreamWriter.writeAttribute("name", String.valueOf("Sheet"));	
	}
	
	@Override
	public void writeRow(List<String> values) throws Exception {
  	    this.xmlStreamWriter.writeStartElement("row");
		this.xmlStreamWriter.writeAttribute("num", String.valueOf(this.rowIndex++));
		Iterator<String> iter = values.iterator();
		int counter = 1;
		while(iter.hasNext()){
			String value = iter.next();
			this.xmlStreamWriter.writeStartElement("cell");
			this.xmlStreamWriter.writeAttribute("num", String.valueOf(counter));
			this.xmlStreamWriter.writeCharacters(value);
			this.xmlStreamWriter.writeEndElement();
			counter ++;
		}
		this.xmlStreamWriter.writeEndElement();
		this.recordsWritten ++;
	}

	@Override
	public void flush() throws Exception{
		this.xmlStreamWriter.writeEndElement();
		this.xmlStreamWriter.writeEndElement();
		this.xmlStreamWriter.writeEndDocument();
		this.xmlStreamWriter.flush();
		this.xmlStreamWriter.close();
	}
	
	@Override
	public String asString(){
		return this.stringWriter.getBuffer().toString();
	}

	@Override
	public int getRecordsWritten() throws Exception {
		// TODO Auto-generated method stub
		return this.recordsWritten;
	}
	
	
}
