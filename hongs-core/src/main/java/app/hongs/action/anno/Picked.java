package app.hongs.action.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 关联补充
 * @author Hong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Filter(PickedInvoker.class)
public @interface Picked {
    String  conf() default "default";
    String  form() default "";
}