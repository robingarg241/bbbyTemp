package com.bbby.aem.core.util;

import com.bbby.aem.core.models.component.ComponentSlingModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper methods for creating {@link ComponentSlingModel} instances from a resource.
 *
 * @author joelepps
 *
 */
public class SlingModelUtils {

    private static final Logger log = LoggerFactory.getLogger(SlingModelUtils.class);

    private static final String CACHE_KEY_PREFIX = "multifield.";

    /**
     * Constructs a new instance of a {@link ComponentSlingModel} object. Object's fields must match the JCR property
     * name in order to be populated correctly.
     *
     * @param resource Resource to convert to T
     * @param request request object
     * @param type Type of T
     * @return Instance of T
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends ComponentSlingModel> T buildComponentSlingModel(
        @Nullable Resource resource,
        @Nullable SlingHttpServletRequest request,
        @Nonnull Class<T> type) {

        log.trace("Building new {} instance", type);

        if (resource == null) {
            return null;
        }

        ComponentSlingModel model = resource.adaptTo(type);
        if (model == null) return null;

        // Some models may require the request object in order to initialize
        // Set request and re-invoke postConstruct here.
        try {
            if (request != null) {
                model.setRequest(request);
                model.postConstruct();
            }
        } catch (Exception e) {
            log.error("Failed to initialize " + resource + " for " + type, e);
            return null;
        }

        return (T) model;
    }

    /**
     * Constructs a new instance of a {@link ComponentSlingModel} object. Object's fields must match the JCR property
     * name in order to be populated correctly.
     *
     * @param resource Resource to convert to T
     * @param type Type of T
     * @return Instance of T
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends ComponentSlingModel> T buildComponentSlingModel(
        @Nullable Resource resource,
        @Nonnull Class<T> type) {

        log.trace("Building new {} instance", type);

        if (resource == null) {
            return null;
        }

        ComponentSlingModel model = resource.adaptTo(type);
        if (model == null) return null;

        return (T) model;
    }

    /**
     * Creates a list of {@link ComponentSlingModel} objects based on a parent resource node.
     *
     * @param resource Parent resource node of list of T nodes
     * @param type Type of T
     * @return List of T
     */
    @SuppressWarnings("unchecked")
    public static <T extends ComponentSlingModel> List<T> buildComponentSlingModels(
        @Nullable Resource resource,
        @Nonnull Class<T> type) {

        List<T> list = new ArrayList<T>();

        if (resource != null) {

            log.trace("Trying to fetch the items from multifield node {}", resource.getName());
            Iterator<Resource> multiFieldItems = resource.listChildren();

            while (multiFieldItems.hasNext()) {
                Resource item = multiFieldItems.next();

                T itemToAdd = buildComponentSlingModel(item, type);
                if (itemToAdd != null) {
                    list.add(itemToAdd);
                }
            }
        } else {
            log.trace("Resource is null, returning empty list for null");
        }
        return list;
    }

    /**
     * Creates a list of {@link ComponentSlingModel} objects based on a parent resource node.
     *
     * @param resource Parent resource node of list of T nodes
     * @param request request object
     * @param type Type of T
     * @param useCache true to cache result in request attribute
     * @return List of T
     */
    @SuppressWarnings("unchecked")
    public static <T extends ComponentSlingModel> List<T> buildComponentSlingModels(
        @Nullable Resource resource,
        @Nullable SlingHttpServletRequest request,
        @Nonnull Class<T> type,
        boolean useCache) {

        List<T> list = new ArrayList<T>();

        if (resource != null) {

            // CACHE CHECK
            if (useCache && request != null) {
                log.trace("Checking cache for {}", resource);
                Object obj = request.getAttribute(CACHE_KEY_PREFIX + resource.getPath());
                if (obj != null) {
                    return (List<T>) obj;
                }
            }

            log.trace("Trying to fetch the items from multifield node {}", resource.getName());
            Iterator<Resource> multiFieldItems = resource.listChildren();

            while (multiFieldItems.hasNext()) {
                Resource item = multiFieldItems.next();

                T itemToAdd = buildComponentSlingModel(item, request, type);
                if (itemToAdd != null) {
                    list.add(itemToAdd);
                }
            }

            // CACHE STORE
            if (useCache && request != null) request.setAttribute(CACHE_KEY_PREFIX + resource.getPath(), list);
        } else {
            log.trace("Resource is null, returning empty list for null");
        }
        return list;
    }
}
