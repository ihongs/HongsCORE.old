package app.hongs.action.annotation;

import app.hongs.action.ActionChains;
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
    public void invoke(ActionHelper helper, ActionChains chains, Annotation anno) throws HongsException;
}
