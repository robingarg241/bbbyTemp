package <%= javaPackage %>;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * <%= prettyName %> Component
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/<%= folderName %>")
@Exporter(name = "jackson", extensions = "json")
public class <%= javaName %> extends ComponentSlingModel {

    @Inject @ValueMapValue
    private String dummyValue;

    public String getDummyValue() {
        return dummyValue;
    }

}
