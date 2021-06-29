/*
 * Moves assets according to predefined folder structure.
 * for example 2019-08-30-09-49_wwww_perf_testimage_9.jpg will be moved to /2019-08-30/noritake/019-08-30-09-49_wwww_perf_testimage_9.jpg 
 */

import com.day.cq.commons.jcr.JcrUtil

def ROOT_PATH = "/content/dam/bbby/asset_transitions_folder/vendor/not_assigned"  
def NODE_LIMIT = 500
  
def boolean processChild(resource) {  
    if (resource == null) return  false
      
    if (resource.getResourceType() == "dam:Asset") {   
        Node node = resource.adaptTo(Node.class) 
        def id = node.getName()
        
        if(id != null) {
            def vendor = node.get("jcr:content/upcmetadata/upc-0/primaryVendorName")
            if(vendor != null) {
                def validVendorName = JcrUtil.createValidName(vendor, JcrUtil.HYPHEN_LABEL_CHAR_MAPPING, "-")
                println("" + node.getPath() + ", " + node.get("jcr:created").format("YYYY-MM-dd") + ", " + vendor + ", " + validVendorName)
            
                def targetFolder = getOrCreateFolders(node)
                println("Moving to folder " + targetFolder)
                
                if(targetFolder != null) {
                    session.move(node.getPath(), targetFolder + "/" + id )
                }
            } else {
                println("No vendor set for  " + node.getPath() + " moving to /vendor-undefined")
                session.move(node.getPath(), node.getParent().getPath() + "/vendor-undefined/" + id )
            }
            
        }
        
        session.save()
        return true
        
    }  else {
        return false
    }
}  

def String getOrCreateFolders(Node node) {
    def vendor = node.get("jcr:content/upcmetadata/upc-0/primaryVendorName")
    def validVendorName = com.day.cq.commons.jcr.JcrUtil.createValidName(vendor, com.day.cq.commons.jcr.JcrUtil.HYPHEN_LABEL_CHAR_MAPPING, "-")
    def dayFormatted = node.get("jcr:created").format("YYYY-MM-dd")
    
    def parent = node.getParent().getPath();
    def datePath = parent + "/" + dayFormatted
    def dateResource = resourceResolver.getResource(datePath) 
    
    if(dateResource == null) {
        println("Creating " + datePath) 
        JcrUtil.createPath(datePath, "sling:Folder", session);
    }
    
    def vendorPath = datePath + "/" + validVendorName
    
    def vendorResource = resourceResolver.getResource(vendorPath) 
    
    if(vendorResource == null) {
        println("Creating " + vendorPath) 
        def vendorFolder = JcrUtil.createPath(vendorPath, "sling:Folder", session);
        if(vendorFolder != null) {
            vendorFolder.setProperty("jcr:title", vendor)
        }
        
    }
    
    return vendorPath
}

  
Resource resource = resourceResolver.getResource(ROOT_PATH)  
  
if (resource != null) {  
    
    def i = 0
    
    for( Resource item : resource.getChildren()) {
        def success = processChild(item)
        if(success) {
            i++
        }
        
        if(i > NODE_LIMIT) break
    }
    /*resource.getChildren().each() {  
        processChild(it)  
        if()
    } */ 
}