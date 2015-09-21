package com.huiyiabc;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.Action;
import app.hongs.db.DB;
import app.hongs.db.DBAction;
import app.hongs.db.Model;

/**
 *
 * @author Hongs
 */
@Action("huiyiabc/auto")
public class AutoAction extends DBAction {
    
    /**
     * 获取模型对象
     * 注意:
     *  对象 Action 注解的命名必须为 "模型路径/实体名称"
     *  方法 Action 注解的命名只能是 "动作名称", 不得含子级实体名称
     * @param helper
     * @return
     * @throws HongsException 
     */
    @Override
    protected  Model getModel(ActionHelper helper)
    throws HongsException {
        ActionRunner runner = (ActionRunner) helper.getAttribute("__RUNNER__");
        String mod = runner.getAction();
        String ent ;
        int    pos ;
        pos  = mod.lastIndexOf('/' );
        mod  = mod.substring(0, pos); // 去掉动作
        pos  = mod.lastIndexOf('/' );
        ent  = mod.substring(1+ pos); // 实体名称 
        mod  = mod.substring(0, pos); // 模型名称
        return DB.getInstance("huiyiabc/" + mod).getModel(ent);
    }

}
