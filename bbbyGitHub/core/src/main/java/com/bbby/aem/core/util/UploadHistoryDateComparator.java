package com.bbby.aem.core.util;


import java.io.Serializable;
import java.util.Comparator;

import com.bbby.aem.core.models.data.UploadHistoryItem;

/**
 * The Class UploadHistoryDateComparator.
 *
 * @author kkoduru,  hero-digital
 *
 */
public class UploadHistoryDateComparator implements Serializable, Comparator<UploadHistoryItem>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(UploadHistoryItem a, UploadHistoryItem b)
    {
        return b.getUploadDate().compareTo(a.getUploadDate());
    }

}

