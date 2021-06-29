package com.bbby.aem.core.models.component;

import com.google.common.collect.ImmutableMap;
import com.bbby.aem.core.util.ServiceUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Column Control Component
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ColumnControl extends ComponentSlingModel {

    private static final String ALL_50 = "50";
    private static final String ALL_33 = "33";
    private static final String ALL_25 = "25";
    private static final String SPLIT_25_75 = "25,75";
    private static final String SPLIT_33_66 = "33,66";
    private static final String SPLIT_42_58 = "42,58";
    private static final String SPLIT_58_42 = "58,42";

    private static final Map<String, String[]> CLASS_MAP = new ImmutableMap.Builder<String, String[]>()
        .put(ALL_50, new String[]{
            "col-12 col-md-6",
            "col-12 col-md-6"})
        .put(ALL_33, new String[]{
            "col-12 col-md-4",
            "col-12 col-md-4",
            "col-12 col-md-4"})
        .put(ALL_25, new String[]{
            "col-12 col-md-3",
            "col-12 col-md-3",
            "col-12 col-md-3",
            "col-12 col-md-3"})
        .put(SPLIT_25_75, new String[]{
            "col-12 col-md-3",
            "col-12 col-md-9"})
        .put(SPLIT_33_66, new String[]{
            "col-12 col-md-4",
            "col-12 col-md-8"})
        .put(SPLIT_42_58, new String[]{
            "col-12 col-md-5",
            "col-12 col-md-7"})
        .put(SPLIT_58_42, new String[]{
            "col-12 col-md-7",
            "col-12 col-md-5"})
        .build();

    private List<Column> columns;

    @Inject
    @ValueMapValue
    @Default(values = ALL_50)
    private String columnLayout;

    @Inject
    @ValueMapValue
    @Default(values = "valign-top")
    private String verticalAlign;

    @Inject
    @ValueMapValue
    private String preventMobileStacking;

    /*
        METHODS
     */

    @Override
    public void postConstruct() throws Exception {
        String[] classes = CLASS_MAP.get(getColumnLayout());
        Resource columnsResource = getResource().getChild("columns");

        columns = new ArrayList<>();
        if (classes != null && columnsResource != null) {
            Iterator<Resource> colIterator = columnsResource.getChildren().iterator();
            int i = 0;
            while (colIterator.hasNext()) {
                Resource colResource = colIterator.next();
                // Once at the end of the array, loop back to the start (mod)
                String classStr = classes[i % classes.length];

                columns.add(new Column(colResource, classStr, isPreventMobileStacking()));

                i++;
            }
        }
    }

    public String getVerticalAlign() {
        return verticalAlign;
    }

    public String getColumnLayout() {
        return columnLayout;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public boolean isPreventMobileStacking() {
        return "true".equals(preventMobileStacking);
    }

    public static class Column {

        private static final Pattern SM_SIZE = Pattern.compile("col-sm-(\\d+)");
        private static final Pattern MD_SIZE = Pattern.compile("col-md-(\\d+)");
        private static final Pattern LG_SIZE = Pattern.compile("col-lg-(\\d+)");
        private static final Pattern XL_SIZE = Pattern.compile("col-xl-(\\d+)");

        private final String name;
        private final String resourceType;
        private final String classes;

        public Column(Resource resource, String classes, boolean preventMobileStacking) {
            this(resource.getName(), resource.getResourceType(), classes, preventMobileStacking);
        }

        public Column(String name, String resourceType, String classes, boolean preventMobileStacking) {
            this.name = name;
            this.resourceType = resourceType;

            if (preventMobileStacking) {
                String smSize = extractColumnSize(SM_SIZE, classes);
                String mdSize = extractColumnSize(MD_SIZE, classes);
                String lgSize = extractColumnSize(LG_SIZE, classes);
                String xlSize = extractColumnSize(XL_SIZE, classes);
                String newMobileClass = "col-" + ServiceUtils.coalesce(smSize, mdSize, lgSize, xlSize);

                classes = StringUtils.replace(classes, "col-12", newMobileClass);
            }

            this.classes = classes;
        }

        public String getName() {
            return name;
        }

        public String getResourceType() {
            return resourceType;
        }

        public String getClasses() {
            return classes;
        }

        /**
         * @return If provided {@code col-md-6} returns {@code 6}
         */
        private static String extractColumnSize(Pattern p, String classes) {
            if (classes == null) return null;
            Matcher m = p.matcher(classes);
            if (m.find()) {
                return m.group(1);
            }
            return null;
        }
    }

}
