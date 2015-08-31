package app.hongs.db;

import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Supply;
import app.hongs.action.anno.Verify;
import app.hongs.action.anno.CommitSuccess;
import app.hongs.dl.IAction;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础数据动作
 * @author Hongs
 */
@Action("hongs/db")
public class DBAction
implements IAction {

    @Action("retrieve")
    @Supply()
    @Override
    public void retrieve(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getRqMap(helper, mod,"retrieve");
        Map     rsp = mod.retrieve(req);
                rsp = getRtMap(helper, mod, rsp);
        helper.reply(rsp);
    }

    @Action("create")
    @Verify()
    @CommitSuccess
    @Override
    public void create(ActionHelper helper) throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getRqMap(helper, mod, "create");
        Map       m = mod.create(req);
        String  msg = getRpMsg(helper, mod, "create", 1);
        helper.reply(msg, m);
    }

    @Action("update")
    @Verify()
    @CommitSuccess
    @Override
    public void update(ActionHelper helper) throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getRqMap(helper, mod, "update");
        int       n = mod.update(req);
        String  msg = getRpMsg(helper, mod, "update", n);
        helper.reply(msg, n);
    }

    @Action("delete")
    @CommitSuccess
    @Override
    public void delete(ActionHelper helper) throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getRqMap(helper, mod, "delete");
        int       n = mod.delete(req);
        String  msg = getRpMsg(helper, mod, "delete", n);
        helper.reply(msg, n);
    }

    @Action("exists")
    public void isExists(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getRqMap(helper, mod, "exists");
        boolean   v = mod.exists(req);
        Map     rst = new HashMap();
        rst.put("valid" , v);
        helper.reply(rst);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getRqMap(helper, mod, "unique");
        boolean   v = mod.unique(req);
        Map     rst = new HashMap();
        rst.put("valid" , v);
        helper.reply(rst);
    }

    /**
     * 获取模型对象
     * @param helper
     * @return
     * @throws HongsException 
     */
    protected  Model getModel(ActionHelper helper)
    throws HongsException {
        String p = (String) helper.getAttribute(app.hongs.action.ActionDriver.PATH);
        int i;
        i = p.lastIndexOf('/'); // 去掉当前动作名
        p = p.substring  (1,i);
        i = p.lastIndexOf('/'); // 拆分配置和表单
        return DB.getInstance(p.substring(0,i)).getModel(p.substring(i+1));
    }

    /**
     * 整理返回数据
     * @param helper
     * @param mod
     * @param rsp
     * @return
     * @throws HongsException 
     */
    protected  Map   getRtMap(ActionHelper helper, Model mod,  Map   rsp)
    throws HongsException {
        return rsp;
    }

    /**
     * 获取请求数据
     * @param helper
     * @param mod
     * @param opr
     * @return
     * @throws HongsException 
     */
    protected  Map   getRqMap(ActionHelper helper, Model mod, String opr)
    throws HongsException {
        Map req = helper.getRequestData();
        if (req.containsKey("id")) {
            req.put(mod.table.primaryKey, req.remove("id"));
        }
        return req;
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
                   lang.loadIgnrFNF(mod.db.name);
        Mview      view = new Mview(mod);
        return lang.translate("fore."+opr+".success", view.getTitle(), Integer.toString(num));
    }

}
