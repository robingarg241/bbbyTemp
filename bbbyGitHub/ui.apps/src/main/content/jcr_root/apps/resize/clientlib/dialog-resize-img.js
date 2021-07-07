(function ($, $document) {

    var BUTTON_URL_NEW = "/apps/resize/content/resize-but.html",
    	RESIZE_ACTIVATOR= "cq-damadmin-admin-actions-resize-img-activator",
        SHARE_ACTIVATOR = "cq-damadmin-admin-actions-adhocassetshare-activator",
        RESIZE_IMG_URL = "/apps/resize/resize-dialog.html",
        CANCEL_CSS = "[data-foundation-wizard-control-action='cancel']",
        SENDER = "experience-aem", REQUESTER = "requester", $resizeModal,
        url = document.location.pathname;

    if( (url.indexOf("/assets.html") == 0 || url.indexOf("/aem/search.html") == 0) ) {
        $document.on("foundation-selections-change", addResizeButton);
    } else if(url.indexOf(RESIZE_IMG_URL) == 0) {
        handleResizeDialog();
    }

    function handleResizeDialog() {
        $document.on("foundation-contentloaded", fillDefaultValues);

        $document.on("click", CANCEL_CSS, sendCancelMessage);

        $(document).on("keyup change mousewheel", "input[name='./width']", changeHeight);
        $(document).on("keyup change mousewheel", "input[name='./height']", changeWidth);

        $document.submit(sendMailSentMessage);
    }

    function changeHeight(){
        var originalWidth = $("input[name='./original-width']").val();
        var originalHeight = $("input[name='./original-height']").val();
        var height = (parseInt($(this).val()) * originalHeight) / originalWidth;
        $("input[name='./height']").val(height);
    }

    function changeWidth(){
        var originalWidth = $("input[name='./original-width']").val();
        var originalHeight = $("input[name='./original-height']").val();
        var width = (parseInt($(this).val()) * originalWidth) / originalHeight;
        $("input[name='./width']").val(width);
    }

    function sendMailSentMessage(){
        var message = {
            sender: SENDER,
            action: "close"
        };

        getParent().postMessage(JSON.stringify(message), "*");
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

    function closeModal(event){
        event = event.originalEvent || {};

        if (_.isEmpty(event.data) || _.isEmpty($resizeModal)) {
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

        var modal = $resizeModal.data('modal');
        modal.hide();
        modal.$element.remove();

        if(message.action == "close"){
          //  showAlert("Email sent...", $resizeModal.mailSentMessage);
        }
    }

    function showAlert(message, title, callback){
        var fui = $(window).adaptTo("foundation-ui"),
            options = [{
                id: "ok",
                text: "OK",
                primary: true
            }];

        message = message || "Unknown Error";
        title = title || "Error";

        fui.prompt(title, message, "default", options, callback);
    }

    function fillDefaultValues() {

        var queryParams = queryParameters(),
            form = $("form")[0];

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
            result["originalWidth"] = assetDetails["tiff:ImageWidth"];
            result["originalHeight"] = assetDetails["tiff:ImageLength"];
        }

        return result;
    }

    function addResizeButton(){
        var showButton = true,
            $items = $(".foundation-selections-item"),
            count = 0;

        $items.each(function () {
        	var assetPath = $(this).data("foundationCollectionItemId");
			if(!assetPath.startsWith("/content/dam")){
				showButton = false;
            }
            count++;
        });

        //first remove the resize button and then add.
        var $resizeActivator = $("." + RESIZE_ACTIVATOR);
        $resizeActivator.remove();

        if(showButton && count == 1){
        	$.ajax(BUTTON_URL_NEW).done(addButton);
        }
    }

    function addButton(html) {
        var $eActivator = $("." + SHARE_ACTIVATOR);

        if ($eActivator.length == 0) {
            return;
        }

        var $resize = $(html).css("margin-left", "20px").insertBefore($eActivator);

        $resize.click(openModal);

        $("." + RESIZE_ACTIVATOR).not(':first').remove();//to remove all except one

        $(window).off('message', closeModal).on('message', closeModal);
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
            assetNames.push("<"+assetName+">");
        });

        var assets = assetPaths.join("\n");

        showResizeModal(getModalIFrameUrl(assets), actionConfig.data.text);
    }

    function showResizeModal(url, mailSentMessage){
        var $iframe = $('<iframe>'),
            $modal = $('<div>').addClass('eaem-resize-image-apply-modal coral-Modal');

        $iframe.attr('src', url).appendTo($modal);

        $modal.appendTo('body').modal({
            type: 'default',
            buttons: [],
            visible: true
        });

        $resizeModal = $modal;

        $resizeModal.mailSentMessage = mailSentMessage;
    }

    function getModalIFrameUrl(assets){
        var url = Granite.HTTP.externalize(RESIZE_IMG_URL) + "?" + REQUESTER + "=" + SENDER;
        url = url + "&assets=" + encodeURIComponent(assets);
        return url;
    }

    function getLoggedInUserID() {
        var currentUserId = "";
        var currentUserInfo;
        var CURRENT_USER_JSON_PATH = Granite.HTTP.externalize('/libs/granite/security/currentuser.json');

        var result = Granite.$.ajax({
            type: "GET",
            async: false,
            url: CURRENT_USER_JSON_PATH
        });

        if (result.status === 200) {
            currentUserInfo = JSON.parse(result.responseText);
            currentUserId = currentUserInfo.authorizableId;
        }

        return currentUserId;
    }
}(jQuery, jQuery(document)));
