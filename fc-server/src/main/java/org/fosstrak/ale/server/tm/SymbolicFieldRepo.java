/**
 * Copyright (C) 2014 KAIST
 * @author Janggwan Im <limg00n@kaist.ac.kr> 
 */


package org.fosstrak.ale.server.tm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fosstrak.ale.exception.ECSpecValidationException;
import org.fosstrak.ale.exception.ImplementationException;
import org.fosstrak.ale.server.Pattern;
import org.fosstrak.ale.server.PatternUsage;
import org.fosstrak.ale.wsdl.aletm.epcglobal.DuplicateNameExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.ImplementationExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.NoSuchNameExceptionResponse;
import org.fosstrak.ale.wsdl.aletm.epcglobal.TMSpecValidationExceptionResponse;
import org.fosstrak.ale.xsd.ale.epcglobal.TMFixedFieldListSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.TMFixedFieldSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.TMSpec;
import org.fosstrak.ale.xsd.ale.epcglobal.TMVariableFieldListSpec;

public class SymbolicFieldRepo {
	/**
	 * Data structure to store symbolic field information for TMSpec API.
	 * 
	 * It consists of a map of epcPattern to symbolic field map, which is a map of fieldname to symbolic field
	 */
	
	private static SymbolicFieldRepo repo = null;
	
	
	//private Map<String, Map<String, SymbolicField>> mapEPCPatternAndSymbolicFieldMap = new HashMap<String, Map<String, SymbolicField>>();
	private Map<String, SymbolicField> mapFieldnameAndSymbolicField = new HashMap<String, SymbolicField>();
	private Map<String, TMSpec> mapEPCPatternAndTMSpec = new HashMap<String, TMSpec>();
	//private Map<String, Pattern> mapEPCPatternAndPatternObject = new HashMap<String, Pattern>();
	
	public SymbolicFieldRepo() {
		
	}

	
	public void addSymbolicField(String epcPattern, TMSpec spec) throws TMSpecValidationExceptionResponse, ImplementationExceptionResponse, DuplicateNameExceptionResponse {
		if(mapEPCPatternAndTMSpec.containsKey(epcPattern)) {
			throw new DuplicateNameExceptionResponse("TMSpec "+epcPattern+" already exists");
		}
		// map (String fieldname, SymbolicField symbolicField)
		
		
		if(spec instanceof TMFixedFieldListSpec) {
			
			List<TMFixedFieldSpec> listFixedfield = ((TMFixedFieldListSpec)spec).getFixedFields().getFixedField();
			
			for(TMFixedFieldSpec fixedfieldSpec : listFixedfield) {
				
				// validate the fieldspec
				if(mapFieldnameAndSymbolicField.containsKey(fixedfieldSpec.getFieldname())) {
					throw new TMSpecValidationExceptionResponse("fieldname "+fixedfieldSpec.getFieldname()+" already exists"); 
				}
				if(fixedfieldSpec.getBank() < 0 || fixedfieldSpec.getBank() >= 4) {
					throw new TMSpecValidationExceptionResponse("Bank number of field "+fixedfieldSpec.getFieldname()+" is not valid");
				}
				if(fixedfieldSpec.getLength() < 0) {
					throw new TMSpecValidationExceptionResponse("Length of field "+fixedfieldSpec.getFieldname()+" is not valid");
				}
				if(fixedfieldSpec.getOffset() < 0) {
					throw new TMSpecValidationExceptionResponse("Offset of field "+fixedfieldSpec.getFieldname()+" is not valid");
				}
				if(fixedfieldSpec.getFieldname().equals("epc") ||
						fixedfieldSpec.getFieldname().equals("killPwd") ||
						fixedfieldSpec.getFieldname().equals("accessPwd") || 
						fixedfieldSpec.getFieldname().equals("epcBank") ||
						fixedfieldSpec.getFieldname().equals("tidBank") ||
						fixedfieldSpec.getFieldname().equals("userBank") ||
						fixedfieldSpec.getFieldname().equals("afi") ||
						fixedfieldSpec.getFieldname().equals("nsi") ) {
					throw new TMSpecValidationExceptionResponse("fieldname "+fixedfieldSpec.getFieldname()+" is built-in fieldname, not allowed to redefine");
				}
					
				
				
				SymbolicField field = new SymbolicField();
				field.setFieldType(0); // indicating "FIXED_FIELD"
				
				if(fixedfieldSpec.getFieldname().equalsIgnoreCase("epc")) {
					
					field.setDataType("epc");
					field.setFormat("epc-tag");
				} else if(fixedfieldSpec.getFieldname().equalsIgnoreCase("killPwd")) {
					// same as "@0.32"
					field.setBank(0);
					field.setLength(32);
					field.setOffset(0);
					field.setDataType("uint");
					field.setFormat("hex");
				} else if(fixedfieldSpec.getFieldname().equalsIgnoreCase("accessPwd")) {
					// same as "@0.32.32"
					field.setBank(0);
					field.setLength(32);
					field.setOffset(32);
					field.setDataType("uint");
					field.setFormat("hex");
				} else if(fixedfieldSpec.getFieldname().equalsIgnoreCase("epcBank")) {
					field.setBank(1);
					field.setLength(-1);	// entire bank contents
					field.setOffset(0);
					field.setDataType("bits");
					field.setFormat("hex");
				} else if(fixedfieldSpec.getFieldname().equalsIgnoreCase("tidBank")) {
					field.setBank(2);
					field.setLength(-1);	// entire bank contents
					field.setOffset(0);
					field.setDataType("bits");
					field.setFormat("hex");
				} else if(fixedfieldSpec.getFieldname().equalsIgnoreCase("userBank")) {
					field.setBank(3);
					field.setLength(-1);	// entire bank contents
					field.setOffset(0);
					field.setDataType("bits");
					field.setFormat("hex");
				} else if(fixedfieldSpec.getFieldname().equalsIgnoreCase("afi")) {
					// same as "@1.8.24"
					field.setBank(1);
					field.setLength(8);
					field.setOffset(24);
					field.setDataType("uint");
					field.setFormat("hex");
				} else if(fixedfieldSpec.getFieldname().equalsIgnoreCase("nsi")) {
					// same as "@1.9.23"
					field.setBank(1);
					field.setLength(9);
					field.setOffset(23);
					field.setDataType("uint");
					field.setFormat("hex");
				} else if (fixedfieldSpec.getFieldname().startsWith("@")) {
					// fieldname with starts with "@"
					try {
						String[] part = fixedfieldSpec.getFieldname().substring(1).split("\\.");
						field.setBank(Integer.parseInt(part[0]));
						field.setLength(Integer.parseInt(part[1]));
						field.setOffset(Integer.parseInt(part[2]));
						field.setDataType("uint");
						field.setFormat("hex");
					} catch(NumberFormatException e) {
						throw new TMSpecValidationExceptionResponse("fieldname "+fixedfieldSpec.getFieldname()+" is wrong");
					}
					
				} else {
					// user-defined symbolic field name
					field.setBank(fixedfieldSpec.getBank());
					field.setLength(fixedfieldSpec.getLength());
					field.setOffset(fixedfieldSpec.getOffset());
				}
				if(fixedfieldSpec.getDefaultDatatype() != null) 
					field.setDataType(fixedfieldSpec.getDefaultDatatype());
				
				if(fixedfieldSpec.getDefaultFormat() != null)
					field.setFormat(fixedfieldSpec.getDefaultFormat());

				mapFieldnameAndSymbolicField.put(fixedfieldSpec.getFieldname(), field);
			}
			
		} else if (spec instanceof TMVariableFieldListSpec) {
			throw new ImplementationExceptionResponse("not implemented yet");
			
		} else {
			throw new TMSpecValidationExceptionResponse();
		}
		synchronized(SymbolicFieldRepo.class) {

			mapEPCPatternAndTMSpec.put(epcPattern, spec);
			//mapEPCPatternAndSymbolicFieldMap.put(epcPattern, mapFieldnameAndSymbolicField);
			/*
			try {
				mapEPCPatternAndPatternObject.put(epcPattern, new Pattern(epcPattern, PatternUsage.FILTER));
			} catch (ECSpecValidationException e) {
				e.printStackTrace();
				throw new ImplementationExceptionResponse(e.getMessage());
			}*/
		}
	}
	
