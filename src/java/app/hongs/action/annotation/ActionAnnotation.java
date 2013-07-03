package app.hongs.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1>动作注解执行器</h1>
 * <pre>
 * 非自动调用不起作用, 仅能用于动作方法上
 * 其他动作注解需声明此注解, 并指定调用类
 * </pre>
 * @author Hong
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionAnnotation {
    Class value();
}
