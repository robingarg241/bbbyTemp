package com.bbby.aem.core.models.component;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.bbby.aem.core.models.component.traits.cta.CtaTrait;

import javax.inject.Inject;

/**
 * Drag Drop Component
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/drag-drop")
@Exporter(name = "jackson", extensions = "json")
public class DragDrop extends ComponentSlingModel implements CtaTrait {

	@Inject
    @ValueMapValue
    private String title;
	
	public String getTitle() {
		return title;
	}

}
