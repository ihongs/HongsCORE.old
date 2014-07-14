package app.hongs.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动作注解执行器
 *
 * <p>
 * 非自动调用不起作用, 仅能用于动作方法上;
 * 其他动作注解需声明此注解, 并指定调用类.
 * </p>
 *
 * @author Hong
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Action {
}
