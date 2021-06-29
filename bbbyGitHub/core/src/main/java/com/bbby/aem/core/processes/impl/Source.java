package com.bbby.aem.core.processes.impl;


import java.io.IOException;
import java.io.InputStream;

/**
 * Represents the source of an asset for import
 */
public interface Source {

    String getName();

    InputStream getStream() throws IOException;

    long getLength() throws IOException;

    HierarchicalElement getElement();

    void close() throws IOException;
    
}
