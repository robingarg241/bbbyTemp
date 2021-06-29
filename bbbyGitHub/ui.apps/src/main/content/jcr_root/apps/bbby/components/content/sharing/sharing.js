'use strict';

/**
 *  This is the main file for share
 */


import $ from 'jquery'
import log from 'loglevel'

(function ($) {
    if ($('.cmp-sharing').length) {


        window.init = (function (d, s, id) {
            //removing tailing string from pinterest thumbnail image
            try {
                var imgUrl = d.getElementById("shareOnPinterest").getAttribute("data-pin-media");
                if (imgUrl != undefined) {
                    imgUrl = imgUrl.split('?ck')[0];
                }
                d.getElementById("shareOnPinterest").setAttribute("data-pin-media", imgUrl);
            } catch (err) {
                console.log("An exception has occured while removing tailing string from img url with image: "
                    + err.message);
            }

            var js, fjs = d.getElementsByTagName(s)[0],
                t = window.init || {};
            if (d.getElementById(id)) return t;
            js = d.createElement(s);
            js.id = id;
            js.src = "https://platform.twitter.com/widgets.js";
            fjs.parentNode.insertBefore(js, fjs);

            t._e = [];
            t.ready = function (f) {
                t._e.push(f);
            };

            return t;
        }(document, "script", "twitter-wjs"));
    }
})(jQuery);
