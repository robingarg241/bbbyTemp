package com.bbby.aem.core.models.component.traits.analytics;

import com.adobe.cq.sightly.WCMUsePojo;
import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.annotations.Property;
import com.citytechinc.cq.component.annotations.widgets.TextField;
import com.bbby.aem.core.models.component.ComponentSlingModel;
import com.bbby.aem.core.models.component.traits.ComponentTrait;
import com.bbby.aem.core.util.SlingModelUtils;
import com.bbby.aem.dialog.classic.multifield.MultiCompositeField;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mixin that provides components with authorable and static analytics attributes to be placed in HTML.
 * <p>
 * Components that implement this mixin should add the following to the outer div of the Sightly markup.
 *
 * <pre>
 * data-sly-attribute="${logic.analyticsAttributeMap}"
 * </pre>
 *
 * @author joelepps 4/19/16
 *
 */
public interface AnalyticsTrait extends ComponentTrait {

    /** Reserved analytics key that is dynamically added to attribute map. Author is not allowed to use this. */
    String DATA_COMPONENT_NAME = "data-component-name";

    interface DialogFields {

        @DialogField(fieldLabel = "Analytics Attributes", name = "./analytics", ranking = 100)
        @MultiCompositeField
        List<AnalyticsAttribute> analyticsAttributes();

    }

    default List<AnalyticsAttribute> getAnalyticsAttributes()
        throws ReflectiveOperationException, IntrospectionException {
        Resource analyticsResource = getResource().getChild("analytics");
        return SlingModelUtils.buildComponentSlingModels(analyticsResource, getRequest(), AnalyticsAttribute.class,
            false);
    }

    /*
     * Try to limit calls to this, as value is not cached. Though as long as size remains small, shouldn't matter.
     */
    default Map<String, String> getAnalyticsAttributeMap() throws ReflectiveOperationException, IntrospectionException {
        return getAnalyticsAttributeMap(getAnalyticsAttributes(), getComponentName(this));
    }

    /**
     * Build the analytics attribute map, consumable by Sightly, from a list of {@link AnalyticsAttribute} objects.
     *
     * @param list List to convert to map
     * @param componentName {@link #DATA_COMPONENT_NAME} entry will be added to the resulting map
     * @return map of analytics attributes
     */
    static Map<String, String> getAnalyticsAttributeMap(List<AnalyticsAttribute> list, String componentName) {
        // Converts list to map, filter out author entered DATA_COMPONENT_NAME, and finally add dynamic
        // DATA_COMPONENT_NAME
        return list
            .stream()
            .filter(aa -> (aa.name != null && !aa.name.equals(DATA_COMPONENT_NAME)))
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toMap(
                        AnalyticsAttribute::getName,
                        AnalyticsAttribute::getValue,
                        (dupVal1, dupVal2) -> dupVal1),
                    map -> {
                        if (componentName != null) map.put(DATA_COMPONENT_NAME, componentName);
                        return map;
                    }));
    }

    /**
     * Build a list of {@link AnalyticsAttribute} objects from a String encoded with delimiters.
     * <p>
     * Resulting list can be passed to {@link #getAnalyticsAttributeMap(List, String)} to build Sightly consumable
     * attribute map.
     *
     * @param parsable String to parse
     * @param primaryDelim String that splits AnalyticsAttribute entries
     * @param secondaryDelim String that splits AnalyticsAttribute key value pair
     * @return list of attribute objects
     */
    static List<AnalyticsAttribute> getAnalyticsAttributeList(String parsable,
        String primaryDelim,
        String secondaryDelim) {
        List<AnalyticsAttribute> list = new ArrayList<>();

        if (StringUtils.isBlank(parsable)) return list;

        String[] attributes = parsable.split(primaryDelim);
        for (String attribute : attributes) {
            if (StringUtils.isBlank(attribute)) {
                continue;
            }

            String[] keyValPair = attribute.split(secondaryDelim);
            if (keyValPair.length == 2) {
                String key = keyValPair[0].trim();
                String val = keyValPair[1].trim();
                if (key.length() == 0 || val.length() == 0) {
                    continue;
                }
                list.add(new AnalyticsAttribute(key, val));
            }
        }

        return list;
    }

    /**
     * Get the component name from the {@link WCMUsePojo} or null.
     *
     * @param trait trait
     * @return component name
     */
    static String getComponentName(ComponentTrait trait) {
        return trait.getClass().getSimpleName().toLowerCase();
    }

    /**
     * Model for analytics attribute.
     *
     * @author joelepps
     *
     */
    @Model(adaptables = { Resource.class, SlingHttpServletRequest.class },
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    class AnalyticsAttribute extends ComponentSlingModel {

        @DialogField(fieldLabel = "Name", name = "./name", ranking = 100,
            fieldDescription = "Name must start with 'data-' and cannot be '" + DATA_COMPONENT_NAME + "'",
            additionalProperties = {
                @Property(name = "regex", value = "^data-.+$"),
                @Property(name = "foundation-validation", value = "regex") })
        @TextField
        @Inject
        @ValueMapValue
        private String name;

        @DialogField(fieldLabel = "Value", name = "./value", ranking = 101)
        @TextField
        @Inject
        @ValueMapValue
        private String value;

        // Needed for SlingModel construction
        public AnalyticsAttribute() {
        }

        public AnalyticsAttribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

}
