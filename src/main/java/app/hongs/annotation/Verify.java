package app.hongs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据校验
 * @author Hong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ActionWrapper(ActionInvoker.class)
public @interface Verify {
    String form() default "";
    String coll() default "default";
    boolean clear() default false;
}
