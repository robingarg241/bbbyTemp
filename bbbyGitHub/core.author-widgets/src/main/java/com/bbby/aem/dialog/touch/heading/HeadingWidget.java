package com.bbby.aem.dialog.touch.heading;

import com.citytechinc.cq.component.annotations.config.TouchUIWidget;
import com.citytechinc.cq.component.touchuidialog.widget.AbstractTouchUIWidget;

@TouchUIWidget(annotationClass = Heading.class, makerClass = HeadingWidgetMaker.class,
        resourceType = HeadingWidget.RESOURCE_TYPE, ranking = 0)
public class HeadingWidget extends AbstractTouchUIWidget {

    public static final String RESOURCE_TYPE = "granite/ui/components/foundation/heading";

    private final String text;
    private final int level;

    public HeadingWidget(HeadingWidgetParameters parameters) {
        super(parameters);

        text = parameters.getText();
        level = parameters.getLevel();
    }

    public String getText() {
        return text;
    }

    public int getLevel() {
        return level;
    }

}
