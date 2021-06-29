/*
 * Moves assets according to predefined folder structure.
 * for example 2019-08-30-09-49_wwww_perf_testimage_9.jpg will be moved to /2019-08-30/noritake/019-08-30-09-49_wwww_perf_testimage_9.jpg 
 */

import com.day.cq.commons.jcr.JcrUtil

def ROOT_PATH = "/content/dam/bbby/asset_transitions_folder/vendor/not_assigned"  
// /content/dam/bbby/asset_transitions_folder/e-comm/rejects_folder
//def ROOT_PATH = "/content/dam/bbby/asset_transitions_folder/e-comm/rejects_folder"  


  
def boolean processChild(resource) {  
    if (resource == null) return  
      
    if (resource.getResourceType() == "dam:Asset") {   
        
        return true
    } 
    
    return false
}  

  
Resource resource = resourceResolver.getResource(ROOT_PATH)  
  
if (resource != null) {  
    
    def i = 0
    
    resource.getChildren().each() {  
        if(processChild(it)) {
            i++
        }  
        
    }
    
    println("Total files remaining: " + i)
}