package com.bbby.aem.core.services;

import org.apache.sling.api.SlingHttpServletRequest;

import javax.annotation.Nonnull;

/**
 * Helper service that rate-limits the number of concurrent search requests that can be processed.
 * <p>
 * This is critical when using native AEM search. Search, by its nature, cannot be effectively cached and is therefore a
 * sufficiently large influx of concurrent search requests can easily bring down an AEM server.
 *
 * @author joelepps 10/8/16
 */
public interface SearchGateKeeper {

    /**
     * Ticket indicating if search is allowed or not. You must always release a ticket when done no matter if search was
     * allowed or not.
     */
    class Ticket {

        private final SlingHttpServletRequest request;
        private final boolean searchAllowed;
        private final String message;

        public Ticket(SlingHttpServletRequest request, boolean searchAllowed, String message) {
            this.request = request;
            this.searchAllowed = searchAllowed;
            this.message = message;
        }

        public boolean isSearchAllowed() {
            return searchAllowed;
        }

        public SlingHttpServletRequest getRequest() {
            return request;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Returns a {@link Ticket} indicating whether or not it is okay to perform a search.
     * <p>
     * You must always invoke {@link #releaseTicket} even if search was not allowed. To be safe this should be done in a
     * try/finally block.
     *
     * @param request request object
     * @return Ticket instance, never null.
     */
    Ticket requestTicket(@Nonnull SlingHttpServletRequest request);

    /**
     * Must be called once, and only once, for any ticket returned by {@link #requestTicket}.
     *
     * @param ticket ticket
     */
    void releaseTicket(@Nonnull Ticket ticket);
}
