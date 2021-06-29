package com.bbby.aem.dialog.touch.checkbox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a Widget of type granite/ui/components/foundation/form/checkbox in
 * Touch UI.
 * <p>
 * This is a patched version of the out of the box @CheckBox that has an incompatibility
 * issue in 6.1 SP1.
 * <p>
 * Granite spec states that the 'checked' attribute is used to set a <em>static</em> value
 * for the checkbox. However, OOB @Checkbox treats 'checked' as an <em>initial</em> value and
 * will always output the field.
 * <a href="https://docs.adobe.com/docs/en/aem/6-1/ref/granite-ui/api/jcr_root/libs/granite/ui/components/foundation/form/checkbox/index.html">source</a>
 * <p>
 * This implementation is compatible with @Checkbox. Therefore, when/if @CheckBox is fixed,
 * this annotation can be removed.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface CheckBoxTouch {

    /**
     * Used for Touch UI only
     *
     * @return String
     */
    String text() default "";

    /**
     * Used for Touch UI only
     *
     * @return String
     */
    String title() default "";
}
