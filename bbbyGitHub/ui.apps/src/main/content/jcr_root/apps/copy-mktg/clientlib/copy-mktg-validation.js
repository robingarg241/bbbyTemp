(function(ns, window, document, $, Granite, undefined) {
	'use strict';

	$.validator.register({
		selector : '[name="./copy-mktg"]',
		validate : function(el) {
			var error, activeValidation;

			activeValidation = el.data('activevalidation');
		}
	});
})(window.aemTouchUIValidation = window.aemTouchUIValidation || {}, window,
		document, Granite.$, Granite);