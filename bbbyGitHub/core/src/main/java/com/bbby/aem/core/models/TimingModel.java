package com.bbby.aem.core.models;

import com.adobe.cq.sightly.WCMUsePojo;
import org.apache.commons.lang3.BooleanUtils;

/**
 * If request has parameter "timing=true", then timing chart is rendered on the page as an HTML comment.
 * <p>
 * Useful for debugging performance issues.
 *
 * @author joelepps
 *
 */
public class TimingModel extends WCMUsePojo {

    private boolean showTiming;

    @Override
    public void activate() throws Exception {
        showTiming = BooleanUtils.toBoolean(getRequest().getParameter("timing"));
    }

    public boolean getShowTiming() {
        return showTiming;
    }

}
