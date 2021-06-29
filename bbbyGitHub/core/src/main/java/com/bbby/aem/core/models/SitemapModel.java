package com.bbby.aem.core.models;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Builds the sitemap model based on the current page.
 * <p>
 * Accepts the following optional parameters:
 * <ul>
 * <li>{@code inheritPriority}, defaults to true</li>
 * <li>{@code inheritChangefreq}, defaults to false</li>
 * </ul>
 *
 * The root (current page) is a special case and child pages will never inherit values from it.
 *
 * @author joelepps 11/8/17
 */
public class SitemapModel extends WCMUsePojo {

    private static final Logger log = LoggerFactory.getLogger(SitemapModel.class);

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");

    private static final String ROBOTS = "robotsOptions";
    private static final String ROBOTS_NOINDEX = "noindex";
    private static final String REDIRECT_TARGET = "redirectTarget";
    private static final String CHANGEFREQ = "changefreq";
    private static final String PRIORITY = "priority";

    private Externalizer externalizer;
    private boolean inheritPriority;
    private boolean inheritChangefreq;
    private List<SitemapEntry> entries;

    @Override
    public void activate() throws Exception {
        this.externalizer = getSlingScriptHelper().getService(Externalizer.class);
        Boolean tmpInheritPriority = get("inheritPriority", Boolean.class);
        this.inheritPriority = tmpInheritPriority == null ? true : tmpInheritPriority; // handle default
        Boolean tmpInheritChangefreq = get("inheritChangefreq", Boolean.class);
        this.inheritChangefreq = tmpInheritChangefreq == null ? false : tmpInheritChangefreq; // handle default

        // without this response type will be text/html
        getResponse().setContentType("application/xml");
    }

    public List<SitemapEntry> getEntries() {
        if (this.entries != null) return this.entries;

        Page page = getCurrentPage();

        if (page == null) {
            log.error("No page found", new Exception("No page found"));
            return Collections.emptyList();
        }

        log.debug("Building sitemap for {} with inheritPriority[{}] and inheritChangefreq[{}]",
            page.getPath(), inheritPriority, inheritChangefreq);

        List<SitemapEntry> entries = new ArrayList<>();
        entries.add(buildEntry(page, null, null));

        // don't pass inherited values because nothing should inherit from the root (home page)
        populateChildrenRecursive(page, null, null, entries);

        this.entries = entries;
        return entries;
    }

    private void populateChildrenRecursive(Page page, String inheritedPriority, String inheritedChangefreq,
        List<SitemapEntry> entries) {

        SitemapPageFilter filter = new SitemapPageFilter();

        for (Iterator<Page> children = page.listChildren(null, false); children.hasNext();) {

            Page child = children.next();

            if (filter.includes(child)) {
                SitemapEntry entry = buildEntry(child, inheritedPriority, inheritedChangefreq);
                entries.add(entry);

                populateChildrenRecursive(child, entry.getPriority(), entry.getChangefreq(), entries);
            } else if (filter.passesRobotsNoIndex(child.getProperties())) {
                // Skip entry but still recurse
                populateChildrenRecursive(child, inheritedPriority, inheritedChangefreq, entries);
            }

        }
    }

    private SitemapEntry buildEntry(Page page, String inheritedPriority, String inheritedChangefreq) {
        ValueMap valueMap = page.getProperties();

        String loc = externalizer.publishLink(getResourceResolver(), String.format("%s.html", page.getPath()));
        Calendar modified = valueMap.get("cq:lastModified", Calendar.class);
        String changefreq = valueMap.get(CHANGEFREQ, String.class);
        String priority = valueMap.get(PRIORITY, String.class);

        if (inheritChangefreq) {
            changefreq = coalesce(changefreq, inheritedChangefreq);
        }
        if (inheritPriority) {
            priority = coalesce(priority, inheritedPriority);
        }

        return new SitemapEntry(
            loc,
            modified == null ? null : DATE_FORMAT.format(modified),
            changefreq,
            priority);
    }

    public static class SitemapEntry {

        private final String loc;
        private final String lastmod;
        private final String changefreq;
        private final String priority;

        public SitemapEntry(String loc, String lastmod, String changefreq, String priority) {
            this.loc = loc;
            this.lastmod = lastmod;
            this.changefreq = changefreq;
            this.priority = priority;
        }

        public String getLoc() {
            return loc;
        }

        public String getLastmod() {
            return lastmod;
        }

        public String getChangefreq() {
            return changefreq;
        }

        public String getPriority() {
            return priority;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SitemapEntry that = (SitemapEntry) o;

            if (loc != null ? !loc.equals(that.loc) : that.loc != null) return false;
            if (lastmod != null ? !lastmod.equals(that.lastmod) : that.lastmod != null) return false;
            if (changefreq != null ? !changefreq.equals(that.changefreq) : that.changefreq != null) return false;
            return priority != null ? priority.equals(that.priority) : that.priority == null;
        }

        @Override
        public int hashCode() {
            int result = loc != null ? loc.hashCode() : 0;
            result = 31 * result + (lastmod != null ? lastmod.hashCode() : 0);
            result = 31 * result + (changefreq != null ? changefreq.hashCode() : 0);
            result = 31 * result + (priority != null ? priority.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "SitemapEntry{" + "loc='" + loc + '\'' + ", lastmod='" + lastmod + '\'' + ", changefreq='"
                + changefreq + '\'' + ", priority='" + priority + '\'' + '}';
        }
    }

    /**
     * @param strings strings to evaluate
     * @return first non-null non-empty string
     */
    private static String coalesce(String... strings) {
        if (strings == null || strings.length == 0) return null;
        for (String s : strings) {
            if (!StringUtils.isBlank(s)) return s;
        }
        return null;
    }

    /**
     * Adds filtering to base {@link PageFilter} by also checking for additional values.
     *
     * @author joelepps
     *
     */
    private static class SitemapPageFilter extends PageFilter {

        private final boolean includeSitemapHidden;
        private final boolean includeRedirectedPages;

        /**
         * <ul>
         * <li>Don't include invalid pages
         * <li>Include hidden pages (hideInNav)
         * <li>Don't include sitemap excluded pages that are marked noindex.
         * <li>Don't include pages which redirect
         * </ul>
         */
        public SitemapPageFilter() {
            this(false, true, false, false);
        }

        public SitemapPageFilter(boolean includeInvalid, boolean includeHidden, boolean includeSitemapHidden,
            boolean includeRedirectedPages) {
            super(includeInvalid, includeHidden);
            this.includeSitemapHidden = includeSitemapHidden;
            this.includeRedirectedPages = includeRedirectedPages;
        }

        @Override
        public boolean includes(Page page) {
            // handles invalid and hideInNave checks
            boolean baseIncludes = super.includes(page);
            if (!baseIncludes) return false;

            ValueMap valueMap = page.getProperties();

            boolean passesRobotsNoIndex = passesRobotsNoIndex(valueMap);
            boolean passesHideRedirectPages = passesHideRedirectPages(valueMap);

            return passesRobotsNoIndex && passesHideRedirectPages;
        }

        private boolean passesRobotsNoIndex(ValueMap valueMap) {
            String robotRule = valueMap.get(ROBOTS, String.class);
            boolean robotNoIndex = robotRule != null && robotRule.contains(ROBOTS_NOINDEX);
            return (includeSitemapHidden || !robotNoIndex);
        }

        private boolean passesHideRedirectPages(ValueMap valueMap) {
            boolean hasRedirect = !StringUtils.isBlank(valueMap.get(REDIRECT_TARGET, String.class));
            return (includeRedirectedPages || !hasRedirect);
        }

    }

}
