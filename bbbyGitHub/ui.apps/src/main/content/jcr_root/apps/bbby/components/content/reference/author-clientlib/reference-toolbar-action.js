/**
 * Adds a toolbar action to the reference component that will open the page
 * containing the referenced component in a new tab.
 */
(function ($, ns, channel, window, undefined) {
    
    // Add supported components here
    var SUPPORTED_COMPONENTS = [
        'bbby/components/content/reference'
    ]
    var ACTION_ICON = "coral-Icon--alias";
    var ACTION_NAME = "OPEN_REFERENCE";

    /**
     * Defines the "Open Reference" Toolbar Action
     *
     * @type {Granite.author.ui.ToolbarAction}
     */
    var openReferencedPageAction = {
        icon: ACTION_ICON,
        text: "Open Referenced Page",
        handler: function (editable) {
            var link = getReferenceLink(editable)
            if (link) {
                window.open(link, '_blank')
            }
        },
        condition: function(editable) {
            if (editable.type != 'bbby/components/content/reference') return false;
            if (!getReferenceLink(editable)) return false;
            
            return true
        }
    };
    
    function getReferenceLink(editable) {
        if (editable) {
            var $source = editable.dom.find('.reference-cmp-source')
            return $source.attr('data-link')
        }
        return null
    }

    // When the Edit Layer gets activated
    var EditorFrame = ns.EditorFrame;
    channel.on("cq-layer-activated", function (event) {
        if (event.layer === "Edit") {
            // Register an additional action
            EditorFrame.editableToolbar.registerAction(ACTION_NAME, openReferencedPageAction);
        }
    });

}(jQuery, Granite.author, jQuery(document), this));
