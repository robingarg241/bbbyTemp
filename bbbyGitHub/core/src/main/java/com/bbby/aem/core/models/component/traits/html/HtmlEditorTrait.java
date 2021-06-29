package com.bbby.aem.core.models.component.traits.html;

import com.google.gson.annotations.Expose;
import com.bbby.aem.core.models.component.ComponentSlingModel;
import com.bbby.aem.core.models.component.traits.ComponentTrait;
import com.bbby.aem.core.util.ServiceUtils;
import com.bbby.aem.core.util.SlingModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides all the necessary fields and logic for component that use the custom (Ace) HTML editor.
 * <p>
 * Components that wish to provide an HTML editor should refer to the documentation in
 * ui.apps/src/main/content/jcr_root/apps/bbby/components/content/_html-editor-toolbar-action/toolbar-action.js
 *
 * @author joelepps 4/19/17
 */
public interface HtmlEditorTrait extends ComponentTrait {

    Pattern VAR_PATTERN = Pattern.compile("\\$\\{[^}]+}");

    /**
     * Implementations must provide the config used for the Editor.
     *
     * @return HtmlEditorConfig instance
     */
    HtmlEditorConfig getHtmlEditorConfig();

    /**
     * Provides non-authored variables to HTML editor.
     */
    default List<ComponentVariable> getComponentVars() {
        List<ComponentVariable> vars = new ArrayList<>();

        vars.add(new ComponentVariable("id", getComponentId()));

        return vars;
    }

    default List<PathVariable> getPathVars() {
        return SlingModelUtils.buildComponentSlingModels(
            getResource().getChild("paths"),
            PathVariable.class);
    }

    default List<AssetVariable> getAssetVars() {
        return SlingModelUtils.buildComponentSlingModels(
            getResource().getChild("assets"),
            AssetVariable.class);
    }

    default List<TextVariable> getTextVars() {
        return SlingModelUtils.buildComponentSlingModels(
            getResource().getChild("texts"),
            TextVariable.class);
    }

    default Map<String, String> getHtmlContent() {
        Map<String, String> contentMap = new HashMap<>();

        for (HtmlEditorConfig.SplitPanelConfig panelConfig : getHtmlEditorConfig().getPanelConfigs()) {
            String content = getProperties().get(panelConfig.getProperty(), String.class);

            // inject variables
            content = injectAllHtmlVariables(content);

            contentMap.put(panelConfig.getProperty(), content);
        }

        return contentMap;
    }

    default String getHtmlEditorConfigJson() {
        return getHtmlEditorConfig().getJson();
    }

    default String injectAllHtmlVariables(String value) {
        return injectHtmlVariables(value, getAllInjectableHtmlVariables());
    }

    default String injectHtmlVariables(String source, List<? extends HtmlVariable> vars) {
        if (vars == null || source == null) return source;
        for (HtmlVariable var : vars) {
            source = StringUtils.replace(source, var.getVarName(), var.getValue());
        }
        return source;
    }

    default Set<String> getUnresolvedHtmlVariables() {
        Set<String> unresolvedVars = new HashSet<>();

        for (Map.Entry<String, String> entry : getHtmlContent().entrySet()) {
            // find all remaining variables
            Matcher m = VAR_PATTERN.matcher(entry.getValue());
            while (m.find()) {
                unresolvedVars.add(m.group(0));
            }
        }

        return unresolvedVars;
    }

    default List<HtmlVariable> getAllInjectableHtmlVariables() {
        List<HtmlVariable> vars = new ArrayList<>();
        vars.addAll(getPathVars());
        vars.addAll(getAssetVars());
        vars.addAll(getTextVars());
        vars.addAll(getComponentVars());
        return vars;
    }

    abstract class HtmlVariable extends ComponentSlingModel {

        @Inject
        @ValueMapValue
        @Expose
        protected String name;

        public String getVarName() {
            return "${" + getVarType() + "." + name + "}";
        }

        abstract String getValue();

        abstract String getVarType();
    }

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    class PathVariable extends HtmlVariable {

        @Inject
        @Self
        @Required
        private Resource self;

        @Inject
        @Expose
        private String value;

        @Expose
        private String type = "path";

        @PostConstruct
        public void postConstruct() {
            value = ServiceUtils.appendLinkExtension(self.getResourceResolver(), value);
        }

        @Override
        String getValue() {
            return value;
        }

        @Override
        String getVarType() {
            return type;
        }
    }

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    class AssetVariable extends HtmlVariable {

        @Inject
        @Expose
        private String value;

        @Expose
        private String type = "asset";

        @Override
        String getValue() {
            return value;
        }

        @Override
        String getVarType() {
            return type;
        }
    }

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    class TextVariable extends HtmlVariable {

        @Inject
        @Expose
        private String value;

        @Expose
        private String type = "text";

        @Override
        String getValue() {
            return value;
        }

        @Override
        String getVarType() {
            return type;
        }
    }

    class ComponentVariable extends HtmlVariable {

        @Expose
        private String type;
        @Expose
        private String value;

        public ComponentVariable(String name, String value) {
            this.name = name;
            this.value = value;
            this.type = "component";
        }

        @Override
        String getValue() {
            return value;
        }

        @Override
        String getVarType() {
            return type;
        }
    }

}
