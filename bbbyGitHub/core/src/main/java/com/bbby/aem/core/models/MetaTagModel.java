package com.bbby.aem.core.models;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.Page;
import com.bbby.aem.core.models.common.PageMetadata;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * Provides Sightly access to the collection of {@link Tag} ({@code <meta>} and {@code <link>}) instances for this page.
 *
 * @author joelepps
 *
 */
public class MetaTagModel extends WCMUsePojo {

    private static final Logger log = LoggerFactory.getLogger(MetaTagModel.class);

    private PageMetadata pageMetadata;
    private Externalizer externalizer;

    @Override
    public void activate() throws Exception {
        pageMetadata = getResource().adaptTo(PageMetadata.class);
        externalizer = getSlingScriptHelper().getService(Externalizer.class);
    }

    public String getTitle() {
        return pageMetadata.getSeoTitle();
    }

    public String getLocale() {
        if (getWcmMode().isEdit()) return "en-US";
        return pageMetadata.getLocaleCode();
    }

    public List<Tag> getTags() {
        final List<Tag> tags = new ArrayList<>();

        Consumer<Tag> addNonNull = tag -> {
            if (tag != null) tags.add(tag);
        };

        // META TAGS

        addNonNull.accept(MetaTag.newInstance("description", pageMetadata.getDescription()));
        addNonNull.accept(MetaTag.newInstance("robots", pageMetadata.getRobotOptions()));
        addNonNull.accept(MetaTag.newInstance("twitter:card", "summary"));
        addNonNull.accept(MetaTag.newInstance("twitter:site", pageMetadata.getTwitterSiteHandle()));
        addNonNull.accept(MetaTag.newFacebookTag("og:title", pageMetadata.getSeoTitle()));
        addNonNull.accept(MetaTag.newFacebookTag("og:description", pageMetadata.getDescription()));
        addNonNull.accept(MetaTag.newFacebookTag("og:image", pageMetadata.getImage()));
        addNonNull.accept(MetaTag.newFacebookTag("og:type", "website"));
        addNonNull.accept(MetaTag.newFacebookTag("og:url", pageMetadata.getCanonicalUrl()));

        // LINK TAGS

        addNonNull.accept(LinkTag.newCanonical(pageMetadata.getCanonicalUrl()));

        for (Page page : pageMetadata.getAltPages()) {
            String hreflang = hrefLangForPage(page);
            String href = externalizer.publishLink(getResourceResolver(), page.getPath()) + ".html";

            addNonNull.accept(LinkTag.newAlternate(href, hreflang));
        }

        return tags;
    }

    private static String hrefLangForPage(Page page) {
        Locale locale = page.getLanguage(false);
        String[] localeParts = StringUtils.split(locale.toString().toLowerCase(Locale.US), "_");

        // en -> en
        if (localeParts.length == 1) return localeParts[0];

        // fr_fr -> fr
        if (localeParts.length == 2 && localeParts[0].equals(localeParts[1])) return localeParts[0];

        // en_us -> en-us
        if (localeParts.length == 2) return localeParts[0] + "-" + localeParts[1];

        log.debug("Unsupported locale {}", locale);
        return null;
    }

    /**
     * Base tag model supporting any tag name and attribute combination.
     */
    public static class Tag {

        private final String name;
        private final Map<String, String> attributes;

        public Tag(String name) {
            if (name == null) throw new IllegalArgumentException("Name cannot be null");

            this.name = name;
            this.attributes = new HashMap<>();
        }

        public void addAttribute(String name, String value) {
            if (name == null) return;
            attributes.put(name, value);
        }

        public String getName() {
            return name;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }
    }

    /**
     * Supports: <br>
     * {@code <meta property="og:title" content="Title of Page"/>} <br>
     * {@code <meta name="twitter:site" content="@CompanyName"/>}
     */
    public static class MetaTag extends Tag {

        private MetaTag(String nameAttribute, String nameValue, String contentAttribute, String contentValue) {
            super("meta");
            addAttribute(nameAttribute, nameValue);
            addAttribute(contentAttribute, contentValue);
        }

        public static MetaTag newInstance(String nameAttribute, String nameValue, String contentAttribute,
            String contentValue) {
            if (nameAttribute == null) return null;
            if (nameValue == null) return null;
            if (contentAttribute == null) return null;
            if (contentValue == null) return null;

            return new MetaTag(nameAttribute, nameValue, contentAttribute, contentValue);
        }

        // Uses standard "name"
        public static MetaTag newInstance(String nameValue, String contentValue) {
            return newInstance("name", nameValue, "content", contentValue);
        }

        // Facebook og:xxxx uses "property"
        public static MetaTag newFacebookTag(String nameValue, String contentValue) {
            return newInstance("property", nameValue, "content", contentValue);
        }
    }

    /**
     * Supports: <br>
     * {@code <link rel="canonical" href="http://acme.com/fr-fr"/>} <br>
     * {@code <link rel="alternate" hreflang="en-us" href="http://acme.com/en-us"/>}
     */
    public static class LinkTag extends Tag {

        private LinkTag(String href, String hreflang, String rel) {
            super("link");
            addAttribute("rel", rel);
            addAttribute("href", href);
            addAttribute("hreflang", hreflang);
        }

        public static LinkTag newCanonical(String href) {
            if (href == null) return null;

            return new LinkTag(href, null, "canonical");
        }

        public static LinkTag newAlternate(String href, String hreflang) {
            if (href == null) return null;
            if (hreflang == null) return null;

            return new LinkTag(href, hreflang, "alternate");
        }

    }

}
