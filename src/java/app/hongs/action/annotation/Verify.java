package app.hongs.action.annotation;

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
@ActionAnnotation(InjectInvoker.class)
public @interface Verify {
    String[] rules();
    String   data() default "";
    String   lang() default "default";
}
