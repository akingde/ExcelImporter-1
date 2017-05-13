package ru.tander.sprint.excel.processor;

import java.io.File;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ru.tander.sprint.excel.parser.IParser;

public class XlsxProcessor extends DefaultHandler implements IProcessor{

	private InputStream istr;
	private IParser parser;	
	private SharedStringsTable sst;
	private StylesTable st;
	private String buf;
	private String type;
	private String style;
	private boolean isData;
	private boolean isCell;
	private int prevCell; // Последняя аписанная ячейка
	private ArrayList<String> values = new ArrayList<String>();

	private OPCPackage pkg; 
	
	int cellCounter = 0; // Первая ячейка с индексом 1

	Format dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	private String fileName;	
	
	public IProcessor setFileName(String fileName){
		this.fileName = fileName;
		return this;
	}

	
	@Override
	public IProcessor setParser(IParser parser) {
		this.parser = parser;
		return this;
	}

	@Override
	public IProcessor setInputStream(InputStream in) {
		this.istr = in;
		return this;
	}

	@Override
	public IProcessor transform() throws Exception {

		this.parser.notifyInit();

		if(istr != null) {
			this.pkg = OPCPackage.open(istr);
		}

		if(fileName != null) {
			this.pkg = OPCPackage.open(fileName);
		}
		
		writeSheet();
		
		this.parser.notifyFlush();
		
		return this;

	}

	
//	private void writeSheet(InputStream istr)
	private void writeSheet()
			throws Exception {

//		OPCPackage pkg = OPCPackage.open(istr);

		XSSFReader reader = new XSSFReader(pkg);

		XMLReader parser = XMLReaderFactory
				.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
		//System.out.println(parser.getClass());

		
		this.sst = reader.getSharedStringsTable();
		this.st = reader.getStylesTable();
		parser.setContentHandler(this);

		Iterator<InputStream> iter = reader.getSheetsData();

		InputStream s = iter.next();

		InputSource source = new InputSource(s);

		parser.parse(source);

		s.close();
	}
	

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		if ("row".equals(name)) {
			this.values.clear();
			//xmlWriter.writeStartElement("row");
			//xmlWriter.writeAttribute("num", attributes.getValue("r"));
			cellCounter = 0;
			prevCell = 0;
		} else if ("c".equals(name)) {
			isCell = true;
			// cellCounter++;
			cellCounter = getCellNumber(attributes.getValue("r"));
			type = attributes.getValue("t");
			style = attributes.getValue("s");
		} else if (isCell && "v".equals(name) || "is".equals(name)) {
			buf = "";
			isData = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (isData) {
			if ("v".equals(name)) {
				isData = false;
				if ("s".equals(type)) {
					int idx = Integer.parseInt(buf);
					buf = new XSSFRichTextString(sst.getEntryAt(idx))
							.toString();
				} else if (type == null || "n".equals(type)) {
					if (isDate()) {
						buf = dateFormatter.format(DateUtil.getJavaDate(Double
								.parseDouble(buf)));
					}
				}

			} else if ("is".equals(name)) {
				isData = false;
				buf = new XSSFRichTextString(buf).toString();
			}
		}
		if (isCell && "c".equals(name)) {
			addCell();
			style = "";
		}
		if ("row".equals(name)) {
			//xmlWriter.writeEndElement();
			try {
				this.parser.notifyWriteRow(this.values);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new SAXException(ExceptionUtils.getStackTrace(e)); //
			}
		}
	}

	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (isData) {
			buf += new String(ch, start, length);
		}
	}
	
	
	
	
	

	private void addCell() {
		// Дописать пропущенные ячейки
		for (int i = prevCell + 1; i < cellCounter; i++) {
				//xmlWriter.writeEmptyElement("cell");
				//xmlWriter.writeAttribute("num", i + "");
				this.values.add(null);
		}

//			xmlWriter.writeStartElement("cell");
//			xmlWriter.writeAttribute("num", cellCounter + "");

		if (buf != null) {
//			try {
			this.values.add(buf);
			//xmlWriter.writeCharacters(buf);
//			} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
//			} catch (ArrayIndexOutOfBoundsException e) {
//				e.printStackTrace();
//			}
		}

//		try {
//			xmlWriter.writeEndElement();
//		} catch (XMLStreamException e) {
//			// TODO Auto-generated catch block
//		}
		buf = "";
		prevCell = cellCounter;
	}

	private boolean isDate() {
		boolean res = false;
		double d = Double.parseDouble(buf);
		if (style != null && !style.isEmpty() && DateUtil.isValidExcelDate(d)) {
			CTXf xf = st.getCellXfAt(Integer.valueOf(style));
			if (xf == null)
				return false;
			int i = (int) xf.getNumFmtId();
			String f = st.getNumberFormatAt(i);
			res = DateUtil.isADateFormat(i, f);
		}
		return res;
	}

	private int getCellNumber(String val) {
		int num = 0;
		int iter = 0;
		char c;
		StringBuffer colname = new StringBuffer();
		for(c = val.charAt(iter); Character.isLetter(c); colname = colname.insert(0, c), iter ++, c = val.charAt(iter));
		for(int i = 0; i < colname.length(); i ++){
			num = num + (int)Math.pow(26, i)* (colname.charAt(i) - 'A' + 1);
		}
		return num;
	}
} 
