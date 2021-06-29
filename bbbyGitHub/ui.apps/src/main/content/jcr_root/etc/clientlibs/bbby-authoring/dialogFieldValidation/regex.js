
(function(window, $) {
    'use strict';

    /*
     * Generic regular expression validation for text fields.
     *
     * Dialog field requires the data-foundation-validation attribute
     * containing 'regex' and a data-regex attribute with the regular
     * expression.
     *
     * Example:
     * @DialogField(fieldLabel = "Name", name = "./name", ranking = 100,
     *     additionalProperties={
     *             @Property(name = "regex", value = "^data-.+$"),
     *             @Property(name = "foundation-validation", value = "regex")})
     */
    $(window).adaptTo("foundation-registry").register("foundation.validation.validator", {
        selector: "[data-foundation-validation~='regex'],[data-validation~='regex']",
        validate: function(el) {
            var $el = $(el);
            var v = $el.val();
            var regexStr = $el.data('regex');

            if (!regexStr) {
                console.log('Missing regex string for ', el);
                return;
            }

            try {
                var re = new RegExp(regexStr);

                if (!v) v = '';

                if (v.match(re)) {
                    // valid
                    $el.closest('form').find('.cq-dialog-submit').prop('disabled', false);
                    return;
                } else {
                    // invalid
                    $el.closest('form').find('.cq-dialog-submit').prop('disabled', true);
                    return 'Value [' + v + '] is not valid.';
                }
            } catch (e) {
                console.log(e);
            }
        }
    });

})(window, Granite.$);
