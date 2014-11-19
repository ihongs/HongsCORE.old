package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;
import app.hongs.annotation.Action;
import app.hongs.annotation.ActionInvoker;
import app.hongs.annotation.ActionWrapper;
import app.hongs.util.ClassNames;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 动作注解链
 *
 * <h3>异常代码</h3>
 * <pre>
 * 区间: 0x1100~0x110f
 * Ex1102 无法执行动作方法，无法访问或参数错误
 * Ex1104 无法获取动作方法，无法访问或参数错误
 * Ex1106 动作不存在或无法访问
 * Ex1108 注解索引溢出
 * 0x1100 动作内异常，当动作内产生异常需要直接抛出时，都可以使用此代号
 * </pre>
 *
 * @author Hong
 */
public class ActionCaller {
    private int idx;
    private final Object object;
    private final Method method;
    private final ActionHelper helper;
    private final Annotation[] annotations;
    private static Map<String, Class> ACTIONS = null;

    static {
        CoreConfig conf = (CoreConfig) Core.GLOBAL_CORE.get(CoreConfig.class);
        String [ ] pkgs = conf.getProperty("core.action.packages").split(";");
        try {
            ACTIONS = getActions(pkgs);
        } catch (HongsException ex) {
            throw new Error(ex);
        }
    }

    public ActionCaller(Object object, Method method, ActionHelper helper) {
        this.annotations = method.getAnnotations();
        this.object = object;
        this.method = method;
        this.helper = helper;
        this.idx = 0;
    }

    public static ActionCaller getInstance(Object object, String method, ActionHelper helper) throws HongsException {
        Class  classo = getClass (object);
        Method metobj = getMethod(classo, method);
        return new  ActionCaller(object, metobj, helper);
    }

    public static ActionCaller getInstance(String action, String method, ActionHelper helper) throws HongsException {
        Class  classo = ActionCaller.getClass (action);
        Method metobj = getMethod(classo, method);
        Object object = Core.getInstance(classo);
        return new  ActionCaller(object, metobj, helper);
    }

    private static Map<String, Class> getActions(String... pkgs) throws HongsException {
        Map <String,Class> acts = new HashMap();
        for (String pkgn : pkgs) {
            Set<String> clss;
            try {
                clss = ClassNames.getClassNames(pkgn);
            } catch (IOException ex) {
                throw new HongsException( 0x1106 , "Can not load package '" + pkgn + "'.", ex);
            }
            if (clss == null) {
                throw new HongsException( 0x1106 , "Can not find package '" + pkgn + "'.");
            }
            for (String clsn : clss) {
                Class clso;
                try {
                    clso = Class.forName(clsn);
                } catch (ClassNotFoundException ex) {
                    throw new HongsException(0x1106, "Can not find class '" + clsn + "'.");
                }
                Action anno = (Action) clso.getAnnotation(Action.class);
                if (anno == null) {
                    continue;
                }
                String actn = anno.value();
                if (actn == null  ||  actn.length( ) == 0 ) {
                    actn = clso.getName().replace('.', '/');
                }
                acts.put(actn, clso);
            }
        }
        return  acts;
    }

    public static Map<String, Class> getActions() {
        return ACTIONS;
    }

    public void doAction() throws HongsException {
        // 如果超出链长度, 则终止执行
        if ( idx  >  annotations.length) {
            throw new HongsException(0x1108, "Action annotation out of index: "
            +idx+">"+annotations.length);
        }

        // 如果已到达链尾, 则执行动作
        if ( idx  == annotations.length) {
            doInvoke();
            return;
        }

        ActionWrapper actw;
        Annotation    anno = annotations[idx ++];
        if (anno instanceof ActionWrapper) {
            actw = ( ActionWrapper ) anno;
        } else {
            actw = anno.annotationType().getAnnotation(ActionWrapper.class);
        }

        // 如果不是动作链, 则跳过注解
        if (actw == null) {
            doAction();
            return;
        }

        // 执行注解过滤器
        Class<? extends ActionInvoker> classo = actw.value();
        ActionInvoker filter = (ActionInvoker)
                      Core.getInstance(classo);
        filter.invoke(helper, this, anno);
    }

    public void doInvoke() throws HongsException {
        try {
            method.invoke(object, helper);
        } catch (   IllegalAccessException e) {
            throw new HongsException(0x1102, "Illegal access for method '"+object.getClass().getName()+"."+method.getName()+"(ActionHelper).");
        } catch ( IllegalArgumentException e) {
            throw new HongsException(0x1102, "Illegal params for method '"+object.getClass().getName()+"."+method.getName()+"(ActionHelper).");
        } catch (InvocationTargetException e) {
            throw new HongsException(0x1100, e.getCause());
        }
    }

    private static Method getMethod(Class classo, String method) throws HongsException {
        try {
            return classo.getMethod("action_" + method, new Class[]{ ActionHelper.class });
        } catch (NoSuchMethodException e) {
            throw new HongsException(0x1104, "Can not find method '"+classo.getName()+".action_"+method+"(ActionHelper)'.");
        } catch (    SecurityException e) {
            throw new HongsException(0x1104, "Can not call method '"+classo.getName()+".action_"+method+"(ActionHelper)'.");
        }
    }

    private static Class getClass(String action) throws HongsException {
        Class  classo = getActions().get(action);
        if  (  classo == null  ) {
            throw new HongsException(0x1106, "Can not find class for action '"+action+"'.");
        }
        return classo;
    }

    private static Class getClass(Object object) {
        return object.getClass();
    }

}
