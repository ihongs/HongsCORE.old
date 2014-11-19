package app.hongs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 处理列表中的选择数据
 * @author Hong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ActionWrapper(InListInvoker.class)
public @interface InList {
    String[] keys() default {};
    String   conf() default "default";
    String   lang() default "default";
}
