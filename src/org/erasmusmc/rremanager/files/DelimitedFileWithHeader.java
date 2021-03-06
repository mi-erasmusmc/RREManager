package org.erasmusmc.rremanager.files;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DelimitedFileWithHeader implements Iterable<DelimitedFileRow> {
	private static char DEFAULT_DELIMITER      = ',';
	private static char DEFAULT_TEXT_DELIMITER = '"';
	
	private String fileName = null;
	private char delimiter	= DEFAULT_DELIMITER;
	private char textDelimiter = DEFAULT_TEXT_DELIMITER;
	private String charSet = null;

	private InputStream	inputStream = null;
	private DelimitedFile delimitedFile = null;
	
	private RowIterator rowIterator = null;
	
	private List<String> header = null;

	public DelimitedFileWithHeader(String fileName) {
		this(fileName, DEFAULT_DELIMITER, DEFAULT_TEXT_DELIMITER, null);
	}

	public DelimitedFileWithHeader(String fileName, char delimiter) {
		this(fileName, delimiter, DEFAULT_TEXT_DELIMITER, null);
	}

	public DelimitedFileWithHeader(String fileName, char delimiter, char textDelimiter) {
		this(fileName, delimiter, textDelimiter, null);
	}
	
	public DelimitedFileWithHeader(String fileName, String charSet) {
		this(fileName, DEFAULT_DELIMITER, DEFAULT_TEXT_DELIMITER, null);
	}
	
	public DelimitedFileWithHeader(String fileName, char delimiter, String charSet) {
		this(fileName, delimiter, DEFAULT_TEXT_DELIMITER, null);
	}

	public DelimitedFileWithHeader(String fileName, char delimiter, char textDelimiter, String charSet) {
		this.fileName = fileName;
		this.delimiter = delimiter;
		this.textDelimiter = textDelimiter;
		this.charSet = charSet;
	}

	
	public DelimitedFileWithHeader(InputStream inputStream, char delimiter) {
		this(inputStream, delimiter, DEFAULT_TEXT_DELIMITER, null);
	}

	
	public DelimitedFileWithHeader(InputStream inputStream, char delimiter, char textDelimiter) {
		this(inputStream, delimiter, textDelimiter, null);
	}

	
	public DelimitedFileWithHeader(InputStream inputStream, String charSet) {
		this(inputStream, DEFAULT_DELIMITER, DEFAULT_TEXT_DELIMITER, null);
	}

	
	public DelimitedFileWithHeader(InputStream inputStream, char delimiter, String charSet) {
		this(inputStream, delimiter, DEFAULT_TEXT_DELIMITER, null);
	}

	public DelimitedFileWithHeader(InputStream inputStream, char delimiter, char textDelimiter, String charSet) {
		this.inputStream = inputStream;
		this.delimiter = delimiter;
		this.textDelimiter = textDelimiter;
		this.charSet = charSet;
	}
	
	
	public boolean openForReading() {
		if ((inputStream == null) && (fileName != null)) {
			delimitedFile = new DelimitedFile(fileName, delimiter, textDelimiter, charSet);
		}
		if ((delimitedFile == null) && (inputStream != null)) {
			delimitedFile = new DelimitedFile(inputStream, delimiter, textDelimiter, charSet);
		}
		return (delimitedFile == null ? false : delimitedFile.openForReading());
	}
	
	
	public boolean isOpen() {
		return (inputStream != null);
	}
	
	
	public Set<String> getColumns() {
		Set<String> columns = null;
		if (rowIterator != null) {
			columns = rowIterator.getColumns();
		}
		return columns;
	}

	
	@Override
	public Iterator<DelimitedFileRow> iterator() {
		return new RowIterator();
	}

	
	public class RowIterator implements Iterator<DelimitedFileRow> {

		private Iterator<List<String>>	iterator;
		private Map<String, Integer>	fieldName2ColumnIndex;

		
		public RowIterator() {
			iterator = delimitedFile.iteratorWithHeader();
			fieldName2ColumnIndex = new HashMap<String, Integer>();
			if (iterator.hasNext()) {
				for (String header : iterator.next())
					fieldName2ColumnIndex.put(header, fieldName2ColumnIndex.size());
			}
		}

		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		
		@Override
		public DelimitedFileRow next() {
			return new DelimitedFileRow(iterator.next(), fieldName2ColumnIndex);
		}

		
		@Override
		public void remove() {
			throw new RuntimeException("Remove not supported");
		}
		
		
		public Set<String> getColumns() {
			return fieldName2ColumnIndex.keySet();
		}
	}
	
	
	public boolean openForWriting() {
		boolean result = false;
		delimitedFile = new DelimitedFile(fileName);
		if (delimitedFile.openForWriting()) {
			if (header != null) {
				delimitedFile.writeRecord(header);
			}
			result = true;
		}
		else {
			delimitedFile = null;
		}
		return result;
	}
	
	
	public void setHeader(List<String> header) {
		this.header = header;
		if (delimitedFile != null) {
			delimitedFile.writeRecord(header);
		}
	}
	
	
	public void writeRow(DelimitedFileRow row) {
		if (delimitedFile != null) {
			List<String> record = new ArrayList<String>();
			for (String column : header) {
				String value = row.get(column, false);
				record.add(value == null ? "" : value);
			}
			delimitedFile.writeRecord(record);
		}
	}
	
	
	public void closeForWriting() {
		delimitedFile.closeForWriting();
		delimitedFile = null;
		header = null;
	}
}
