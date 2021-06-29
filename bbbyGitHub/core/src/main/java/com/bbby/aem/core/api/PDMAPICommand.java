package com.bbby.aem.core.api;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import java.net.URI;
import java.net.URL;
import java.util.List;

public interface PDMAPICommand {
    String METHOD_GET = "GET";
    String METHOD_POST = "POST";
    String METHOD_PUT = "PUT";
    String METHOD_DELETE = "DELETE";

    /**
     * Get the request method to be used.
     *
     * @return the request method
     */
    String getMethod();

    /**
     * Get the path of the backend command.
     *
     * @return the command path
     */
    String getURL();

    /**
     * Return the list of query parameters to be used.
     *
     * @return the list of query parameters
     */
    List<NameValuePair> getQueryParameters();

    /**
     * Return the list of header attributes to be used.
     *
     * @return the list of header attributes
     */
    List<Header> getHeaders();

    /**
     * Return the entity for the body content.
     *
     * @return the body content entity
     */
    HttpEntity getEntity();

    /**
     * Does the backend service require authentication?
     *
     * @return {@code true} if the backend API requires authentication, otherwise {@code false}
     */
    boolean isAuthenticationRequired();
}
