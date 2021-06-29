package com.bbby.aem.core.models.common;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.annotations.Expose;
import com.bbby.aem.core.util.ServiceUtils;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Simple DTO representing an AEM tag and, optionally, its children.
 * <p>
 * Designed for JSON serialization or use by Sightly.
 *
 * @author joelepps
 *
 */
public class AemTag {

    private static final Logger log = LoggerFactory.getLogger(AemTag.class);

    @Expose
    private final String id;
    @Expose
    private final String title;
    @Expose
    private final List<AemTag> children;

    /**
     * New {@code AemTag} representation of the provided {@code tag}.
     *
     * @param tag Tag to use
     * @param resolver Resource resolver instance
     * @param locale Locale to use for title
     * @param levels Number of levels to recurse for children. 0 for none, 1 for only immediate children, etc.
     * @throws IllegalArgumentException If any parameters are null or invalid
     */
    public AemTag(Tag tag, ResourceResolver resolver, Locale locale, int levels) throws IllegalArgumentException {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null");
        if (resolver == null) throw new IllegalArgumentException("Resolver cannot be null");
        if (locale == null) throw new IllegalArgumentException("Locale cannot be null");
        if (levels < 0) throw new IllegalArgumentException("Levels cannot be less than 0");

        Resource tagResource = resolver.resolve(tag.getPath());
        if (tagResource instanceof NonExistingResource) {
            throw new IllegalArgumentException("Tag resource missing for " + tag.getPath());
        }

        String tagTitle = ServiceUtils.coalesce(tag.getLocalizedTitle(locale), tag.getTitle());

        if (levels == 0) {
            this.id = tag.getTagID();
            this.title = tagTitle;
            this.children = null; // Gson, by default, won't include null fields. Call serializeNulls() to change this.
        } else {
            List<AemTag> children = StreamSupport.stream(tagResource.getChildren().spliterator(), false)
                .map(r -> r.adaptTo(Tag.class))
                .filter(Objects::nonNull)
                .map(t -> new AemTag(t, resolver, locale, levels - 1))
                .collect(Collectors.toList());

            this.id = tag.getTagID();
            this.title = tagTitle;
            this.children = children;
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<AemTag> getChildren() {
        if (children == null) return Collections.emptyList();
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AemTag aemTag = (AemTag) o;

        return id != null ? id.equals(aemTag.id) : aemTag.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getTitle() + " (" + getId() + ") [" + getChildren().size() + "]" ;
    }

    /*
     * STATIC HELPER METHODS
     */

    /**
     * @param tagId Tag ID
     * @param resolver Resource Resolver
     * @param locale Locale to use for title
     * @param levels Number of levels to recurse for children. 0 for none, 1 for only immediate children, etc.
     * @return Built AemTag or null
     */
    @Nullable
    public static AemTag createAemTag(String tagId, ResourceResolver resolver, Locale locale, int levels) {
        TagManager tagManager = resolver.adaptTo(TagManager.class);
        if (tagManager == null) {
            log.warn("Tag manager could not be resolved when working on {}", tagId);
            return null;
        }
        Tag tag = tagManager.resolve(tagId);
        if (tag == null) {
            log.warn("Tag {} could not be resolved", tagId);
            return null;
        }
        return new AemTag(tag, resolver, locale, levels);
    }
}

