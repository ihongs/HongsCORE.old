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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 动作注解链
 *
 * <h3>异常代码</h3>
 * <pre>
 * 区间: 0x1100~0x110f
 * 0x1100 动作内异常
 * 0x1102 找不到动作
 * 0x1104 注解链溢出
 * 0x1106 无法执行动作方法，无法访问或参数错误
 * 0x1108 无法获取包、类或方法
 * </pre>
 *
 * @author Hong
 */
public class ActionRunner {
    private int idx = 0;
    private final Object object;
    private final Method method;
    private final ActionHelper helper;
    private final Annotation[] annarr;

    public ActionRunner(String action, ActionHelper helper) throws HongsException {
        method = getActions().get(action);
        if ( method == null ) {
            throw new HongsException(0x1102, "Can not find action '"+action+"'");
        }
        this.object = Core.getInstance(method.getDeclaringClass());
        this.annarr = method.getAnnotations();
        this.helper = helper;
    }

    public void doAction() throws HongsException {
        // 如果超出链长度, 则终止执行
        if ( idx  >  annarr.length) {
            throw new HongsException(0x1104, "Action annotation out of index: "
            +idx+">"+annarr.length);
        }

        // 如果已到达链尾, 则执行动作
        if ( idx  == annarr.length) {
            doInvoke();
            return;
        }

        ActionWrapper actw;
        Annotation    anno = annarr[idx ++];
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
            throw new HongsException(0x1106, "Illegal access for method '"+object.getClass().getName()+"."+method.getName()+"(ActionHelper).");
        } catch ( IllegalArgumentException e) {
            throw new HongsException(0x1106, "Illegal params for method '"+object.getClass().getName()+"."+method.getName()+"(ActionHelper).");
        } catch (InvocationTargetException e) {
            throw new HongsException(0x1100, e.getCause());
        }
    }

    private static final ReadWriteLock ACTLOCK = new ReentrantReadWriteLock();
    private static Map<String, Method> ACTIONS = null;

    public  static Map<String, Method> getActions() throws HongsException {
        Lock rlock = ACTLOCK. readLock();
        rlock.lock();
        try {
            if (ACTIONS != null) {
                return  ACTIONS;
            }
        } finally {
            rlock.unlock();
        }

        Lock wlock = ACTLOCK.writeLock();
        wlock.lock();
        try {
            CoreConfig conf = (CoreConfig) Core.GLOBAL_CORE.get(CoreConfig.class);
            String [ ] pkgs = conf.getProperty("core.action.packages").split(";");
            ACTIONS = getActions( pkgs );
            return ACTIONS;
        } finally {
            wlock.unlock();
        }
    }

    private static Map<String, Method> getActions(String... pkgs) throws HongsException {
        Map<String, Method> acts = new HashMap();

        for(String pkgn : pkgs) {
            Set< String > clss;
            try {
                clss = ClassNames.getClassNames(pkgn);
            } catch (IOException ex) {
                throw new HongsException( 0x1108 , "Can not load package '" + pkgn + "'.", ex);
            }
            if (clss == null) {
                throw new HongsException( 0x1108 , "Can not find package '" + pkgn + "'.");
            }

            for(String clsn : clss) {
                Class  clso;
                try {
                    clso = Class.forName(clsn);
                } catch (ClassNotFoundException ex) {
                    throw new HongsException(0x1108, "Can not find class '" + clsn + "'.");
                }

                // 从注解提取动作名
                Action anno = (Action) clso.getAnnotation(Action.class);
                if (anno == null) {
                    continue;
                }
                String actn = anno.value();
                if (actn == null || actn.length() == 0) {
                    actn = clsn.replace('.', '/');
                }

                Method[] mtds = clso.getMethods();
                for(Method mtdo : mtds) {
                    String mtdn = mtdo.getName( );

                    // 从注解提取动作名
                    Action annx = (Action) mtdo.getAnnotation(Action.class);
                    if (annx == null) {
                        continue;
                    }
                    String actx = annx.value();
                    if (actx == null || actx.length() == 0) {
                        actx = mtdn;
                    }

                    // 检查方法是否合法
                    Class[] prms = mtdo.getParameterTypes();
                    if (prms == null || prms.length   != 1 || !prms[0].isAssignableFrom(ActionHelper.class)) {
                        throw new HongsException(0x1108, "Can not find action method '"+clsn+"."+mtdn+"(ActionHelper)'.");
                    }

                    acts.put(actn + "/" + actx, mtdo);
                }
            }
        }

        return acts;
    }

}
