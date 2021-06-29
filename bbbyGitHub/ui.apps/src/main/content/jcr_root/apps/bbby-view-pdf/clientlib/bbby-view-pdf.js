(function ($, $document) {
    var BUTTON_URL_PDF = "/apps/bbby-view-pdf/content/bbby-view-pdf-but.html",
    	PDF_ACTIVATOR= "cq-damadmin-admin-actions-bbby-view-pdf-activator",
        SHARE_ACTIVATOR = "cq-damadmin-admin-actions-adhocassetshare-activator",
        DOWNLOAD_ACTIVATOR = "cq-damadmin-admin-actions-download-activator",
        CREATE_PDF_URL = "/bin/bbby/bbby-view-pdf?assetPaths=",
        SENDER = "experience-aem", REQUESTER = "requester",
        url = document.location.pathname;

	if (url.indexOf("/assets.html") == 0 || url.indexOf("/aem/search.html") == 0 || url.indexOf("/mnt/overlay/dam/gui/content/collections/collectiondetails.html") == 0) {
		$document.on("foundation-selections-change", addViewPDFButton);
	} else if (url.indexOf("/mnt/overlay/dam/gui/content/assets/metadataeditor.external.html") == 0) {
		$document.on("foundation-contentloaded", addViewPDFButton);
	} else if (url.indexOf("/assetdetails.html") == 0 && url.toLowerCase().indexOf(".pdf") >= 0) {
		$document.on("foundation-contentloaded", addViewPDFButtonDetail);
	}

    function addViewPDFButton(){
        var showButton = true;

        var $items = $(".foundation-selections-item");

        var count = 0;

        $items.each(function () {
        	var assetPath = $(this).data("foundationCollectionItemId");
			if(!assetPath.startsWith("/content/dam") || !assetPath.toLowerCase().includes("PDF".toLowerCase())){
				showButton = false;
            }
            count++;
        });
        
        //first remove the view pdf button and then add.
        var $pdfActivator = $("." + PDF_ACTIVATOR);
        $pdfActivator.remove();

        if(showButton && count == 1){
        	$.ajax(BUTTON_URL_PDF).done(addButton);
        }
    }
    
    function addViewPDFButtonDetail(){

        var showButton = true;
        var $items = $(".allsets-list-info");
        var count = 0;

        $items.each(function () {
        	var assetPath = $(this).data("asset");
            count++;
        });
        
        //first remove the view pdf button and then add.
        var $pdfActivator = $("." + PDF_ACTIVATOR);
        $pdfActivator.remove();

        if(showButton && count == 1){
        	$.ajax(BUTTON_URL_PDF).done(addButton);
        }
    }

    function addButton(html) {
        var $eActivator = $("." + SHARE_ACTIVATOR);

        if ($eActivator.length == 0) {
            $eActivator = $("." + DOWNLOAD_ACTIVATOR);
            if ($eActivator.length == 0) {
            	return;
            }
        }

        var $pdf = $(html).css("margin-left", "20px").insertBefore($eActivator);

        $pdf.click(viewPDF);
    }

    function viewPDF() {
    	var $items,
         	assetPaths = [];

		if (url.indexOf("/assetdetails.html") == 0 && url.toLowerCase().indexOf(".pdf") >= 0) {
			$items = $(".allsets-list-info");
			$items.each(function() {
				assetPaths.push($(this).data("asset"));
			});
		} else {
			$items = $(".foundation-selections-item");
			$items.each(function() {
				assetPaths.push($(this).data("foundationCollectionItemId"));
			});
		}
		window.open(CREATE_PDF_URL + assetPaths.join(","), "_blank");
    }

}(jQuery, jQuery(document)));