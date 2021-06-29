package com.bbby.aem.core.services.impl;

import com.bbby.aem.core.services.SearchGateKeeper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Default implementation for search gate keeper.
 *
 * @author joelepps 10/8/16
 */
@Component(
    immediate = true,
    service = SearchGateKeeper.class)
@Designate(ocd = SearchGateKeeperImplConfiguration.class)
public class SearchGateKeeperImpl implements SearchGateKeeper {

    private static final Logger log = LoggerFactory.getLogger(SearchGateKeeper.class);

    private int activeSearches;

    private int max;

    @Activate
    public synchronized void activate(SearchGateKeeperImplConfiguration config) {
        max = config.maxSearches();
    }

    @Deactivate
    public synchronized void deactivate() {
        activeSearches = 0;
    }

    @Override
    public synchronized Ticket requestTicket(@Nonnull SlingHttpServletRequest request) {

        String message = "Search count at " + activeSearches + " (max " + max + ") for "
            + request.getPathInfo() + " " + request.getQueryString();

        if (activeSearches >= max) {
            log.debug("requestTicket: Search DENIED: " + message);
            return new Ticket(request, false, message);
        } else {
            activeSearches++;
            log.debug("requestTicket: Search ALLOWED: " + message);
            return new Ticket(request, true, message);
        }
    }

    @Override
    public synchronized void releaseTicket(@Nonnull Ticket ticket) {
        SlingHttpServletRequest request = ticket.getRequest();

        if (!ticket.isSearchAllowed()) {
            log.trace("releaseTicket: No action needed for ticket release as search was not allowed: {} {}",
                request.getPathInfo(), request.getQueryString());
            return;
        }

        log.debug("releaseTicket: {} {} with active count {}", request.getPathInfo(), request.getQueryString(),
            activeSearches);

        if (activeSearches == 0) {

            // this should never happen
            log.error("Count is at 0, cannot decrement further. {} {}",
                request.getPathInfo(),
                request.getQueryString());

        } else {

            log.debug("releaseTicket: released {} {}, decrementing {}",
                request.getPathInfo(),
                request.getQueryString(),
                activeSearches);

            activeSearches--;
        }
    }
}