	public void removeSymbolicField(String epcPattern) throws NoSuchNameExceptionResponse {
		synchronized(SymbolicFieldRepo.class) {
			if (!mapEPCPatternAndTMSpec.containsKey(epcPattern)) {
				throw new NoSuchNameExceptionResponse("TMSpec name "+epcPattern+" does not exist");
			}
			TMSpec tmspec = mapEPCPatternAndTMSpec.remove(epcPattern);
			if(tmspec instanceof TMFixedFieldListSpec) {
				TMFixedFieldListSpec spec = (TMFixedFieldListSpec)tmspec;
				for(TMFixedFieldSpec f : spec.getFixedFields().getFixedField()) {
					mapFieldnameAndSymbolicField.remove(f.getFieldname());
				}
			}
			
			//mapEPCPatternAndSymbolicFieldMap.remove(epcPattern);
			//mapEPCPatternAndPatternObject.remove(epcPattern);
		}
	}
	
	public Set<String> getSymbolicFieldNames() {
		return mapEPCPatternAndTMSpec.keySet();
	}
	
	public TMSpec getTMSpec(String epcPattern) throws NoSuchNameExceptionResponse {
		if(!mapEPCPatternAndTMSpec.containsKey(epcPattern)) {
			throw new NoSuchNameExceptionResponse("TMSpec name "+epcPattern+" does not exist");
		}
		return mapEPCPatternAndTMSpec.get(epcPattern);
	}
	
	public SymbolicField getSymbolicField(String fieldname) {
		return mapFieldnameAndSymbolicField.get(fieldname);
	}
	
	public static SymbolicFieldRepo getInstance() {
		if(repo == null) {
			synchronized(SymbolicFieldRepo.class) {
				repo = new SymbolicFieldRepo();				
			}
		}
		return repo;
	}
	/*
	public synchronized Map<String, SymbolicField> findSymbolicFieldMap(String epc) {
		for(String key : mapEPCPatternAndSymbolicFieldMap.keySet()) {
			System.out.println(key);
			Pattern p = mapEPCPatternAndPatternObject.get(key);
			try {
				if(p.isMember(epc)) {
					return mapEPCPatternAndSymbolicFieldMap.get(key);
				}
			} catch (ECSpecValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ImplementationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}*/
}
