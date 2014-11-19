package app.hongs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据注入
 * @author Hong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ActionWrapper(InjectInvoker.class)
public @interface Inject {
    String[] keys();
    String   conf() default "default";
    TYPES    type() default TYPES.RSP;
    
    public static enum TYPES { REQ, RSP }
}
