package ru.tander.sprint.excel.processor;

public class ProcessorFactory {
	
	private static String extension(String fileName){
		String extension = "";
		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		
		if (i > p) {
		    extension = fileName.substring(i+1);
		}
		return extension;
	}
	
	public static IProcessor create(String fileName){
		 String ext = extension(fileName);
		 if("xls".equalsIgnoreCase(ext)){
		 	return new XlsProcessor();
		 }else
		 if("xlsx".equalsIgnoreCase(ext)){
			 return new XlsxProcessor();	
		 }else {
		 	throw new NullPointerException("File format can be xls of xlsx.");
		 }
	}
}
