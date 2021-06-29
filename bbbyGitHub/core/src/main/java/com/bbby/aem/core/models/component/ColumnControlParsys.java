package com.bbby.aem.core.models.component;

import com.adobe.cq.sightly.WCMUsePojo;
import com.bbby.aem.core.util.SlingModelUtils;
import org.apache.sling.api.resource.Resource;

/**
 * Model used by the Column Control's Parsys component. Simply loads the model for the parent Column Control for
 * access by each column.
 *
 * @author joelepps
 * 3/9/18
 */
public class ColumnControlParsys extends WCMUsePojo {

    private ColumnControl columnControl;

    @Override
    public void activate() {
        Resource columnControlResource = getResource().getParent();
        columnControl = SlingModelUtils.buildComponentSlingModel(columnControlResource, ColumnControl.class);
    }

    public ColumnControl getColumnControl() {
        return columnControl;
    }
}
