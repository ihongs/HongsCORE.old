package app.hongs.action;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.ActionInvoker;
import app.hongs.action.annotation.ActionWrapper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class ActionChains {
    private int idx;
    private Method method;
    private Object object;
    private ActionHelper helper;
    private Annotation[] annotations;

    public ActionChains(Method method, Object object, ActionHelper helper) {
        this.annotations = method.getAnnotations();
        this.method = method;
        this.object = object;
        this.helper = helper;
        this.idx = 0;
    }

    public void doAction() throws HongsException {
        // 如果超出链长度, 则终止执行
        if ( idx  >  annotations.length) {
            throw new HongsException(0x1108, "Action annotation out of index: "
            +idx+">"+annotations.length);
        }

        // 如果已到达链尾, 则执行动作
        if ( idx ==  annotations.length) {
            doInvoke();
            return;
        }

        ActionWrapper ann1;
        Annotation  ann2 = annotations[idx ++];
        if (ann2 instanceof ActionWrapper) {
            ann1 = ( ActionWrapper ) ann2;
        } else {
            ann1 = ann2.annotationType().getAnnotation(ActionWrapper.class);
        }

        // 如果不是动作链, 则跳过注解
        if (ann1 == null) {
            doAction();
            return;
        }

        doFilter(ann1 , ann2);
    }

    private void doInvoke() throws HongsException {
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

    private void doFilter(ActionWrapper ann1, Annotation ann2) throws HongsException {
        Class<? extends ActionInvoker> cls = ann1.value();
        ActionInvoker obj;
        
        try {
            obj = cls.newInstance();
        } catch (InstantiationException ex) {
            throw new HongsException(0x1102, "Can not get instance for "+cls.getName());
        } catch (IllegalAccessException ex) {
            throw new HongsException(0x1102, "Can not get instance for "+cls.getName());
        }
        
        obj.invoke(helper, this, ann2);
    }

}
