package com.bbby.aem.core.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Provides instance of Gson.
 * <p>
 * Primary purpose is to set the "prettyPrinting" flag in preProd environments.
 */
public interface GsonProvider {

    /**
     * Returns a pre-configured Gson instance.
     * <p>
     * The configuration may differ depending on environment.
     * <p>
     * Unsafe should be false in most all cases to protect against XSS.
     *
     * @param unsafe {@code true} if JSON should not be HTML escaped.
     * @return gson instance
     */
    Gson getGson(boolean unsafe);

    /**
     * Return minimally pre-configured GsonBuilder object.
     *
     * @return GsonBuilder
     */
    GsonBuilder getGsonBuilder();
}
