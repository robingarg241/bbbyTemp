package com.bbby.aem.core.models.component;

import com.day.cq.i18n.I18n;
import com.day.cq.search.Trends;
import com.day.cq.search.facets.Bucket;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.bbby.aem.core.services.SearchGateKeeper;
import com.bbby.aem.core.services.SiteSearch;
import com.bbby.aem.core.util.ServiceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Search Component
 */
@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Search extends ComponentSlingModel {

    private static final Logger log = LoggerFactory.getLogger(Search.class);

    public static final String RESOURCE_TYPE = "bbby/components/content/search";

    /* SEARCH FIELDS */
    private SiteSearch search;
    private SiteSearch.Result result;
    private String query;
    private Trends trends;
    private I18n i18n;
    private TagManager tagManager;

    private boolean searchDenied;

    @Inject
    @SlingObject
    @Required
    private SlingHttpServletRequest request;

    @Inject
    @OSGiService
    @Required
    private SearchGateKeeper searchGateKeeper;

    /* AUTHOR FIELDS */

    @Inject
    @ValueMapValue
    private String searchIn;

    @Inject
    @ValueMapValue
    private String rangeText;

    @Inject
    @ValueMapValue
    private String noResultsText;

    @Inject
    @ValueMapValue
    private String removeFilterText;

    @Inject
    @ValueMapValue
    private String errorText;

    @Inject
    @ValueMapValue
    private List<String> searchTags;

    /**
     * This means that tag counts will be aggregated into the 'Filterable Tag' value buckets. However, this mode can
     * result in inflated counts if pages are tagged with multiple values in the same tag category.
     */
    @Inject
    @ValueMapValue
    private String rollUpTags;

    /*
     * These two maps share a reference to the same List<TagModel> values. Both are needed because sightly only supports
     * Maps with String keys.
     */
    private Map<Tag, List<TagModel>> tagMapInternal;
    private Map<String, List<TagModel>> tagMap;

    @Override
    public void postConstruct() throws Exception {

        SearchGateKeeper.Ticket searchTicket = searchGateKeeper.requestTicket(request);
        try {
            if (searchTicket.isSearchAllowed()) {
                search = new SiteSearch(request);
                search.setHitsPerPage(10);
                initSearchIn();

                long start = System.currentTimeMillis();
                query = search.getQuery();
                result = search.getResult();
                trends = search.getTrends();
                long end = System.currentTimeMillis();

                log.info("Search for {} {} took {}ms", request.getPathInfo(), request.getQueryString(),
                    end - start);

                tagManager = getResourceResolver().adaptTo(TagManager.class);
                i18n = new I18n(request);

                initTagMaps();
                populateTagMap();
            } else {
                log.warn("Search request denied: {}", searchTicket.getMessage());
            }
        } finally {
            searchGateKeeper.releaseTicket(searchTicket);
        }
    }

    private void initSearchIn() {
        String searchIn = getProperties().get("searchIn", String.class);

        String path = request.getParameter("path");
        if (!StringUtils.isBlank(searchIn) && !StringUtils.isBlank(path) && path.startsWith(searchIn)) {
            search.setSearchIn(path);
        } else {
            search.setSearchIn(ServiceUtils.coalesce(searchIn, path));
        }
    }

    public String getSearchIn() {
        return searchIn;
    }

    public String getRangeText() {
        return ServiceUtils.coalesce(rangeText, i18n.get("{2} Results for <i>{3}</i>"));
    }

    public String getNoResultsText() {
        return ServiceUtils.coalesce(noResultsText, i18n.get("No search results for <i>{3}</i>"));
    }

    public String getErrorText() {
        return errorText;
    }

    public String getRemoveFilterText() {
        return removeFilterText;
    }

    public String getQuery() {
        return query;
    }

    public SiteSearch.Result getResult() {
        return result;
    }

    public List<SiteSearch.Hit> getSearchResults() {
        if (result != null) {
            return result.getHits();
        }
        return null;
    }

    public String getSpellcheck() {
        if (result != null) {
            return result.getSpellcheck();
        }
        return null;
    }

    public String getSpellcheckUrl() throws UnsupportedEncodingException {
        String spellcheck = getSpellcheck();
        if (spellcheck != null) {
            return getCurrentPage().getPath() + ".html?q=" + URLEncoder.encode(spellcheck, "UTF-8");
        }
        return null;
    }

    public int getResultSize() {
        if (result != null && result.getHits() != null) {
            return result.getHits().size();
        }
        return 0;
    }

    public long getResultStartCount() {
        if (result != null) {
            return result.getStartIndex() + 1;
        }
        return 0;
    }

    public long getResultEndCount() {
        if (result != null && result.getHits() != null) {
            return result.getStartIndex() + result.getHits().size();
        }
        return 0;
    }

    public Trends getTrends() {
        return trends;
    }

    public boolean isSearchDenied() {
        return searchDenied;
    }

    public List<String> getTrendQueries() throws RepositoryException, UnsupportedEncodingException {
        if (trends != null && trends.getQueries() != null && !trends.getQueries().isEmpty()) {
            return trends.getQueries()
                .stream()
                .map(a -> getCurrentPage().getPath() + ".html?q=" + urlEncode(a.getQuery()))
                .collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }

    public List<String> getTagKeys() {
        List<String> tagList = new ArrayList<>(tagMap.keySet());
        tagList.sort(String::compareTo);
        return tagList.stream().filter(a -> !tagMap.get(a).isEmpty()).collect(Collectors.toList());
    }

    public Map<String, List<TagModel>> getTags() {
        return tagMap;
    }

    public List<String> getRelatedQueries() throws RepositoryException {
        List<String> relatedQueries = new ArrayList<>();
        if (search != null && search.getRelatedQueries() != null) {
            relatedQueries = search.getRelatedQueries()
                .stream()
                .map(rq -> getCurrentPage().getPath() + ".html?q=" + urlEncode(rq))
                .collect(Collectors.toList());
        }
        return relatedQueries;
    }

    public boolean getShowPagination() throws RepositoryException {
        return result != null && result.getResultPages().size() > 1;
    }

    public SiteSearch getSearch() {
        return search;
    }

    public TagManager getTagManager() {
        return tagManager;
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode " + s + " with UTF-8", e);
            return null;
        }
    }

    private void initTagMaps() {
        this.tagMapInternal = new HashMap<>();
        this.tagMap = new HashMap<>();
        if (searchTags == null) return;

        Locale locale = getCurrentPage().getLanguage(false);

        for (String tagStr : searchTags) {
            Tag tag = tagManager.resolve(tagStr);
            if (tag != null) {
                // Shared array means updates to one list affects both maps (this is intended)
                List<TagModel> sharedArray = new ArrayList<>();
                tagMap.put(tag.getTitle(locale), sharedArray);
                tagMapInternal.put(tag, sharedArray);
            } else {
                log.warn("Cannot resolve tag {}", tagStr);
            }
        }
    }

    private void populateTagMap() throws RepositoryException {
        /*
         * Pretty inefficient (3 layers of for loops + sorting), but number of tags should always be minimal.
         */
        if (result != null && result.getFacets() != null && result.getFacets().containsKey("tags")) {
            for (Map.Entry<Tag, List<TagModel>> entry : tagMapInternal.entrySet()) {
                Tag parentTag = entry.getKey();
                List<TagModel> matchingTags = entry.getValue();

                List<Bucket> buckets = result.getFacets().get("tags").getBuckets();
                for (Bucket bucket : buckets) {
                    Tag tag = tagManager.resolve(bucket.getValue());
                    if (tag == null) {
                        log.warn("Skipping bucket {} for {}, it did not resolve to a tag", bucket.getValue(),
                            getQuery());
                        continue;
                    }
                    Tag matchingTag = ServiceUtils.isDescendantTag(parentTag, tag);
                    if (matchingTag != null) {
                        if (!"true".equals(rollUpTags)) {
                            matchingTags.add(new TagModel(this, tag, bucket.getCount()));
                        } else {
                            boolean foundMatch = false;
                            for (TagModel alreadyMatchedTag : matchingTags) {
                                if (matchingTag.equals(alreadyMatchedTag.getTag())) {
                                    alreadyMatchedTag.addCount(bucket.getCount());
                                    foundMatch = true;
                                }
                            }
                            if (!foundMatch) {
                                matchingTags.add(new TagModel(this, matchingTag, bucket.getCount()));
                                matchingTags.sort(Comparator.comparing(x -> x.getTag().getTagID()));
                            }
                        }
                    }
                }
            }
        }
    }

    public static class TagModel {

        private Tag tag;
        private long count;
        private Locale locale;
        private List<String> tagParameters;
        private String currentPath;
        private String query;

        public TagModel(Search model, Tag tag, long count) {
            this.tag = tag;
            this.count = count;

            String[] tagParms = model.request.getParameterValues("tag");
            this.tagParameters = (tagParms != null) ? Arrays.asList(tagParms) : new ArrayList<>(0);

            this.locale = model.getCurrentPage().getLanguage(false);
            this.currentPath = model.getCurrentPage().getPath() + ".html";
            this.query = model.getQuery();
        }

        public boolean isSelected() {
            return tagParameters != null && tagParameters.contains(tag.getTagID());
        }

        public Tag getTag() {
            return tag;
        }

        public long getCount() {
            return count;
        }

        public void addCount(long count) {
            this.count += count;
        }

        public String getLocalizedTagTitle() {
            return tag.getTitle(locale);
        }

        public String getQueryUrl() {
            if (isSelected()) {
                return currentPath + "?q=" + urlEncode(query);
            } else {
                return currentPath + "?q=" + urlEncode(query) + "&tag=" + urlEncode(tag.getTagID());
            }
        }
    }

}
