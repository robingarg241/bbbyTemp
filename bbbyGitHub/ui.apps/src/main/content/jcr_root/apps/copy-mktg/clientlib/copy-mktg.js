(function ($, $document) {
    var BUTTON_URL_NEW = "/apps/copy-mktg/content/copy-mktg-but.html",
    	NOTIFY_ACTIVATOR= "cq-damadmin-admin-actions-copy-mktg-activator",
        SHARE_ACTIVATOR = "cq-damadmin-admin-actions-adhocassetshare-activator",
        SEND_MAIL_SERVLET = "/bin/bbby/copy-mktg",
        SEND_MAIL_URL = "/apps/copy-mktg/copy-mktg-dialog.html",
        CANCEL_CSS = "[data-foundation-wizard-control-action='cancel']",
        SENDER = "experience-aem", REQUESTER = "requester", $mailModal,
        url = document.location.pathname;


    if( url.indexOf("/assets.html") == 0 || url.indexOf("/aem/search.html") == 0 ){
        $document.on("foundation-selections-change", addSendMail);
    }else if(url.indexOf(SEND_MAIL_URL) == 0){
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
        if(modal != undefined) {
            modal.hide();
            modal.$element.remove();
        }

        if(message.action == "send"){
          //  showAlert("Email sent...", $mailModal.mailSentMessage);
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
			if(!assetPath.startsWith("/content/dam/marketing")){
				showButton = false;
            }
            count++;
        });

        //first remove the notify button and then add.
        var $notifyActivator = $("." + NOTIFY_ACTIVATOR);
        $notifyActivator.remove();

        if(showButton && count > 0){
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

        var $mail = $(html).css("margin-left", "20px").insertBefore($eActivator);

        $mail.click(openModal);

        $("." + NOTIFY_ACTIVATOR).not(':first').remove();//to remove all except one

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

        showMailModal(getModalIFrameUrl(assets));
    }

    function showMailModal(url){
        var $iframe = $('<iframe>'),
            $modal = $('<div>').addClass('eaem-copy-mktg-apply-modal coral-Modal');

        $iframe.attr('src', url).appendTo($modal);

        $modal.appendTo('body').modal({
            type: 'default',
            buttons: [],
            visible: true
        });

        $mailModal = $modal;

    }

    function getModalIFrameUrl(assets){
        var url = Granite.HTTP.externalize(SEND_MAIL_URL) + "?" + REQUESTER + "=" + SENDER;

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
