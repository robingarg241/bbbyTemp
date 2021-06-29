package com.bbby.aem.core.models.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.sling.api.request.RequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value object representing one row in the metadata Excel spreadsheet
 * 
 * @author vpokotylo
 *
 */
public class MetadataRow {
    
    
	private final Logger log = LoggerFactory.getLogger(MetadataRow.class);
	
	/**
     * The Contact info
     */
    String filename;
    
    boolean assetUpdate = false;

    String reasonForUpdate;
        
	String upc;
    
    String assetType;
    
    String shotType;
    
    int sequence;
    
    String additionalUPC;
    
    boolean srtProvided = false;
	
	boolean bbbyToCreateSRT = false;
    
    Row row;
    
    Map<String, RequestParameter[]> reqParams;
    
    public MetadataRow(Row row)  {
        this.row = row;
    }
    
    public MetadataRow(Map<String, RequestParameter[]> reqParams)  {
        this.reqParams = reqParams;
    }
    
   	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	
	public boolean isAssetUpdate() {
		return assetUpdate;
	}

	public void setAssetUpdate(boolean assetUpdate) {
		this.assetUpdate = assetUpdate;
	}

	public String getReasonForUpdate() {
		return reasonForUpdate;
	}

	public void setReasonForUpdate(String reasonForUpdate) {
		this.reasonForUpdate = reasonForUpdate;
	}
	
	public String getUpc() {
		return upc;
	}

	public void setUpc(String upc) {
		this.upc = upc;
	}

	public String getAssetType() {
		return assetType;
	}

	public void setAssetType(String assetType) {
		this.assetType = assetType;
	}

	public String getShotType() {
		return shotType;
	}

