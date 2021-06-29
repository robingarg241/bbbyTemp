/*
================================================================================
This file handles initializing and tearing down the carousel component as the
use switches between edit, design, and preview modes.

Listens for the "cq-layer-activated" event in order to trigger the logic.

Refer to LayerManager.js for details on "cq-layer-activated" event:
/libs/cq/gui/components/authoring/editors/clientlibs/core/js/layers/LayerManager.js
================================================================================
*/
;(function ($, channel) {
    "use strict";

    channel.on("cq-layer-activated", function(e) {
        // First get the jquery object that the iframed application has loaded.
        // This is needed because the application has the jQuery object that
        // has the Owl plugin loaded.
        var $j = $("#ContentFrame")[0].contentWindow.$;
        if (!$j) return;

        var $carousels = $("#ContentFrame").contents().find('.cmp-carousel');
        var teardown = false;
        $carousels.each(function() {
            switch(e.layer) {
                case "Edit":
                    teardownCarousel($j(this));
                    teardown = true;
                    break;
                case "Design":
                    teardownCarousel($j(this));
                    teardown = true;
                    break;
                case "Preview":
                    initCarousel($j(this));
                    break;
            }
        });

        if (teardown & e.layer != e.prevLayer) {
            // initCarousel(), which calls removeInvalidSlides(), will end up
            // deleting the parsys dropzone elements in order to prevent
            // them from showing up as empty slides.
            // If you switch back to Edit mode (from Preview) you are then
            // still missing these elements.
            // refreshPage() will restore these missing elements.
            refreshPage();
        }
    });

    function teardownCarousel($carousel) {
        if (!isOwlified($carousel)) return;
        $carousel.data('owl.carousel').destroy();
    }

    function initCarousel($carousel) {
        if (isOwlified($carousel)) return;

        var doc = $carousel[0].ownerDocument;
        var win = doc.defaultView || doc.parentWindow;

        // reuse our standard carousel init function
        win.carousel.init($carousel);

        removeInvalidSlides($carousel);
    }

    function isOwlified($carousel) {
        if (!$carousel.hasClass('owl-loaded')) return false;
        return true;
    }

    /**
        Preview mode in AEM creates extra divs (things like a parsys for dragging a
        new component). This function removes any slides that resulted from those
        author only divs.

        This code should only affect preview mode. AEM does not pump out the invalid
        divs in publish mode.

        @param $carousel jQuery object representing a single instance of a carousel
    */
    function removeInvalidSlides($carousel) {
        var $slides = $carousel.find('.owl-item:not(.cloned)');

        var slidesRemoved = false;
        for (var i = $slides.length - 1; i >= 0; i--) {
            var remove = null;

            var $slide = $($slides[i]);
            if ($slide.children('.new.section').length) {
                remove = 'author only new section div'
            }
            if ($slide.children('cq').length) {
                remove = 'author only cq div';
            }

            if (remove) {
                slidesRemoved = true;
                if (window.console) console.log('Removing #' + i + ' slide: ' + remove);
                $carousel.data('owl.carousel').remove(i);
            }
        }

        if (slidesRemoved) {
            // Refresh the carousel for slide deletions to fully take effect
            $carousel.trigger('refresh.owl.carousel');
        }
    }

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
