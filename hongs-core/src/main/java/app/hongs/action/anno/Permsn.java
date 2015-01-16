package app.hongs.action.anno;

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
public @interface Permsn {
    String  role() default "";
    String  conf() default "default";
}