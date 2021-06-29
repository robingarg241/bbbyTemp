package com.bbby.aem.dialog.touch.checkbox;

import com.citytechinc.cq.component.annotations.config.TouchUIWidget;
import com.citytechinc.cq.component.touchuidialog.widget.AbstractTouchUIWidget;

@TouchUIWidget(annotationClass = CheckBoxTouch.class, makerClass = CheckboxTouchWidgetMaker.class,
        resourceType = CheckboxTouchWidget.RESOURCE_TYPE, ranking = 100)
public class CheckboxTouchWidget extends AbstractTouchUIWidget {

    public static final String RESOURCE_TYPE = "granite/ui/components/foundation/form/checkbox";

    private final String text;
    private final String title;

    // HERO: isChecked indicates static value, not initial state.
    // private final boolean checked;

    public CheckboxTouchWidget(CheckboxTouchWidgetParameters parameters) {
        super(parameters);

        text = parameters.getText();
        title = parameters.getTitle();

        // HERO: isChecked indicates static value, not initial state.
        // checked = parameters.isChecked();

    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    // HERO: isChecked indicates static value, not initial state.
    /*public boolean isChecked() {
        return checked;
    }*/

}
