package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsError;
import app.hongs.HongsException;
import static app.hongs.action.ActionWarder.PATH;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Filter;
import app.hongs.action.anno.FilterInvoker;
import app.hongs.util.CsNs;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 动作执行器
 *
 * <h3>异常代码</h3>
 * <pre>
 * 区间: 0x10f0~0x10ff
 * 0x10f0 注解链溢出
 * 0x10f1 尚未登陆
 * 0x10f3 无权访问
 * 0x10f4 无此动作
 * 0x10f5 无法执行, 禁止访问或参数错误
 * </pre>
 *
 * @author Hong
 */
public class ActionRunner {
    private int idx = 0;
    private String action;
    private final Object object;
    private final Method method;
    private final ActionHelper helper;
    private final Annotation[] annarr;

    public ActionRunner(String action, ActionHelper helper) throws HongsException {
        this.method = getActions().get(action);
        if ( method == null ) {
            throw new HongsException(0x10f4, "Can not find action '"+action+"'");
        }
        this.object = Core.getInstance(method.getDeclaringClass());
        this.annarr = method.getAnnotations( );
        this.helper = helper;
        this.action = action;
        helper.setAttribute("__RUNNER__",this);
    }

    public Object getObject() {
        return object;
    }

    public String getMtdAnn() {
        return method.getAnnotation(Action.class).value();
    }

    public String getClsAnn() {
        return object.getClass().getAnnotation(Action.class).value();
    }

    /**
     * 获取动作名
     * 如果有指定工作路径(ActionWarder.PATH)则返回工作动作名
     * @return
     * @throws HongsException
     */
    public String getAction() throws HongsException {
        String x = (String) helper.getAttribute(PATH);
        if (x != null) { // 去除根和扩展名
            return x.substring(1, x.lastIndexOf('.'));
        } else {
            return action;
        }
    }

    public void doAction() throws HongsException {
        // 如果超出链长度, 则终止执行
        if ( idx  >  annarr.length) {
            throw new HongsException(0x10f0, "Action annotation out of index: "
            +idx+">"+annarr.length);
        }

        // 如果已到达链尾, 则执行动作
        if ( idx  == annarr.length) {
            doInvoke();
            return;
        }

        Filter actw;
        Annotation    anno = annarr[idx ++];
        if (anno instanceof Filter) {
            actw = ( Filter ) anno;
        } else {
            actw = anno.annotationType().getAnnotation(Filter.class);
        }

        // 如果不是动作链, 则跳过注解
        if (actw == null) {
            doAction();
            return;
        }

        // 执行注解过滤器
        Class<? extends FilterInvoker> classo = actw.value();
        FilterInvoker filter = Core.getInstance(classo);
        filter.invoke(helper , this, anno);
    }

    public void doInvoke() throws HongsException {
        try {
            method.invoke(object, helper);
        } catch (   IllegalAccessException e) {
            throw new HongsException(0x10f5, "Illegal access for method '"+object.getClass().getName()+"."+method.getName()+"(ActionHelper).");
        } catch ( IllegalArgumentException e) {
            throw new HongsException(0x10f5, "Illegal params for method '"+object.getClass().getName()+"."+method.getName()+"(ActionHelper).");
        } catch (InvocationTargetException e) {
            Throwable ex = e.getCause();
            if (ex instanceof HongsException) {
                throw (HongsException) ex;
            } else
            if (ex instanceof HongsError) {
                throw (HongsError) ex;
            } else {
                throw new HongsException(0x10f5, ex);
            }
        }
    }

    private static final ReadWriteLock ACTLOCK = new ReentrantReadWriteLock();
    private static Map<String, Method> ACTIONS = null;

    public  static Map<String, Method> getActions() {
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
            String[] pkgs = CoreConfig.getInstance("_begin_").getProperty("core.load.serv").split(";");
            ACTIONS = getActions( pkgs );
            return ACTIONS;
        } finally {
            wlock.unlock();
        }
    }

    private static Map<String, Method> getActions(String... pkgs) {
        Map<String, Method> acts = new HashMap();

        for(String pkgn : pkgs) {
            pkgn = pkgn.trim( );
            if (pkgn.length ( ) == 0) continue;
            Set< String > clss ;

            if (pkgn.endsWith(".*")) {
                pkgn = pkgn.substring(0, pkgn.length() -2);
                try {
                    clss = CsNs.getClassNames(pkgn, false);
                } catch (IOException ex) {
                    throw new HongsError( 0x4a , "Can not load package '" + pkgn + "'.", ex);
                }
                if (clss == null) {
                    throw new HongsError( 0x4a , "Can not find package '" + pkgn + "'.");
                }
            } else {
                clss = new HashSet();
                clss.add(pkgn);
            }

            for(String clsn : clss) {
                Class  clso;
                try {
                    clso = Class.forName(clsn);
                } catch (ClassNotFoundException ex) {
                    throw new HongsError(0x4a, "Can not find class '" + clsn + "'.");
                }

                // 从注解提取动作名
                Action anno = (Action) clso.getAnnotation(Action.class);
                if (anno == null) {
                    continue;
                }
                String actn = anno.value();
                if (actn == null || actn.length() == 0) {
                    actn =  clsn.replace('.','/');
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
                        actx =  mtdn;
                    }

                    // 检查方法是否合法
                    Class[] prms = mtdo.getParameterTypes();
                    if (prms == null || prms.length != 1 || !prms[0].isAssignableFrom(ActionHelper.class)) {
                        throw new HongsError(0x4a, "Can not find action method '"+clsn+"."+mtdn+"(ActionHelper)'.");
                    }

                    if ("__main__".equals(actx)) {
                        acts.put(actn /*__main__*/ , mtdo );
                    } else {
                        acts.put(actn + "/" + actx , mtdo );
                    }
                }
            }
        }

        return acts;
    }

}
