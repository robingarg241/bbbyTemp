(function ($, $document) {

    var BUTTON_URL_NEW = "/apps/dim-calc/content/dim-calc-but.html",
    	DIM_CAL_ACTIVATOR= "cq-damadmin-admin-actions-dimension-calculator-activator",
        SHARE_ACTIVATOR = "cq-damadmin-admin-actions-adhocassetshare-activator",
        DIM_CAL_URL = "/apps/dim-calc/dim-calc-dialog.html",
        CANCEL_CSS = "[data-foundation-wizard-control-action='cancel']",
        SENDER = "experience-aem", REQUESTER = "requester", $dimensionCalcModal,
        url = document.location.pathname;

    if( (url.indexOf("/assets.html") == 0 || url.indexOf("/aem/search.html") == 0) ) {
        $document.on("foundation-selections-change", addDimCalcButton);
    } else if(url.indexOf(DIM_CAL_URL) == 0) {
        handleDimCalDialog();
    }

    function handleDimCalDialog() {
        $document.on("foundation-contentloaded", fillDefaultValues);

        $document.on("click", CANCEL_CSS, sendCancelMessage);

        $(document).on("keyup change mousewheel", "input[name='./width']", changeHeight);
        $(document).on("keyup change mousewheel", "input[name='./height']", changeWidth);

    }

    function changeHeight(){
        var originalWidth = $("input[name='./original-width']").val();
        var originalHeight = $("input[name='./original-height']").val();
        var thisWidth = $(this).val();
        var height = parseInt(thisWidth) > 0 ? (parseInt(thisWidth) * originalHeight) / originalWidth : 0;
        $("input[name='./height']").val(Math.round(height));
    }

    function changeWidth(){
        var originalWidth = $("input[name='./original-width']").val();
        var originalHeight = $("input[name='./original-height']").val();
        var thisHeight = $(this).val();
        var width = parseInt(thisHeight) > 0 ? (parseInt(thisHeight) * originalWidth) / originalHeight : 0;
        $("input[name='./width']").val(Math.round(width));
    }

    function sendCancelMessage(){
        var message = {
            sender: SENDER,
            action: "cancel"
        };

        getParent().postMessage(JSON.stringify(message), "*");
    }

    function getParent() {
        if (window.opener) {
            return window.opener;
        }
        return parent;
    }

    function closeDimCalcModal(event){
        event = event.originalEvent || {};

        if (_.isEmpty(event.data) || _.isEmpty($dimensionCalcModal)) {
            return;
        }

        var message, action;

        try{
            message = JSON.parse(event.data);
        }catch(err){
            return;
        }

        if (!message || message.sender !== SENDER) {
            return;
        }

        var modal = $dimensionCalcModal.data('modal');
        if(modal != undefined) {
            modal.hide();
            modal.$element.remove();
        }

    }

    function fillDefaultValues() {

        var queryParams = queryParameters(),
            form = $("form")[0];

        setWidgetValue(form, "[name='./img-name']", [queryParams.assetName]);
        setWidgetValue(form, "[name='./original-width']", [queryParams.originalWidth]);
        setWidgetValue(form, "[name='./original-height']", [queryParams.originalHeight]);
    }

    function setWidgetValue(form, selector, value){

        Coral.commons.ready(form.querySelector(selector), function (field) {
            field.value = _.isEmpty(value) ? "" : decodeURIComponent(value);
        });
    }

    function queryParameters() {
        var result = {}, param,
            params = document.location.search.split(/\?|\&/);

        params.forEach( function(it) {
            if (_.isEmpty(it)) {
                return;
            }
            param = it.split("=");
            result[param[0]] = param[1];
        });

        var assetDetails = Granite.$.ajax({
            type: "GET",
            async: false,
            url: document.location.origin + decodeURIComponent(result.assets) + ".infinity.json"
        });

        if(assetDetails && assetDetails.responseJSON) {
            assetDetails = assetDetails["responseJSON"]["jcr:content"]["metadata"];
            if(assetDetails["tiff:ImageWidth"] && assetDetails["tiff:ImageLength"]) {
                result["originalWidth"] = assetDetails["tiff:ImageWidth"];
                result["originalHeight"] = assetDetails["tiff:ImageLength"];
            } else {
                $(".form-textfield-original-width-height-alert").show();
            }
        }

        return result;
    }

    function addDimCalcButton(){
        var showButton = true,
            $items = $(".foundation-selections-item"),
            count = 0;

        $items.each(function () {
        	var assetPath = $(this).data("foundationCollectionItemId");
        	var regex = /([/|.|\w|\s|-])*\.(?:[a-z]*)/g;
        	var found = assetPath.match(regex);

			if(!assetPath.startsWith("/content/dam")){
				showButton = false;
            } else if(assetPath.startsWith("/content/dam") && found == null){
                showButton = false;
            }
            count++;
        });

        //first remove the dimension Calculator button and then add.
        var $dimCalcActivator = $("." + DIM_CAL_ACTIVATOR);
        $dimCalcActivator.remove();

        if(showButton && count == 1){
        	$.ajax(BUTTON_URL_NEW).done(addButton);
        }
    }

    function addButton(html) {
        var $eActivator = $("." + SHARE_ACTIVATOR);

        if ($eActivator.length == 0) {
            return;
        } else if($eActivator.length > 1) {
            $eActivator.each(function (index, element) {
                if(!$(element).hasClass("foundation-collection-action-hidden")) {
                    $eActivator = $(element);
                    return false;
                }
            });
        }

        var $dimCalc = $(html).css("margin-left", "20px").insertBefore($eActivator);

        $dimCalc.click(openModal);

        $("." + DIM_CAL_ACTIVATOR).not(':first').remove();//to remove all except one

        $(window).off('message', closeDimCalcModal).on('message', closeDimCalcModal);
    }

    function openModal(){
        var actionConfig = ($(this)).data("foundationCollectionAction");

        var folderName = "";

        var $items = $(".foundation-selections-item"),
            assetPaths = [],
            assetNames = [];

        $items.each(function () {
        	var assetPath = $(this).data("foundationCollectionItemId");
        	var assetName = assetPath.substr(assetPath.lastIndexOf("/") + 1);
        	folderName = assetPath.substr(0, assetPath.lastIndexOf("/"));
            assetPaths.push(assetPath);
            assetNames.push(assetName);
        });

        var assets = assetPaths.join("\n");
        var assetName = assetNames.join("\n");

        showDimCalcModal(getModalIFrameUrl(assets, assetName), actionConfig.data.text);
    }

    function showDimCalcModal(url, mailSentMessage){
        var $iframe = $('<iframe>'),
            $modal = $('<div>').addClass('eaem-dimension-calculator-apply-modal coral-Modal');

        $iframe.attr('src', url).appendTo($modal);

        $modal.appendTo('body').modal({
            type: 'default',
            buttons: [],
            visible: true
        });

        $dimensionCalcModal = $modal;

        $dimensionCalcModal.mailSentMessage = mailSentMessage;
    }

    function getModalIFrameUrl(assets, assetName){
        var url = Granite.HTTP.externalize(DIM_CAL_URL) + "?" + REQUESTER + "=" + SENDER;
        url = url + "&assets=" + encodeURIComponent(assets) + "&assetName=" + assetName;
        return url;
    }

}(jQuery, jQuery(document)));
