;(function ($, ns, channel, window, undefined) {

    var emptyPlaceHolderClass = 'cq-placeholder';

    var hasPlaceholderBase = ns.Inspectable.prototype.hasPlaceholder;

    /**
     * Customized implementation which adds empty placholder support for components wrapped in a background component.
     * @returns {false|String} Returns the placeholder string or false if no placeholder exists
     */
    ns.Inspectable.prototype.hasPlaceholder = function() {
        var baseValue = hasPlaceholderBase.call(this);

        // If base already returned a value, no need to continue
        if (baseValue) return baseValue;

        // Handle case where background component is wrapping an empty component.
        // If this is the case, emptyPlaceholder will be either 2 or 3 levels below the cmp-background
        var emptyPlaceholder = this.dom.find('> .cmp-background > > .' + emptyPlaceHolderClass);
        if (!emptyPlaceholder.length) {
            emptyPlaceholder = this.dom.find('> .cmp-background > > > .' + emptyPlaceHolderClass);
        }

        return emptyPlaceholder.length ? Granite.I18n.getVar(emptyPlaceholder.data('emptytext')) : false;
    };

}(jQuery, Granite.author, jQuery(document), this));
