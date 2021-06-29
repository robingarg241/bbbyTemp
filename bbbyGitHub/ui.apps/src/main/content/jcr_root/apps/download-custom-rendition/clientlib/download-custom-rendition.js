(function ($, $document) {
    var BUTTON_URL_DOWNLOAD_CUSTOM_RENDITION = "/apps/download-custom-rendition/content/download-custom-rendition-but.html",
    	DOWNLOAD_CUSTOM_RENDITION_ACTIVATOR= "cq-damadmin-admin-actions-download-custom-rendition-activator",
        SHARE_ACTIVATOR = "cq-damadmin-admin-actions-adhocassetshare-activator",
        DOWNLOAD_CUSTOM_RENDITION_SERVLET = "/bin/bbby/download-custom-rendition.zip",
        DOWNLOAD_CUSTOM_RENDITION_URL = "/apps/download-custom-rendition/download-custom-rendition-dialog.html",
        CANCEL_CSS = "[data-foundation-wizard-control-action='cancel']",
        SENDER = "experience-aem", REQUESTER = "requester", $mailModal,
        url = document.location.pathname;

    if( url.indexOf("/assets.html") == 0 || url.indexOf("/aem/search.html") == 0 ){
        $document.on("foundation-selections-change", addSendMail);
    }else if(url.indexOf(DOWNLOAD_CUSTOM_RENDITION_URL) == 0){
        handleSendMailDialog();
    }

    function handleSendMailDialog(){
        $document.on("foundation-contentloaded", fillDefaultValues);

        $document.on("click", CANCEL_CSS, sendCancelMessage);

        $document.submit(sendMailSentMessage);
    }

    function sendMailSentMessage(){
        var message = {
            sender: SENDER,
            action: "send"
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

        if (_.isEmpty(event.data) || _.isEmpty($mailModal)) {
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

        var modal = $mailModal.data('modal');

        if(message.action == "send"){
           // post();
            post();
        }

        modal.hide();
        modal.$element.remove();

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

    function fillDefaultValues(){
        var queryParams = queryParameters(),
            form = $("form")[0];

        setWidgetValue(form, "[name='./assets']", queryParams.assets);
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

        return result;
    }

    function addSendMail(){
        var showButton = true;

        var $items = $(".foundation-selections-item");

        var count = 0;

        $items.each(function () {
        	var assetPath = $(this).data("foundationCollectionItemId");
        	//Temporary removed the Download Custom Rendition functionality 
			if(!assetPath.startsWith("/content/dam/ttemp")){
				showButton = false;
            }
            count++;
        });
        
        //first remove the notify button and then add.
        var $notifyActivator = $("." + DOWNLOAD_CUSTOM_RENDITION_ACTIVATOR);
        $notifyActivator.remove();

        if(showButton && count >= 1){
        	$.ajax(BUTTON_URL_DOWNLOAD_CUSTOM_RENDITION).done(addButton);
        }
    }

    function addButton(html) {
        var $eActivator = $("." + SHARE_ACTIVATOR);

        if ($eActivator.length == 0) {
            return;
        }

        var $mail = $(html).css("margin-left", "20px").insertBefore($eActivator);

        $mail.click(openModal);
        
        $("." + DOWNLOAD_CUSTOM_RENDITION_ACTIVATOR).not(':first').remove();//to remove all except one

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

        showMailModal(getModalIFrameUrl("AEM ALERT: Please Review Activity", assets));
    }

    function showMailModal(url){
        var $iframe = $('<iframe id ="myIFrame">'),
            $modal = $('<div id ="myIFrameDiv">').addClass('eaem-download-custom-rendition-apply-modal coral-Modal');

        $iframe.attr('src', url).appendTo($modal);

        $modal.appendTo('body').modal({
            type: 'default',
            buttons: [],
            visible: true
        });

        $mailModal = $modal;

       // $mailModal.mailSentMessage = mailSentMessage;
    }

      // Post to the provided URL with the specified parameters.
    function post1() {
        var filepath = $('#myIFrame').contents().find('#selectedAssetId').val();
		var patharr;
        patharr = [];
        patharr.push(encodeURIComponent(filepath));

 		var params = {
                "path": patharr,
 	 	        "_charset_": encodeURIComponent("utf-8"),
                //"downloadAssets": encodeURIComponent($(".asset-select [type='checkbox']", container).prop("checked")),
               // "downloadRenditions": encodeURIComponent($(".rendition-select [type='checkbox']", container).prop("checked")),
               // "downloadSubassets":  encodeURIComponent($(".subasset-select [type='checkbox']", container).prop("checked")),
               // "flatStructure":encodeURIComponent(!($(".flatstructure-select [type='checkbox']", container).prop("checked"))),
                "licenseCheck": encodeURIComponent("false")
        }

        var filename = "xyz.zip";

        var url = filepath + ".assetdownload.zip/" + encodeURIComponent(filename).replace(/%2F/g, "/");
        url = Granite.HTTP.externalize(url);

        $('#myIFrame').contents().find('.flatstructure-select').each(function() {
            params[$(this).attr('name')] = $(this).attr('checked');
        });

        params["assetspaths"] = $('#myIFrame').contents().find('#selectedAssetId').val();
        params["width"] = $('#myIFrame').contents().find("input[name=width]").val();
        params["height"] = $('#myIFrame').contents().find("input[name=height]").val();
        params["resolution"] = $('#myIFrame').contents().find("input[name=resolution]").val();

        var s7ExportSettings = "{is_modifier:printRes=120&wid=123&hei=173&fmt=jpeg,RGB&icc=Adobe RGB (1998),relative,1,0&iccEmbed=1}";
        alert(s7ExportSettings);
        if (typeof s7ExportSettings !== 'undefined' && s7ExportSettings !== null) {
           //s7ExportSettings is encoded in the addParameter() method
            params["s7exportsettings"]= encodeURIComponent(s7ExportSettings);
        }

        var form = $('<form></form>');
        form.attr("method", "post");
        form.attr("action", url);
        form.attr("target","_blank");
        $.each(params, function(key, value) {
            if ($.isArray(value)){
                $.each(value, function(keyArray, valueArray){
                        var field = $('<input></input>');
                        field.attr("type", "hidden");
                        field.attr("name", key);
                        field.attr("value", valueArray);
                        form.append(field);
                    }
                );
            }
            else {
                var field = $('<input></input>');
                field.attr("type", "hidden");
                field.attr("name", key);
                field.attr("value", value);
                form.append(field);
            }
        });
        // The form needs to be a part of the document in
        // order for us to be able to submit it.
        $(document.body).append(form);
        form.submit();
        setTimeout(callback,100);
    };


    // Post to the provided URL with the specified parameters.
    function post() {
        var params = {};

        $('#myIFrame').contents().find('.flatstructure-select').each(function() {
            params[$(this).attr('name')] = $(this).attr('checked');
        });

        params["assetspaths"] = $('#myIFrame').contents().find('#selectedAssetId').val();
        params["width"] = $('#myIFrame').contents().find("input[name=width]").val();
        params["height"] = $('#myIFrame').contents().find("input[name=height]").val();
        params["resolution"] = $('#myIFrame').contents().find("input[name=resolution]").val();

        var form = $('<form></form>');
        form.attr("method", "get");
        form.attr("action", Granite.HTTP.externalize(DOWNLOAD_CUSTOM_RENDITION_SERVLET));
        form.attr("target","_blank");
         $.each(params, function(key, value) {
            if ($.isArray(value)){
                $.each(value, function(keyArray, valueArray){
                        var field = $('<input></input>');
                        field.attr("type", "hidden");
                        field.attr("name", key);
                        field.attr("value", valueArray);
                        form.append(field);
                    }
                );
            }
            else {
                var field = $('<input></input>');
                field.attr("type", "hidden");
                field.attr("name", key);
                field.attr("value", value);
                form.append(field);
            }
        });

        // The form needs to be a part of the document in
        // order for us to be able to submit it.
        $(document.body).append(form);
        form.submit();
       // setTimeout(callback,100);
    };

    function getModalIFrameUrl(subject, assets){
        var url = Granite.HTTP.externalize(DOWNLOAD_CUSTOM_RENDITION_URL) + "?" + REQUESTER + "=" + SENDER;
        url = url + "&assets=" + encodeURIComponent(assets);
        return url;
    }

    function checkboxShowHideHandler(el) {
 		alert("checkboxShowHideHandler");
        alert($(el).tagName);
        $('#myIFrame').contents().find('.flatstructure-select').each(function() {
            alert("checkboxShowHideHandler");
        });
        $('#myIFrame').contents().find('.flatstructure-select').each(function (i, element) {
            alert("checkboxShowHideHandler1");
            if($(element).is("coral-checkbox")) {
                // handle Coral3 base drop-down
                Coral.commons.ready(element, function (component) {
                    showHide(component, element);
                    component.on("change", function () {
                        showHide(component, element);
                    });
                });
            } else {
                // handle Coral2 based drop-down
                var component = $(element).data("checkbox");
                if (component) {
                    showHide(component, element);
                }
            }
        })
    }

    function showHide(component, element) {
        console.log('showing');
        // get the selector to find the target elements. its stored as data-.. attribute
        var target = $(element).data("cqDialogCheckboxShowhideTarget");
        var $target = $(target);

        if (target) {
            $target.addClass("hide");
            if (component.checked) {
                $target.removeClass("hide");
            }
        }
    }

}(jQuery, jQuery(document)));