package app.hongs.action.annotation;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * <h1>动作注解链</h1>
 * <pre>
 * 异常代码区间: 0x1100~0x110f
 * 错误代码：
 * 0x1101 动作链索引溢出
 * 0x1103 找不到链的动作方法，不存在或遇安全问题
 * 0x1105 无法执行链动作方法，无法访问或参数错误
 * </pre>
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

    public void doAction() throws HongsException,
           IllegalAccessException,
           IllegalArgumentException,
           InvocationTargetException {
        // 如果超出链长度, 终止并记录日志
        if ( idx  >  annotations.length) {
            throw new HongsException(0x1101, "Action annotation out of index: "
            +idx+">"+annotations.length);
        }

        // 如果已到达链尾, 则执行动作函数
        if ( idx ==  annotations.length) {
            method.invoke(object,helper);
            return;
        }

        // 如果不是动作链, 则跳过注解检查
        ActionAnnotation anno  =  annotations[idx++]
             .annotationType()
             .getAnnotation (ActionAnnotation.class);
        if (anno == null) {
            doAction();
            return;
        }

        Class  cls  =  anno.value();
        Method mtd  =  null;

        try {
            mtd = anno.value().getMethod("invoke", new Class[] {ActionHelper.class, ActionChain.class, Annotation.class});
            mtd.invoke(null, helper, this, anno);
        } catch (NoSuchMethodException ex) {
            throw new HongsException(0x1103, "Can not find invoke method for "+cls.getName());
        } catch (SecurityException ex) {
            throw new HongsException(0x1103, "Can not exec invoke method for "+cls.getName());
        } catch (IllegalAccessException ex) {
            throw new HongsException(0x1105, "Illegal access for "+cls.getName()+"."+mtd.getName());
        } catch (IllegalArgumentException ex) {
            throw new HongsException(0x1105, "Illegal argument for "+cls.getName()+"."+mtd.getName());
        } catch (InvocationTargetException ex) {
            throw ex;
        }
    }
}
