package app.hongs.db;

import app.hongs.Cnst;
import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Supply;
import app.hongs.action.anno.Verify;
import app.hongs.action.anno.CommitSuccess;
import app.hongs.dl.IAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基础数据动作
 * @author Hongs
 */
@Action("hongs/db")
public class DBAction
implements IAction {

    @Action("retrieve")
    @Supply(conf="", form="")
    @Override
    public void retrieve(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = helper.getRequestData();
                req = getRqMap(helper, mod, "retrieve", req);
        Map     rsp = mod.retrieve(req);
                rsp = getRpMap(helper, mod, "retrieve", rsp);
        helper.reply(rsp);
    }

    @Action("create")
    @Verify(conf="", form="")
    @CommitSuccess
    @Override
    public void create(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = helper.getRequestData();
                req = getRqMap(helper, mod, "create", req);
        Map     rsp = mod.create(req);
                rsp = getRpMap(helper, mod, "create", rsp);
        String  msg = getRpMsg(helper, mod, "create", 1  );
        helper.reply(msg, rsp);
    }

    @Action("update")
    @Verify(conf="", form="")
    @CommitSuccess
    @Override
    public void update(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = helper.getRequestData();
                req = getRqMap(helper, mod, "update", req);
        int     num = mod.update(req);
        String  msg = getRpMsg(helper, mod, "update", num);
        helper.reply(msg, num);
    }

    @Action("delete")
    @CommitSuccess
    @Override
    public void delete(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = helper.getRequestData();
                req = getRqMap(helper, mod, "delete", req);
        int     num = mod.delete(req);
        String  msg = getRpMsg(helper, mod, "delete", num);
        helper.reply(msg, num);
    }

    @Action("exists")
    public void isExists(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = helper.getRequestData();
                req = getRqMap(helper, mod, "exists", req);
        boolean val = mod.exists(req);
        helper.reply(null, val);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = helper.getRequestData();
                req = getRqMap(helper, mod, "unique", req);
        boolean val = mod.unique(req);
        helper.reply(null, val);
    }

    /**
     * 获取模型对象
     * 注意:
     *  对象 Action 注解的命名必须为 "模型路径/实体名称"
     *  方法 Action 注解的命名只能是 "动作名称", 不得含子级实体名称
     * @param helper
     * @return
     * @throws HongsException
     */
    protected  Model getModel(ActionHelper helper)
    throws HongsException {
        ActionRunner runner = (ActionRunner) helper.getAttribute(Cnst.RUNNER_ATTR);
        String mod = runner.getAction();
        String ent ;
        int    pos ;
        pos  = mod.lastIndexOf('/' );
        mod  = mod.substring(0, pos); // 去掉动作
        pos  = mod.lastIndexOf('/' );
        ent  = mod.substring(1+ pos); // 实体名称
        mod  = mod.substring(0, pos); // 模型名称
        Model  mol = DB.getInstance(mod).getModel(ent);
        new Mview(mol).getFields(   );
        return mol ;
    }

    /**
     * 获取请求数据
     * @param helper
     * @param mod
     * @param opr
     * @param req
     * @return
     * @throws HongsException
     */
    protected  Map   getRqMap(ActionHelper helper, Model mod, String opr, Map req)
    throws HongsException {
        if (!Cnst.ID_KEY.equals(mod.table.primaryKey)) {
            if (req.containsKey(Cnst.ID_KEY)) {
                req.put(mod.table.primaryKey, req.get(Cnst.ID_KEY));
            }
        }
        return req;
    }

    /**
     * 整理返回数据
     * @param helper
     * @param mod
     * @param opr
     * @param rsp
     * @return
     * @throws HongsException
     */
    protected  Map   getRpMap(ActionHelper helper, Model mod, String opr, Map rsp)
    throws HongsException {
        if (!Cnst.ID_KEY.equals(mod.table.primaryKey)) {
            if (rsp.containsKey("info")) {
                /**/ Map  info = (Map ) rsp.get("info");
                info.put(Cnst.ID_KEY , info.get(mod.table.primaryKey));
            }
            if (rsp.containsKey("list")) {
                List<Map> list = (List) rsp.get("list");
            for(/**/ Map  info :  list ) {
                info.put(Cnst.ID_KEY , info.get(mod.table.primaryKey));
            }
            }
        }
        return rsp;
    }

    /**
     * 获取返回消息
     * @param helper
     * @param mod
     * @param opr
     * @param num
     * @return
     * @throws HongsException
     */
    protected String getRpMsg(ActionHelper helper, Model mod, String opr, int num)
    throws HongsException {
        CoreLocale lang = CoreLocale.getInstance().clone();
                   lang.loadIgnrFNF( mod.db.name );
        Mview  view = new Mview(mod);
        return lang.translate("fore."+opr+".success", view.getTitle(), Integer.toString(num));
    }

}
