package app.hongs.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动作注解执行器
 * 非自动调用不起作用, 仅能用于actoin方法
 * 其他动作注解需要从此注解继承, 并指定invoker
 * @author Hong
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface ActionAnnotation {
    Class value();
}
