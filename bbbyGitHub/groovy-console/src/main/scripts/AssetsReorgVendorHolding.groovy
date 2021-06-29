/*
 * Moves assets according to predefined folder structure.
 * for example 2019-08-30-09-49_wwww_perf_testimage_9.jpg will be moved to /2019-08-30/noritake/019-08-30-09-49_wwww_perf_testimage_9.jpg 
 */

import com.day.cq.commons.jcr.JcrUtil

def ROOT_PATH = "/content/dam/bbby/asset_transitions_folder/vendor/vendor_assets_holding"  
def NODE_LIMIT = 1000
  
def boolean processChild(resource) {  
    if (resource == null) return  false
      
    if (resource.getResourceType() == "sling:Folder") {   
        Node node = resource.adaptTo(Node.class) 
        def id = node.getName()
        
        def createdBy = node.get("jcr:createdBy")
        if(createdBy != "vp-content-service") {
            println("Skipping " + node.getPath() + ", not a batch node")
            return false
        }
        
        if(id != null) {
            println("" + node.getPath() + ", " + node.get("jcr:created").format("YYYY-MM-dd"))
            
            def targetFolder = getOrCreateFolders(node)
            println("Moving to folder " + targetFolder)
                
            if(targetFolder != null) {
                session.move(node.getPath(), targetFolder + "/" + id )
            }
            
        }
        
        session.save()
        return true
        
    } 
    
    return false
}  

def String getOrCreateFolders(Node node) {
    
    def dayFormatted = node.get("jcr:created").format("YYYY-MM-dd")
    
    def parent = node.getParent().getPath();
    def datePath = parent + "/" + dayFormatted
    def dateResource = resourceResolver.getResource(datePath) 
    
    if(dateResource == null) {
        println("Creating " + datePath) 
        JcrUtil.createPath(datePath, "sling:Folder", session);
    }
    
    return datePath
}

  
Resource resource = resourceResolver.getResource(ROOT_PATH)  
  
if (resource != null) {  
    
    def i = 0
    
    for( Resource item : resource.getChildren()) {
        if(processChild(item)) {
            i++
        }
        
        if( i > NODE_LIMIT) break
    }
    /*resource.getChildren().each() {  
        processChild(it)  
        if()
    } */ 
}