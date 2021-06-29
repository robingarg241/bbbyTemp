/*
 * Custom Author mode editor that uses the Ace editor
 * (https://ace.c9.io/#nav=about).
 *
 * To use this editor [component]/cq:editConfig/cq:inplaceEditing/@editorType
 * must be set to 'ace'.
 *
 * If using component dialog annotations this is done with
 * @Component.inPlaceEditingEditorType = 'ace' class annotation.
 */
(function ($, ns, channel, window, undefined) {
    "use strict";

    var Split = ace.require('ace/split').Split
    var UndoManager = ace.require("./undomanager").UndoManager

    ace.require('ace/config').setDefaultValue("session", "useWorker", false);

    /**
     * Constructor for AceEditor
     */
    var AceEditor = function() {
        // configs and editors are parallel arrays
        this.state = {}
    }

    AceEditor.prototype.setUp = function (editable) {
        var self = this

        // reset variables
        self.state = {}

        // Load configs from DOM
        var $config = editable.dom.find('.ace-editor-config')
        if ($config.length) {
            var rawConfig = $config.attr('data-config')
            if (rawConfig) {
                self.state = JSON.parse(rawConfig)
            } else {
                alert("Could not load editor configs. Please contact your system administrator for support.")
                return
            }
        } else {
            alert("Could not find editor configs. Please contact your system administrator for support.")
            return
        }

        // hide the authoring overlay
        channel.trigger("cq-hide-overlays")

        // close the side panel if it is opened
        if (ns.ui.SidePanel.isOpened()) {
            ns.ui.SidePanel.close()
        }

        // Add the stub markup to the main Content div
        self.$modal = $('#Content').append(
            '<div class="ace-modal">'+
            '<div class="ace-modal-content">'+
            '<div class="ace-modal-nav">'+
                '<a href="#" class="ace-btn ace-modal-show-vars">Variables</a>'+
                '<a href="#" class="ace-btn ace-modal-close">Close</a>'+
                '<a href="#" class="ace-btn ace-modal-save">Save</a>'+
                '<span class="ace-modal-msg">MESSAGE</span>'+
            '</div>'+
            '<div class="ace-editor-wrap">'+
                '<div class="ace-editor"></div>'+
            '</div>'+
            '</div>'+
            '</div>').find('.ace-modal')

        // Wire up save btn
        self.$modal.find('.ace-modal-save').on('click', function() {
            self.save(editable)
            return false
        })
        // Wire up close btn
        self.$modal.find('.ace-modal-close').on('click', function() {
            var panelWithUnsavedChanges = self.hasUnsavedChanges()
            if (panelWithUnsavedChanges) {
                if (confirm('Unsaved changes on panel ' + panelWithUnsavedChanges + ' will be lost if you continue.')) {
                    self.tearDown(editable)
                }
                return false
            } else {
                self.tearDown(editable)
                return false
            }
        })
        // Wire up variables btn
        self.$modal.find('.ace-modal-show-vars').on('click', function() {
            var curSplits = self.split.getSplits()
            var editorSplits = self.state.editors.length

            if (curSplits > editorSplits) {
                // close variable panel
                self.split.setSplits(curSplits - 1)
            } else {
                // open variable panel
                self.split.setSplits(curSplits + 1)
                var editor = self.split.getEditor(curSplits)

                var txt = '/*\n    Variables available for injection.\n*/\n\n'
                for (var i = 0; i < self.state.injectableVariables.length; i++) {
                    var variable = self.state.injectableVariables[i]
                    txt += '${' + variable.type + '.' + variable.name + '}\n'
                    txt += '/* ' + variable.value + ' */\n'
                }

                editor.session.setMode('ace/mode/groovy')
                editor.setValue(txt)
                editor.clearSelection()
                editor.gotoLine(0, 0)
                editor.setReadOnly(true)
            }

            return false
        })

        // Wire up window/editor resize
        self.$modal.resize(function(e) {
            self.split.resize()
        })

        // Initialize Ace editors
        self.state.editors = [] // reset editors array
        var container = self.$modal.find('.ace-editor')[0]
        var theme = null
        self.split = new Split(container, theme, self.state.panelConfigs.length)
        for (var i = 0; i < self.state.panelConfigs.length; i++) {
            var editor = self.split.getEditor(i)
            editor.session.setMode("ace/mode/" + self.state.panelConfigs[i].mode)
            editor.session.setUndoManager(new UndoManager())
            editor.commands.addCommand({
                name: 'save',
                bindKey: {win: 'Ctrl-S', mac: 'Command-S'},
                exec: function(editor) {
                    self.save(editable)
                }
            })
            self.state.editors.push(editor)
        }

        // Load data into the editor
        ns.persistence.readParagraphContent(editable, true)
            .done(function (data) {
                var content = $.parseJSON(data)
                for (var i = 0; i < self.state.editors.length; i++) {
                    var editor = self.state.editors[i]
                    var config = self.state.panelConfigs[i]
                    var initialContent = content[config.property] || config.defaultText

                    editor.setValue(initialContent)
                    editor.clearSelection()
                    editor.gotoLine(0, 0)
                }
                self.resetUnsavedChangesTracker()
            }).fail(function (data) {
                console.log('Error reading paragraph content!', editable)
                // ns.persistence.updateParagraph(editable, {"./sling:resourceType": editable.type}).done(function () {
                //     ns.persistence.readParagraphContent(editable, true).done(function (data) {
                //
                //     })
                // })
            })
    }

    /**
     * @return the panel (number) that has unsaved changes or false
     */
    AceEditor.prototype.hasUnsavedChanges = function() {
        var self = this

        for (var i = 0; i < self.state.editors.length; i++) {
            var editor = self.state.editors[i]
            var config = self.state.panelConfigs[i]
            if (editor.getValue() != config.originalContent) {
                return i + 1
            }
        }
        return false
    }

    /**
     * Reset the unsaved changes tracker with current content.
     */
    AceEditor.prototype.resetUnsavedChangesTracker = function() {
        var self = this
        for (var i = 0; i < self.state.editors.length; i++) {
            var editor = self.state.editors[i]
            var config = self.state.panelConfigs[i]

            config.originalContent = editor.getValue()
        }
    }

    AceEditor.prototype.save = function (editable) {
        var self = this
        var props = {}

        for (var i = 0; i < self.state.editors.length; i++) {
            var editor = self.state.editors[i]
            var config = self.state.panelConfigs[i]
            props['./'+config.property] = editor.getValue()
        }

        self.toast('Saving...')

        var doneFunc = function() {
            self.resetUnsavedChangesTracker()
            self.toast('Updates saved.', 2000)
        }
        var failFunc = function(e) {
            self.toast('An error occured while saving!')
            console.log('An error occured during save: ', e)
        }

        ns.edit.EditableActions.UPDATE.execute(editable, props)
            .then(doneFunc)
            .fail(failFunc)
    }

    AceEditor.prototype.tearDown = function (editable) {
        var self = this

        for (var i = 0; i < self.state.editors.length; i++) {
            var editor = self.state.editors[i]
            editor.destroy()
        }
        self.state = {}
        self.split = null

        self.$modal.remove()
        self.$modal = null
        channel.trigger("cq-show-overlays")
    }

    /**
     * @param msg: message to show
     * @param hideAfter: (optional) hide after milliseconds
     */
    AceEditor.prototype.toast = function (msg, hideAfter) {
        var self = this
        var $msg = self.$modal.find('.ace-modal-msg')
        $msg.text(msg)
        $msg.addClass('active')

        if (hideAfter) {
            setTimeout(function() {
                $msg.removeClass('active')
            }, hideAfter)
        }
    }

    // Expose for other components to use
    ns.AceEditor = AceEditor

    // Register editor for EditorConfig.xml support
    ns.editor.register('ace', new AceEditor())

}(jQuery, Granite.author, jQuery(document), this));
