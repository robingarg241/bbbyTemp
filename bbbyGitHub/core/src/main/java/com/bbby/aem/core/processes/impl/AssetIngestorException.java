package com.bbby.aem.core.processes.impl;


/**
 * Generic exception for asset ingestor process.
 */
public class AssetIngestorException extends Exception {

    /**
     * Default constructor.
     */
    public AssetIngestorException() {
    }

    /**
     * Constructor with an error message.
     *
     * @param message an error message
     */
    public AssetIngestorException(String message) {
        super(message);
    }

    /**
     * Constructor with an error message and a cause.
     * @param message an error message
     * @param cause a cause
     */
    public AssetIngestorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with a cause.
     * @param cause a cause
     */
    public AssetIngestorException(Throwable cause) {
        super(cause);
    }
}
