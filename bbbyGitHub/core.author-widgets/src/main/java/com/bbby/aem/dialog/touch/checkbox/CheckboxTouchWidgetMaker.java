/**
 *    Copyright 2013 CITYTECH, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.bbby.aem.dialog.touch.checkbox;

import com.citytechinc.cq.component.dialog.exception.InvalidComponentFieldException;
import com.citytechinc.cq.component.touchuidialog.TouchUIDialogElement;
import com.citytechinc.cq.component.touchuidialog.exceptions.TouchUIDialogGenerationException;
import com.citytechinc.cq.component.touchuidialog.widget.maker.AbstractTouchUIWidgetMaker;
import com.citytechinc.cq.component.touchuidialog.widget.maker.TouchUIWidgetMakerParameters;
import org.codehaus.plexus.util.StringUtils;

public class CheckboxTouchWidgetMaker extends AbstractTouchUIWidgetMaker<CheckboxTouchWidgetParameters> {

	public CheckboxTouchWidgetMaker(TouchUIWidgetMakerParameters parameters) {
		super(parameters);
	}

	@Override
	public TouchUIDialogElement make(CheckboxTouchWidgetParameters widgetParameters) throws ClassNotFoundException,
		InvalidComponentFieldException, TouchUIDialogGenerationException {

		CheckBoxTouch checkboxAnnotation = getAnnotation(CheckBoxTouch.class);

		widgetParameters.setText(getTextForField(checkboxAnnotation));
		widgetParameters.setTitle(getTitleForField(checkboxAnnotation));
		// HERO: isChecked indicates static value, not initial state.
		// widgetParameters.setChecked(getCheckedForField(checkboxAnnotation));

		return new CheckboxTouchWidget(widgetParameters);
	}

	public String getTextForField(CheckBoxTouch annotation) {
		if (annotation != null && StringUtils.isNotBlank(annotation.text())) {
			return annotation.text();
		}

		return null;
	}

	public String getTitleForField(CheckBoxTouch annotation) {
		if (annotation != null && StringUtils.isNotBlank(annotation.title())) {
			return annotation.title();
		}

		return null;
	}

	// HERO: isChecked indicates static value, not initial state.
	/*
	public boolean getCheckedForField(CheckBox annotation) {
		if (annotation != null) {
			return annotation.checked();
		}

		return false;
	}*/

}
