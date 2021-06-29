/*
 * Adds the HTML Editor action to supported components.
 *
 * Components that use the editor must have their Java model implement
 * HtmlEditorTrait and must have their Sightly/HTL include the
 * bbby/components/content/html htmlEditorConfig template.
 *
 * It is recommended you add id="${model.componentId}" to the wrapper <div> on
 * the component to allow authors to easily target the specific instance of the
 * the component.
 *
 * Toolbar Action Example:
 * https://github.com/Adobe-Marketing-Cloud/aem-authoring-extension-toolbar-screenshot
 */
(function ($, ns, channel, window, undefined) {
    // Add supported components here
    var SUPPORTED_COMPONENTS = [
        // 'bbby/components/content/hero-banner' // EXAMPLE
    ]
    var ACTION_ICON = "coral-Icon--code";
    var ACTION_TITLE = "HTML Editor";

    var htmlEditorAction = {
        icon: ACTION_ICON,
        text: ACTION_TITLE,
        handler: function (editable) {
            var aceEditor = new ns.AceEditor()
            aceEditor.setUp(editable)
        },
        condition: function(editable) {
            for (var i = 0; i < SUPPORTED_COMPONENTS.length; i++) {
                if (SUPPORTED_COMPONENTS[i] == editable.type) {
                    return true
                }
            }
            return false
        }
    };

    // When the Edit Layer gets activated
    var EditorFrame = ns.EditorFrame;
    channel.on("cq-layer-activated", function (event) {
        if (event.layer === "Edit") {
            // Register an additional action
            EditorFrame.editableToolbar.registerAction(ACTION_TITLE, htmlEditorAction);
        }
    });

}(jQuery, Granite.author, jQuery(document), this));