	public void setShotType(String shotType) {
		this.shotType = shotType;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	public String getAdditionalUPC() {
		return additionalUPC;
	}

	public void setAdditionalUPC(String additionalUPC) {
		this.additionalUPC = additionalUPC;
	}

	public boolean isSrtProvided() {
		return srtProvided;
	}

	public void setSrtProvided(boolean srtProvided) {
		this.srtProvided = srtProvided;
	}

	public boolean isBbbyToCreateSRT() {
		return bbbyToCreateSRT;
	}

	public void setBbbyToCreateSRT(boolean bbbyToCreateSRT) {
		this.bbbyToCreateSRT = bbbyToCreateSRT;
	}

	public MetadataRow buildMetadataFromExcel() {

		// Filename
		Cell c = row.getCell(0);
		if (c != null && (c.getCellTypeEnum() == CellType.STRING || c.getCellTypeEnum() == CellType.FORMULA)) {
			//DAM-927 & 992 : Image name have space in the last and trim is not working as expected.
			this.setFilename(StringUtils.replace(StringUtils.trim(c.getStringCellValue()),"\u00A0", ""));
			log.info("FileName is : " +this.getFilename());
		} else {
			log.info("Filename is null, Please check the Excelsheet.");
			return null;
		}

		// Asset Update
		c = row.getCell(1);
		if (c != null && c.getCellTypeEnum() == CellType.STRING) {
			String value = c.getStringCellValue();
			this.setAssetUpdate("yes".equals(value.toLowerCase()));
		}

		// Reason for Update
		c = row.getCell(2);
		if (c != null && c.getCellTypeEnum() == CellType.STRING) {
			this.setReasonForUpdate(c.getStringCellValue());
		}

		// UPC
		c = row.getCell(3);
		if (c != null && c.getCellTypeEnum() == CellType.NUMERIC) {
			log.info("UPC is " + c.getNumericCellValue());
			double numValue = c.getNumericCellValue();
			String stringVaue = String.format("%.0f", numValue);
			//DAM-518 :  remove prefix 0's,
			this.setUpc(removeZeros(stringVaue));
		} else if (c != null && (c.getCellTypeEnum() == CellType.STRING || c.getCellTypeEnum() == CellType.FORMULA)) {
			log.info("UPC is " + c.getStringCellValue());
			//DAM-518 :  remove prefix 0's,
			this.setUpc(removeZeros(c.getStringCellValue()));
		} else if (c != null){
			String format = c.getCellStyle().getDataFormatString();
			log.info("Unexpected format for UPC {}", format);
		} else{
			log.info("UPC is null Please check the Excelsheet.");
		}

		// Asset Type
		c = row.getCell(4);
		if (c != null && (c.getCellTypeEnum() == CellType.STRING || c.getCellTypeEnum() == CellType.FORMULA)) {
			this.setAssetType(c.getStringCellValue());
		}else{
			log.info("Asset Type is null, Please check the Excelsheet.");
		}

		// Shot type
		c = row.getCell(5);
		if (c != null && (c.getCellTypeEnum() == CellType.STRING || c.getCellTypeEnum() == CellType.FORMULA)) {
			this.setShotType(c.getStringCellValue());
		}else{
			log.info("Shot Type is null, Please check the Excelsheet.");
		}

		// Sequence
		c = row.getCell(6);
		if (c != null && c.getCellTypeEnum() == CellType.NUMERIC) {
			this.setSequence(new Double(c.getNumericCellValue()).intValue());
		}

		// Additional UPC - Videos bbby:additionalUPC
		c = row.getCell(7);

		if (c != null && c.getCellTypeEnum() == CellType.NUMERIC) {
			double numValue = c.getNumericCellValue();
			String stringVaue = String.format("%.0f", numValue);

			this.setAdditionalUPC(stringVaue);

		} else if (c != null && c.getCellTypeEnum() == CellType.STRING) {
			this.setAdditionalUPC(c.getStringCellValue());
		}

		// bbby:srtProvided
		c = row.getCell(8);
		if (c != null && c.getCellTypeEnum() == CellType.STRING) {
			String value = c.getStringCellValue();
			this.setSrtProvided("yes".equals(value.toLowerCase()));
		}

		// bbby:bbbyToCreateSRT
		c = row.getCell(9);
		if (c != null && c.getCellTypeEnum() == CellType.STRING) {
			String value = c.getStringCellValue();
			this.setBbbyToCreateSRT("yes".equals(value.toLowerCase()));
		}

		return this;
	}
    
	public MetadataRow buildMetadata() throws IOException {
		
        //rowOut.setFilename(c.getStringCellValue());
		String value = Streams.asString(reqParams.get("assetUpdate")[0].getInputStream());
		this.setAssetUpdate("yes".equals(value));
		
		if(reqParams.containsKey("reasonUpdate")) {
			this.setReasonForUpdate(Streams.asString(reqParams.get("reasonUpdate")[0].getInputStream()));
		}
		
		//DAM-518 :  remove prefix 0's,
		String upcs = removeZeros(Streams.asString(reqParams.get("upc")[0].getInputStream()));
		this.setUpc(upcs);
		
		if(reqParams.containsKey("assetType")) {
			this.setAssetType(Streams.asString(reqParams.get("assetType")[0].getInputStream()));
		}
		
		this.setShotType(Streams.asString(reqParams.get("shotType")[0].getInputStream()));

        if (StringUtils.isNotEmpty(Streams.asString(reqParams.get("sequence")[0].getInputStream()))) {
        	try {
        		this.setSequence(Integer.parseInt(Streams.asString(reqParams.get("sequence")[0].getInputStream())));
    		} catch(NumberFormatException e) {
    			this.setSequence(0);
    		}
        }
    	
        if(reqParams.containsKey("additionalUpc")) {
        	this.setAdditionalUPC(Streams.asString(reqParams.get("additionalUpc")[0].getInputStream()));
        }
        
        if(reqParams.containsKey("bbbysrt")) {
        	String bbbysrt = Streams.asString(reqParams.get("bbbysrt")[0].getInputStream());
    		this.setBbbyToCreateSRT("yes".equals(bbbysrt.toLowerCase()));
        }
        
		if(reqParams.containsKey("srtProvided")) {
			String srtProvided = Streams.asString(reqParams.get("srtProvided")[0].getInputStream());
			this.setSrtProvided("yes".equals(srtProvided.toLowerCase()));
		}
		
		
        return this;
    }
	
	//DAM-518 :  remove prefix 0's,
	private String removeZeros(String str) {
		String citiesCommaSeparated = null;
		if (str != null && str != "") {
			String[] stra = null;
			if(str.contains(";")){
				stra = str.split(";");
			}else{
				stra = str.split(",");
			}
			if (stra != null && stra.length > 0) {
				ArrayList<String> ans = new ArrayList<String>();
				for (String sq : stra) {
					ans.add(sq.trim().replaceFirst("^0+(?!$)", ""));
				}
				citiesCommaSeparated = String.join(",", ans);
			}
		}
		return citiesCommaSeparated;
	}
}
