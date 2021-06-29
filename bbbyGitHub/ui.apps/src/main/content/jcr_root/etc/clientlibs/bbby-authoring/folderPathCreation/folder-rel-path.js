(function($, $document) {
    var FILE_PATH = "./jcr:content/metadata/dam:path",
        initialized = false;

    $document.on("foundation-contentloaded", init);

    function init(){
        if(initialized){
            return;
        }
        initialized = true;
        convertActualPath();
    }

    function convertActualPath(){
    	try{
	        var host = location.protocol + "//" + location.host;
	        var $path = $("[name='"+FILE_PATH+"']");
	        var $relPath = $("[name='./jcr:content/dam:relativePath']");
			console.log("host is..."+host);
	 		var relPathValue = $relPath.val();
	        console.log("relPathValue..."+relPathValue);
	        if (relPathValue){
				relPathValue = relPathValue.substring(0, relPathValue.lastIndexOf("/") + 1);
		
		        if(relPathValue.includes("/assets.html/content/dam/")){
		        }
		        else{
		        	relPathValue = host+'/assets.html/content/dam/'+relPathValue;
		        }
		        $path.val(!relPathValue ?  "Parent Path Unavailable" : "Click for Parent Folder");
		        $($path).css('cursor', 'pointer')
		 	    $($path).attr('readonly','readonly');
		        $($path).css('text-decoration', 'underline')
		        $($path).css('font-size', '16px')
		        $($path).css('background-color', 'transparent')
		        $($path).css('margin-top', '23px')
				$($path).css('padding-left', '0px')
		     	$($path).css('margin-bottom', '10px')
		        $($path).css('color', 'gray')
		        $($path).css('font-weight', 'bold')
				$path.click(function(){
				window.open(relPathValue, '_blank');
				});
	        }else {
	        	$($path).hide();
	        }
	        $($relPath).hide();
    	}catch (e) {
    		console.log(e);
    	}
    }

}(jQuery, jQuery(document)));