package net.zyski.zmacro.client.Macro;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Macro {
    String name();

    String description() default "";

    String version() default "1.0";

    String author() default "Unknown";

    String icon() default "textures/item/paper.png";
}

