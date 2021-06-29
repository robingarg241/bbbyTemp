package com.hero.jcr.commandline.util;

import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

public class FilenameUtilTest {
	
	/*
	 * We have three rules for changing asset uri
	 * This method modifies the found URI and changes it 
	 * based on these rules
	 */
	private static String modifyNodeName(String nodeName, String language){
		
		String retVal = null;
		
		String fullPath = FilenameUtils.getFullPath(nodeName);
		String extension = FilenameUtils.getExtension(nodeName);
		String fileName = FilenameUtils.getName(nodeName);
		
		String newPath = fullPath.replace("global/amer/us", language);
		
		if (extension.equals("pdf")){
			
			if(fileName.startsWith("en_")){
				
				fileName = fileName.replace("en_", language + "_");
				
			}
		
		}
		
		retVal = newPath + fileName;
		
		return retVal;
	}

	public static void main(String[] args) {
		
		ArrayList<String> nodeList = new ArrayList<String>();

		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/executive-brief/gated/en_how-to-avoid-the-trap-of-mdm-silos_executive-brief_7103.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/executive-brief/gated/en_how-to-overcome-the-limitations-of-customer-data-integration_executive-brief_7102.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/white-paper/gated/en_ema-impact-brief-big-data_white-paper_1684.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/customer-success-story/gated/en_idc-slash-information-retention-costs-with-application-retirement_case-study_7131.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/white-paper/gated/en_big-data-unleashed_turning-big-data-into-big-opportunities_white-paper_1601.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/white-paper/gated/en_data-warehouse-archiving_white-paper_7082.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/image/cc01/customer-logos/cc01-dsk-bank.png");
		nodeList.add("/content/dam/informatica-com/global/amer/us/image/misc/uk-oracle-user-group-partner-of-the-year-awards.gif");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/solution-brief/capgemini-bim-informatica-dwo_sales-flyer_1405.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/image/c04/c04-banking-and-capital-markets.png");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/executive-brief/idc-managing-big-data-in-motion_executive-brief_1916.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/analyst-report/constellation-cosmos-data-archiving_analyst-report_2210.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/image/cc02/prestige.gif");
		nodeList.add("/content/dam/informatica-com/global/amer/us/image/misc/informatica_logo_notagline.gif");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/data-sheet/ips-data-migration_data-sheet_6058-6772.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/executive-brief/dont-lose-millions-bad-supplier-information_executive-brief_2662.pdf");
		nodeList.add("/content/dam/informatica-com/global/amer/us/image/ca02/il4_vital-link-between-business-apps-and-data-gov-226x226.jpg");
		nodeList.add("/content/dam/informatica-com/global/amer/us/image/c09/c09-products-big-data-infer-nonobviousrelationships.png");
		nodeList.add("/content/dam/informatica-com/global/amer/us/image/c21/c21-file-processor.png");
		nodeList.add("/content/dam/informatica-com/global/amer/us/collateral/training-contentless/rm01-rulepoint-courses.jpg");
		
		for (String node : nodeList){
			
			String newNode = modifyNodeName(node, "de");
			System.out.println(node + "\t" + newNode);
			
		}
			
		
		
	}

}
