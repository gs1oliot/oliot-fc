/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.tm;

public class SymbolicField {
	public static int FIXED_FIELD = 0;
	public static int VARIABLE_FIELD = 1;
	

	private String epcPattern;
	
	private int fieldType;		// FIXED_FIELD = 0 or VARIABLE_FIELD = 1
	private String dataType;
	private String format;
	
	private int bank;
	private int length;
	private int offset;
	
	
	public String getEpcPattern() {
		return epcPattern;
	}
	public void setEpcPattern(String epcPattern) {
		this.epcPattern = epcPattern;
	}
	public int getFieldType() {
		return fieldType;
	}
	public void setFieldType(int fieldType) {
		this.fieldType = fieldType;
	}
	public int getBank() {
		return bank;
	}
	public void setBank(int bank) {
		this.bank = bank;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}


	
}
