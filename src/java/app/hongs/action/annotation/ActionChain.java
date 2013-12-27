package app.hongs.action.annotation;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * 动作注解链
 *
 * <h3>异常代码</h3>
 * <pre>
 * 区间: 0x1100~0x110f
 * 0x1102 没有找到动作链方法，不存在或遇安全问题
 * 0x1104 无法执行动作链方法，无法访问或参数错误
 * 0x1106 无法执行动作方法，无法访问或参数错误
 * 0x1108 动作链索引溢出
 * 0x1100 动作内异常，当动作内产生异常需要直接抛出时，都可以使用此代号
 * </pre>
 *
 * @author Hong
 */
public class ActionChain {
    private int idx;
    private Method method;
    private Object object;
    private ActionHelper helper;
    private Annotation[] annotations;

    public ActionChain(Method method, Object object, ActionHelper helper) {
        this.annotations = method.getAnnotations();
        this.method = method;
        this.object = object;
        this.helper = helper;
        this.idx = 0;
    }

    public void doAction() throws HongsException {
        // 如果超出链长度, 终止执行
        if ( idx  >  annotations.length) {
            throw new HongsException(0x1108, "Action annotation out of index: "
            +idx+">"+annotations.length);
        }

        // 如果已到达链尾, 则执行动作函数
        if ( idx ==  annotations.length) {
            invokeAction();
            return;
        }

        // 如果不是动作链, 则跳过注解检查
        ActionAnnotation ann1;
        Annotation ann2 = annotations[idx++];
        if (ann2 instanceof ActionAnnotation) {
            ann1 = ( ActionAnnotation ) ann2;
        }
        else {
            ann1 = ann2.annotationType().getAnnotation(ActionAnnotation.class);
        }
        if (ann1 == null) {
            doAction();
            return;
        }

        invokeFilter(ann1, ann2);
    }

    private void invokeAction() throws HongsException {
        try {
            method.invoke(object, helper);
        } catch (IllegalAccessException ex) {
            throw new HongsException(0x1106, "Illegal access for "+object.getClass().getName()+"."+method.getName());
        } catch (IllegalArgumentException ex) {
            throw new HongsException(0x1106, "Illegal argument for "+object.getClass().getName()+"."+method.getName());
        } catch (InvocationTargetException ex) {
            throw new HongsException(0x1100, ex.getCause());
        }
    }

    private void invokeFilter(ActionAnnotation ann1, Annotation ann2) throws HongsException {
        Class  cls = ann1.value();
        Method mtd;

        try {
            mtd = cls.getMethod("invoke", new Class[] {
                ActionHelper.class, ActionChain.class, Annotation.class
            });
        } catch (NoSuchMethodException ex) {
            throw new HongsException(0x1102, "Can not find invoke method for "+cls.getName());
        } catch (SecurityException ex) {
            throw new HongsException(0x1102, "Can not exec invoke method for "+cls.getName());
        }

        try {
            mtd.invoke(null, helper, this, ann2);
        }
        catch (IllegalAccessException ex) {
            throw new HongsException(0x1104, "Illegal access for "+cls.getName()+"."+mtd.getName());
        } catch (IllegalArgumentException ex) {
            throw new HongsException(0x1104, "Illegal argument for "+cls.getName()+"."+mtd.getName());
        } catch (InvocationTargetException ex) {
            // 如果异常是动作方法内产生的
            // 则将这个异常直接向上层抛出
            Throwable ta = ex.getCause();
            if  (ta instanceof HongsException) {
                HongsException he = (HongsException) ta;
                if ( 0x1100 == he.getCode( ) ) {
                      ta = ta.getCause();
                }
            }

            throw new HongsException(0x1100, ta);
        }
    }

}
