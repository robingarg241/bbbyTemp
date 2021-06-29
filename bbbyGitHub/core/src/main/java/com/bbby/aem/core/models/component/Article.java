package com.bbby.aem.core.models.component;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Article Component
 * <p>
 * Renders large bodies of text in a multi-column format.
 */

@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/article")
@Exporter(name = "jackson", extensions = "json")
public class Article extends ComponentSlingModel {

    @Inject
    @ValueMapValue
    private String title;

    @Inject
    @ValueMapValue
    private String articleTitleSize;

    @Inject
    @ValueMapValue
    private String articleColumnCount;

    public String getTitle() { return title; }

    public String getArticleTitleSize() {
        return articleTitleSize;
    }

    public String getArticleColumnCount() {
        return articleColumnCount;
    }

}
