package com.bbby.aem.dialog.touch.heading;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a Widget of type granite/ui/components/foundation/heading in
 * Touch UI.
 *
 * @author joelepps
 *         4/6/2015
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Heading {

    /**
     * Text
     *
     * @return String
     */
    String text() default "";

    /**
     * Heading level/size (1-6)
     *
     * @return String
     */
    int level() default 6;
}
