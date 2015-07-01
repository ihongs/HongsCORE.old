package app.hongs.action.anno;

import app.hongs.action.anno.Filter;
import app.hongs.action.anno.SupplyInvoker;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 枚举补充
 * @author Hong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Filter(SupplyInvoker.class)
public @interface Picked {
    String  form() default "";
    String  conf() default "default";
}