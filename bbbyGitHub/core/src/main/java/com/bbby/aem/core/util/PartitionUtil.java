package com.bbby.aem.core.util;

import java.util.ArrayList;

public class PartitionUtil {
    
    public static ArrayList<String> hashFileName(String fileName){

        boolean isImageSet = false;
        
        //imagesets have not extenstion, we will give them one temporarally
        if (fileName.toLowerCase().contains("imageset")) {
            
            fileName = fileName + ".tmp";
            isImageSet = true;
            
        }
        
        //split off extension, we want all 'versions' of an asset in one place e.g. bob.jpg, bob.psd
        String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
        
        //is this really a filename?
        String baseFileName;
        if (tokens.length >= 2){
        
            if (isImageSet){
    
                //imagesets all start with the same name (Imageset), so we are going to reverse the string for more varity
                baseFileName = new StringBuilder(tokens[0]).reverse().toString();
                
            } else {

                baseFileName = tokens[0];
                
            }
        } else {
            
            baseFileName = "unknown";
        
        }
        
        ArrayList<String> partitions = new ArrayList<String>();
        
        String hashMaster = String.valueOf(Math.abs(baseFileName.hashCode()));
        
        //we expect this to be a *very* rare occurance
        if (hashMaster.length() < 6){
            
            hashMaster = hashMaster + "000000";
            
        }
        
        String firstPartition = hashMaster.substring(0, 3);
        String secondPartition = hashMaster.substring(3, 6);
        
        partitions.add(firstPartition);
        partitions.add(secondPartition);

        return partitions;  
        
    }

}
