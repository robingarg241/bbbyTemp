package com.bbby.aem.core.services.impl;

import com.bbby.aem.core.services.SelectorValidationFilterExtension;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;

/**
 * Adds supplemental logic to {@link com.bbby.aem.core.filters.SelectorValidationFilter} which whitelists all requests
 * from configured users or user groups.
 * <p>
 * Needed for most author interfaces which make use of selectors that are not allowed for typical end users of the site.
 *
 * @author joelepps
 * 7/31/18
 */
@Component(
    immediate = true,
    service = SelectorValidationFilterExtension.class)
@Designate(ocd = SelectorValidationSessionWhitelistConfig.class)
public class SelectorValidationSessionWhitelist implements SelectorValidationFilterExtension {

    private static final Logger log = LoggerFactory.getLogger(SelectorValidationSessionWhitelist.class);

    private Set<String> whiteListedUsers;
    private Set<String> whitelistedGroups;

    @Activate
    protected void activate(SelectorValidationSessionWhitelistConfig config) {
        whiteListedUsers = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(config.users())));
        whitelistedGroups = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(config.groups())));
    }

    @Override
    public boolean isValid(SlingHttpServletRequest request) {
        if (whitelistedGroups.isEmpty() && whiteListedUsers.isEmpty()) return false;

        Authorizable authorizable = getAuthorizable(request);
        if (authorizable == null) return false;

        if (!whiteListedUsers.isEmpty()) {
            String userId = getUserID(authorizable);
            if (userId != null && whiteListedUsers.contains(userId)) return true;
        }

        if (!whitelistedGroups.isEmpty()) {
            Set<String> memberOf = getGroups(authorizable);

            boolean hasMatch = !Collections.disjoint(whitelistedGroups, memberOf);
            if (hasMatch) return true;
        }

        return false;
    }

    private Authorizable getAuthorizable(SlingHttpServletRequest request) {
        ResourceResolver resolver = request.getResourceResolver();

        Session session = resolver.adaptTo(Session.class);
        if (session == null) {
            log.warn("Unable to get session for {}", request.getPathInfo());
            return null;
        }

        UserManager userManager = resolver.adaptTo(UserManager.class);
        if (userManager == null) {
            log.warn("Unable to get user manager for {}", request.getPathInfo());
            return null;
        }

        Authorizable authorizable;
        try {
            authorizable = userManager.getAuthorizable(session.getUserID());
        } catch (RepositoryException e) {
            log.warn("Unable to get authorizable for " + session.getUserID(), e);
            return null;
        }

        return authorizable;
    }

    private String getUserID(Authorizable authorizable) {
        try {
            return authorizable.getID();
        } catch (RepositoryException e) {
            log.error("Unable to get user id for " + authorizable, e);
            return null;
        }
    }

    private Set<String> getGroups(Authorizable authorizable) {

        Set<String> memberOf = new HashSet<>();
        try {
            Iterator<Group> i = authorizable.memberOf();
            while (i.hasNext()) {
                memberOf.add(i.next().getID());
            }
        } catch (RepositoryException e) {
            log.error("Unable to get groups for " + authorizable, e);
            return Collections.emptySet();
        }

        return memberOf;
    }

}
