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
package com.bbby.aem.dialog.touch.multifield;

import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.annotations.IgnoreDialogField;
import com.citytechinc.cq.component.dialog.DialogFieldConfig;
import com.citytechinc.cq.component.dialog.exception.InvalidComponentClassException;
import com.citytechinc.cq.component.dialog.exception.InvalidComponentFieldException;
import com.citytechinc.cq.component.dialog.util.DialogUtil;
import com.citytechinc.cq.component.maven.util.ComponentMojoUtil;
import com.citytechinc.cq.component.touchuidialog.TouchUIDialogElement;
import com.citytechinc.cq.component.touchuidialog.TouchUIDialogElementComparator;
import com.citytechinc.cq.component.touchuidialog.exceptions.TouchUIDialogGenerationException;
import com.citytechinc.cq.component.touchuidialog.widget.factory.TouchUIWidgetFactory;
import com.citytechinc.cq.component.touchuidialog.widget.maker.AbstractTouchUIWidgetMaker;
import com.citytechinc.cq.component.touchuidialog.widget.maker.TouchUIWidgetMakerParameters;
import com.bbby.aem.dialog.classic.multifield.MultiCompositeField;
import javassist.CtMember;
import javassist.CtMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiCompositeFieldWidgetMaker extends AbstractTouchUIWidgetMaker<MultiCompositeFieldWidgetParameters> {

    public MultiCompositeFieldWidgetMaker(TouchUIWidgetMakerParameters parameters) {
        super(parameters);
    }

    @Override
    public TouchUIDialogElement make(MultiCompositeFieldWidgetParameters widgetParameters) throws ClassNotFoundException, InvalidComponentFieldException, TouchUIDialogGenerationException {

        MultiCompositeField multiCompositeFieldAnnotation = getAnnotation(MultiCompositeField.class);

        try {
            widgetParameters.setItems(buildLayoutItems(multiCompositeFieldAnnotation));
            widgetParameters.setMaxLimit(multiCompositeFieldAnnotation.maxLimit());
        } catch (Exception e) {
            throw new TouchUIDialogGenerationException("Exception encountered while constructing contained elements for the MultiCompositeField " 
                        + parameters.getDialogFieldConfig().getFieldName() + " of class " + parameters.getContainingClass().getName(), e);
        }

        return new MultiCompositeFieldWidget(widgetParameters);
    }

    private List<TouchUIDialogElement> buildLayoutItems(MultiCompositeField multiCompositeFieldAnnotation) throws Exception {

        List<CtMember> fieldsAndMethods = new ArrayList<CtMember>();

        fieldsAndMethods.addAll(ComponentMojoUtil.collectFields(getCtType()));
        fieldsAndMethods.addAll(ComponentMojoUtil.collectMethods(getCtType()));

        List<TouchUIDialogElement> elements = new ArrayList<TouchUIDialogElement>();

        for (CtMember member : fieldsAndMethods) {
            if (!member.hasAnnotation(IgnoreDialogField.class)) {

                DialogFieldConfig dialogFieldConfig = null;
                if (member instanceof CtMethod) {
                    try {
                        dialogFieldConfig = DialogUtil.getDialogFieldFromSuperClasses((CtMethod) member);
                    } catch (InvalidComponentClassException e) {
                        throw new InvalidComponentFieldException(e.getMessage(), e);
                    }
                } else {
                    if (member.hasAnnotation(DialogField.class)) {
                        dialogFieldConfig = new DialogFieldConfig((DialogField) member.getAnnotation(DialogField.class), member);
                    }
                }

                if (dialogFieldConfig != null && !dialogFieldConfig.isSuppressTouchUI()) {
                    Class<?> fieldClass = parameters.getClassLoader().loadClass(member.getDeclaringClass().getName());

                    double ranking = dialogFieldConfig.getRanking();

                    TouchUIWidgetMakerParameters curFieldMember = new TouchUIWidgetMakerParameters();
                    curFieldMember.setDialogFieldConfig(dialogFieldConfig);
                    curFieldMember.setContainingClass(fieldClass);
                    curFieldMember.setClassLoader(parameters.getClassLoader());
                    curFieldMember.setClassPool(parameters.getClassPool());
                    curFieldMember.setWidgetRegistry(parameters.getWidgetRegistry());
                    curFieldMember.setUseDotSlashInName(true);

                    TouchUIDialogElement currentDialogElement = TouchUIWidgetFactory.make(curFieldMember, -1);
                    if (currentDialogElement != null) {
                        currentDialogElement.setRanking(ranking);
                        elements.add(currentDialogElement);
                    }
                }
            }
        }

        Collections.sort(elements, new TouchUIDialogElementComparator());

        return elements;
    }

}
