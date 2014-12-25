package app.hongs.db.serv;

import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.annotation.CommitSuccess;
import app.hongs.annotation.Supply;
import app.hongs.annotation.Verify;
import app.hongs.db.DB;
import app.hongs.db.Model;
import app.hongs.dl.IAction;
import static app.hongs.action.CowlFilter.ENTITY;
import static app.hongs.action.CowlFilter.MODULE;
import java.util.Map;

/**
 * 自动动作处理
 * @author Hongs
 */
@Action("haim/db")
public class HaimAction
implements IAction {

    @Action("retrieve")
    @Supply()
    public void retrieve(ActionHelper helper)
    throws HongsException {
        String  module = (String) helper.getAttribute(MODULE);
        String  entity = (String) helper.getAttribute(ENTITY);
        Model   mod = DB.getInstance(module).getModel(entity);
        Map     req = getMyReq(helper, mod);
        Map     rst;
        if (req.containsKey(mod.table.primaryKey)) {
            rst = mod.getList(req);
        } else {
            rst = mod.getList(req);
        }
        helper.reply(rst);
    }

    @Action("create")
    @Verify()
    @CommitSuccess
    public void doCreate(ActionHelper helper) throws HongsException {
        String  module = (String) helper.getAttribute(MODULE);
        String  entity = (String) helper.getAttribute(ENTITY);
        Model   mod = DB.getInstance(module).getModel(entity);
        Map     req = getMyReq(helper, mod);
        String  i   = mod.create(req);
        String  n   = ( String ) req.get( mod.findCols[0] );
        String  msg = getMyMsg(module, entity, "create", 1);
        helper.reply(msg, i, n);
    }

    @Action("update")
    @Verify()
    @CommitSuccess
    public void doUpdate(ActionHelper helper) throws HongsException {
        String  module = (String) helper.getAttribute(MODULE);
        String  entity = (String) helper.getAttribute(ENTITY);
        Model   mod = DB.getInstance(module).getModel(entity);
        Map     req = getMyReq(helper, mod);
        int     i  = mod.update(req);
        String  msg = getMyMsg(module, entity, "update", i);
        helper.reply(msg, i);
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper) throws HongsException {
        String  module = (String) helper.getAttribute(MODULE);
        String  entity = (String) helper.getAttribute(ENTITY);
        Model   mod = DB.getInstance(module).getModel(entity);
        Map     req = getMyReq(helper, mod);
        int     i   = mod.delete(req);
        String  msg = getMyMsg(module, entity, "delete", i);
        helper.reply(msg, i);
    }

    @Action("exists")
    public void isExists(ActionHelper helper)
    throws HongsException {
        String  module = (String) helper.getAttribute(MODULE);
        String  entity = (String) helper.getAttribute(ENTITY);
        Model   mod = DB.getInstance(module).getModel(entity);
        Map     req = getMyReq(helper, mod);
        boolean rst = mod.exists(req);
        helper.reply(null, rst);
    }

    @Action("unique")
    public void isUnique(ActionHelper helper)
    throws HongsException {
        String  module = (String) helper.getAttribute(MODULE);
        String  entity = (String) helper.getAttribute(ENTITY);
        Model   mod = DB.getInstance(module).getModel(entity);
        Map     req = getMyReq(helper, mod);
        boolean rst = mod.unique(req);
        helper.reply(null, rst);
    }

    public Map getMyReq(ActionHelper helper, Model mod)
    throws HongsException {
        Map req = helper.getRequestData();
        if (req.containsKey("id")) {
            req.put(mod.table.primaryKey, req.remove("id"));
        }
        return req;
    }

    public String getMyMsg(String module, String entity, String action, int num) {
        CoreLanguage lang = CoreLanguage.getInstance().clone( ); lang.load(module);
        String n = (entity.length()==0?module:module+"."+entity).replace("/", ".");
               n = lang.containsKey(n) ? lang.translate(n) : "";
        return lang.translate("fore."+action+".success", n, Integer.toString(num));
    }

}
