package com.bbby.aem.core.services.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.bbby.aem.core.services.AltPageResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link AltPageResolver}.
 *
 * @author joelepps 7/13/17
 */
public class AltPageResolverImpl implements AltPageResolver {

    private static final Logger log = LoggerFactory.getLogger(AltPageResolverImpl.class);

    /**
     * Parameter 1: resource type Parameter 2: search path
     */
    private static final String SQL2_QUERY = "SELECT * FROM [cq:Page] AS s WHERE s.[jcr:content/sling:resourceType] = '%s' AND ISDESCENDANTNODE('%s')";

    private final ResourceResolver resolver;
    private final PageManager pageManager;

    public AltPageResolverImpl(ResourceResolver resolver) {
        this.resolver = resolver;
        this.pageManager = resolver.adaptTo(PageManager.class);
    }

    @Override
    public List<Page> getAltPages(Page page, String homePageResourceType) {
        List<Page> pages = new ArrayList<>();

        if (page == null) return pages;
        if (StringUtils.isBlank(homePageResourceType)) return pages;

        try {
            Page homePage = getHomePage(page, homePageResourceType);
            if (homePage == null) {
                log.debug("No home page found above {}, returning empty alt page list", page.getPath());
                return pages;
            }
            if (!homePage.getPath().startsWith("/content/")) {
                log.debug("Home page not supported, must be under /content/. {}", homePage.getPath());
                return pages;
            }

            String relativePath = page.getPath().replace(homePage.getPath(), "");

            NodeIterator results = searchForHomePages(resolver, homePage);
            while (results.hasNext()) {
                Node homePageNode = results.nextNode();
                Page candidateHomePage = pageManager.getPage(homePageNode.getPath());

                if (!shouldIncludeHomePage(candidateHomePage)) {
                    log.trace("Skipping home page {}", homePageNode.getPath());
                    continue;
                }

                String potentialPath = candidateHomePage.getPath() + "/" + relativePath;

                Resource resource = resolver.getResource(potentialPath);
                if (resource == null) continue;

                Page candidatePage = resource.adaptTo(Page.class);
                if (candidatePage == null) continue;
                if (candidatePage.getPath().equals(page.getPath())) continue;

                pages.add(candidatePage);
            }

        } catch (Exception e) {
            log.error("Failed to build alt locale pages for " + page.getPath(), e);
        }

        return pages;
    }

    /**
     * Finds the homepage based on the first parent matching the supplied {@code homePageResourceType} type.
     */
    @Nullable
    private static Page getHomePage(Page page, String homePageResourceType) {
        if (page == null) return null;

        Resource contentResource = page.getContentResource();
        if (contentResource == null) {
            // skip and continue up
            return getHomePage(page.getParent(), homePageResourceType);
        }

        if (homePageResourceType.equals(contentResource.getResourceType())) {
            return page;
        } else {
            return getHomePage(page.getParent(), homePageResourceType);
        }
    }

    private static NodeIterator searchForHomePages(ResourceResolver resolver, Page homePage)
        throws RepositoryException {
        Session session = resolver.adaptTo(Session.class);
        if (session == null) throw new RepositoryException("Failed to resolve session for " + homePage.getPath());

        Workspace workspace = session.getWorkspace();
        QueryManager queryManager = workspace.getQueryManager();

        // searchPath is the parent of homePage that is the immediate child of /content
        String searchPath = homePage.getAbsoluteParent(1).getPath();
        String resourceType = homePage.getProperties().get("sling:resourceType", String.class);

        String sqlQuery = String.format(SQL2_QUERY, resourceType, searchPath);

        log.trace("Searching with query: {}", sqlQuery);

        long queryStart = System.currentTimeMillis();

        Query query = queryManager.createQuery(sqlQuery, Query.JCR_SQL2);

        QueryResult queryResult = query.execute();
        NodeIterator nodeIterator = queryResult.getNodes();

        log.debug("Query finished in {} ms: {}", System.currentTimeMillis() - queryStart, sqlQuery);

        return nodeIterator;
    }

    private static boolean shouldIncludeHomePage(Page homePage) {
        if (homePage == null) return false;
        if (homePage.isHideInNav()) return false;
        return true;
    }
}
