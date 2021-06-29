/*
================================================================================

================================================================================
*/
;(function ($, channel) {
    "use strict";

    channel.on("cq-persistence-before-create", function(e) {
        console.log(e)
    });

    function refreshPage() {
        if (window.console) console.log('Will perform jcr:content/par refresh');

        var path = $("#ContentFrame").contents().find('cq[data-path$="/jcr:content/par"]').data('path');
        if (!path) {
            if (window.console) console.log('Could not find main jcr:content/par in order to do refresh!');
            return;
        }

        Granite.author.editables.find(path)[0].refresh();
    }
}(jQuery, jQuery(document)));
