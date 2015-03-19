package app.hongs.serv.simple;

import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Supply;
import app.hongs.action.anno.Verify;
import app.hongs.db.DB;
import app.hongs.db.Model;
import app.hongs.db.Mview;
import app.hongs.dl.anno.CommitSuccess;
import app.hongs.dl.IAction;
import java.util.Collection;
import java.util.Map;

/**
 * 自动动作处理
 * @author Hongs
 */
@Action("hongs/auto/db")
public class AutoAction
implements IAction {

    @Action("retrieve")
    @Supply()
    public void retrieve(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getMyReq(helper, mod);
        Object  pkv = req.get (mod.table.primaryKey);
        Map     rst;
        if (pkv == null || pkv instanceof Collection) {
            rst  = mod.getList(req);
        } else {
            rst  = mod.getInfo(req);
        }
        helper.reply(rst);
    }

    @Action("create")
    @Verify()
    @CommitSuccess
    public void create(ActionHelper helper) throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getMyReq(helper, mod);
        String  i   = mod.create(req);
        String  n   = (String) req.get(mod.findCols[0]);
        String  msg = getMyMsg(mod, "create", 1);
        helper.reply(msg, i, n);
    }

    @Action("update")
    @Verify()
    @CommitSuccess
    public void update(ActionHelper helper) throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getMyReq(helper, mod);
        int     i   = mod.update(req);
        String  msg = getMyMsg(mod, "update", i);
        helper.reply(msg, i);
    }

    @Action("delete")
    @CommitSuccess
    public void delete(ActionHelper helper) throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getMyReq(helper, mod);
        int     i   = mod.delete(req);
        String  msg = getMyMsg(mod, "delete", i);
        helper.reply(msg, i);
    }

    @Action("exists")
    public void isExists(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getMyReq(helper, mod);
        boolean rst = mod.exists(req);
        helper.reply(rst);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        Model   mod = getModel(helper);
        Map     req = getMyReq(helper, mod);
        boolean rst = mod.unique(req);
        helper.reply(rst);
    }

    public Model getModel(ActionHelper helper)
    throws HongsException {
        String p = (String) helper.getAttribute(app.hongs.action.ActionDriver.PATH);
        int i;
        i = p.lastIndexOf('/'); // 去掉动作名
        p = p.substring  (1,i);
        i = p.lastIndexOf('/'); // 拆分配置和表单
        return DB.getInstance(p.substring(0,i)).getModel(p.substring(i+1));
    }
    
    public Map getMyReq(ActionHelper helper, Model mod)
    throws HongsException {
        Map req = helper.getRequestData();
        if (req.containsKey("id")) {
            req.put(mod.table.primaryKey, req.remove("id"));
        }
        return req;
    }

    public String getMyMsg(Model mod, String opr, int num) throws HongsException {
        CoreLocale lang = CoreLocale.getInstance().clone();
                   lang.loadIgnrFNF(mod.db.name);
        Mview      view = new Mview(mod);
        return lang.translate("fore."+opr+".success", view.getTitle(), Integer.toString(num));
    }

}
