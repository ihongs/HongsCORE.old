package app.hongs.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据追加
 * @author Hong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ActionAnnotation(DataInvoker.class)
public @interface Data {
    TYPES    type() default TYPES.RSP;
    String   conf() default "default";
    String[] keys() default {};
    
    public static enum TYPES { REQ, RSP }
}
