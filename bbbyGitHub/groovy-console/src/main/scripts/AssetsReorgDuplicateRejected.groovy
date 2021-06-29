/*
 * Moves assets according to predefined folder structure.
 * for example 2019-08-30-09-49_wwww_perf_testimage_9.jpg will be moved to /2019-08-30/019-08-30-09-49_wwww_perf_testimage_9.jpg 
 */

import com.day.cq.commons.jcr.JcrUtil

def ROOT_PATH = "/content/dam/bbby/asset_transitions_folder/vendor/duplicate_vendor_assets"  
def NODE_LIMIT = 2000
  
def processChild(resource) {  
    if (resource == null) return  
      
    if (resource.getResourceType() == "dam:Asset") {   
        Node node = resource.adaptTo(Node.class) 
        def id = node.getName()
        
        if(id != null) {
            println("" + node.getPath() + ", " + node.get("jcr:created").format("YYYY-MM-dd"))
            
            def targetFolder = getOrCreateFolders(node)
            println("Moving to folder " + targetFolder)
                
            if(targetFolder != null) {
                session.move(node.getPath(), targetFolder + "/" + id )
            }
            
        }
        
        session.save()
        
    } 
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
        processChild(item)
        
        if(i++ > NODE_LIMIT) break
    }
    /*resource.getChildren().each() {  
        processChild(it)  
        if()
    } */ 
}