package com.hero.jcr.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class PartitionUtil {
    
	public static ArrayList<String> hashFileName(String fileName){
		
		String[] parts = fileName.split("/");
		
		String firstPartition = parts[parts.length - 2];
		String secondPartition = parts[parts.length - 1];
		
		ArrayList<String> partsArray = new ArrayList<String>(Arrays.asList(parts));	
		
		int arraySize = partsArray.size();
		
		List<String> leadingPart = partsArray.subList(0, arraySize - 2);
		
		String lead = String.join("/", leadingPart);			

		ArrayList<String> partitions = new ArrayList<String>();
		partitions.add(lead);
		partitions.add(firstPartition);
		partitions.add(secondPartition);

		return partitions;  
		
	}

}
