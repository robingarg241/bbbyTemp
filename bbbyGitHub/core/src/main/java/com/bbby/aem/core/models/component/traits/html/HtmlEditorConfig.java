package com.bbby.aem.core.models.component.traits.html;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration provided to the Ace Editor.
 *
 * @author joelepps 4/20/17
 */
public class HtmlEditorConfig {

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public enum Mode {
        HTML("html"), CSS("css"), JS("javascript");

        private String name;

        Mode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class Builder {

        private String componentId;
        private List<HtmlEditorTrait.HtmlVariable> injectableVariables;
        private List<SplitPanelConfig> panelConfigs;

        public Builder(String componentId,
            List<HtmlEditorTrait.HtmlVariable> injectableVariables,
            Mode mode, String property, String defaultText) {
            this.componentId = componentId;
            this.injectableVariables = injectableVariables;
            this.panelConfigs = new ArrayList<>();
            addSplitPanel(mode, property, defaultText);
        }

        public Builder addSplitPanel(Mode mode, String property, String defaultText) {
            this.panelConfigs.add(new SplitPanelConfig(mode.getName(), property, defaultText));
            return this;
        }

        public HtmlEditorConfig create() {
            return new HtmlEditorConfig(componentId, injectableVariables, panelConfigs);
        }
    }

    @Expose
    private String componentId;
    @Expose
    private List<HtmlEditorTrait.HtmlVariable> injectableVariables;
    @Expose
    private List<SplitPanelConfig> panelConfigs;

    private HtmlEditorConfig(String componentId,
        List<HtmlEditorTrait.HtmlVariable> injectableVariables,
        List<SplitPanelConfig> panelConfigs) {

        this.componentId = componentId;
        this.injectableVariables = injectableVariables;
        this.panelConfigs = panelConfigs;
    }

    public String getComponentId() {
        return componentId;
    }

    public List<HtmlEditorTrait.HtmlVariable> getInjectableVariables() {
        return injectableVariables;
    }

    public List<SplitPanelConfig> getPanelConfigs() {
        return panelConfigs;
    }

    public String getJson() {
        return GSON.toJson(this);
    }

    static class SplitPanelConfig {

        @Expose
        private String mode;
        @Expose
        private String property;
        @Expose
        private String defaultText;

        private SplitPanelConfig(String mode, String property, String defaultText) {
            this.mode = mode;
            this.property = property;
            this.defaultText = defaultText;
        }

        public String getMode() {
            return mode;
        }

        public String getProperty() {
            return property;
        }

        public String getDefaultText() {
            return defaultText;
        }
    }

}
