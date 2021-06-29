;(function ($, ns, channel) {
    "use strict";

    channel.on("cq-layer-activated", function(e) {
        if (e.layer === "Edit") {
            $("#ContentFrame").contents().find('.edit-start, .edit-end').show();
        } else if (e.layer === "Preview") {
            $("#ContentFrame").contents().find('.edit-start, .edit-end').hide();
        } else if (e.layer === "Design") {
            $("#ContentFrame").contents().find('.edit-start, .edit-end').show();
        }
    });

    channel.on("cq-layer-activated", function(e) {
        var $body = $("#ContentFrame").contents().find('body');

        $body.removeClass('edit preview design publish');
        if (e.layer === "Edit") {
            $body.addClass('edit');
        } else if (e.layer === "Preview") {
            $body.addClass('preview');
        } else if (e.layer === "Design") {
            $body.addClass('design');
        } else {
            $body.addClass('publish');
        }
    });

}(jQuery, Granite.author, jQuery(document)));
