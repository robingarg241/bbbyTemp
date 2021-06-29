package com.bbby.aem.core.models;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.components.ComponentContext;

/**
 * Used for situations like header and footer. By default, the header is editable on page it has been included on.
 * This causes confusion because the component actually exists on a separate page and that referenced page needs to be
 * activated for changes to show up.
 *
 * @author joelepps
 * 6/22/18
 */
public class BypassComponentHandlingModel extends WCMUsePojo {

    private Object originalValue;

    @Override
    public void activate() throws Exception {
        originalValue = getRequest().getAttribute(ComponentContext.BYPASS_COMPONENT_HANDLING_ON_INCLUDE_ATTRIBUTE);
    }

    public void getDisableComponentEditing() {
        originalValue = getRequest().getAttribute(ComponentContext.BYPASS_COMPONENT_HANDLING_ON_INCLUDE_ATTRIBUTE);
        getRequest().setAttribute(ComponentContext.BYPASS_COMPONENT_HANDLING_ON_INCLUDE_ATTRIBUTE, true);
    }

    public void getRestoreComponentEditing() {
        getRequest().setAttribute(ComponentContext.BYPASS_COMPONENT_HANDLING_ON_INCLUDE_ATTRIBUTE, originalValue);
    }

}

