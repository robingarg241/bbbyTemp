package com.bbby.aem.core.models.component;

import com.bbby.aem.core.models.component.traits.html.HtmlEditorConfig;
import com.bbby.aem.core.models.component.traits.html.HtmlEditorTrait;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;

/**
 * Html Component.
 * <p>
 * Use of this component is generally not recommended as it crosses the line between code and content.
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/html")
@Exporter(name = "jackson", extensions = "json")
public class Html extends ComponentSlingModel implements HtmlEditorTrait {

    @Override
    public HtmlEditorConfig getHtmlEditorConfig() {
        return new HtmlEditorConfig.Builder(
            getComponentId(),
            getAllInjectableHtmlVariables(),
            HtmlEditorConfig.Mode.HTML,
            "text",
            "<!-- HTML -->")
                .addSplitPanel(HtmlEditorConfig.Mode.CSS, "css", "/* CSS */")
                .addSplitPanel(HtmlEditorConfig.Mode.JS, "js", "// JavaScript")
                .create();
    }

}
