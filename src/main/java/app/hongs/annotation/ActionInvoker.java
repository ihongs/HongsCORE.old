package app.hongs.annotation;

import app.hongs.action.ActionCaller;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import java.lang.annotation.Annotation;

/**
 * 动作注解执行器
 *
 * 包裹器必须指定执行器方可执行
 *
 * @author Hongs
 */
public interface ActionInvoker {
    public void invoke(ActionHelper helper, ActionCaller chains, Annotation anno) throws HongsException;
}
