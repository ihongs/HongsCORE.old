package app.hongs.db.serv;

import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotaion.Action;
import app.hongs.annotaion.Supply;
import app.hongs.annotaion.Verify;
import app.hongs.db.DB;
import app.hongs.db.Model;
import app.hongs.db.Mview;
import app.hongs.dl.IAction;
import static app.hongs.action.ActionCasual.ENTITY;
import static app.hongs.action.ActionCasual.MODULE;
import java.util.Collection;
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
        Map     req = getMyReq( helper, mod);
        Object  pkv = req.get ( mod.table.primaryKey);
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
        String  module = (String) helper.getAttribute(MODULE);
        String  entity = (String) helper.getAttribute(ENTITY);
        Model   mod = DB.getInstance(module).getModel(entity);
        Map     req = getMyReq(helper, mod);
        String  i   = mod.create(req);
        String  n   = ( String ) req.get( mod.findCols[0] );
        String  msg = getMyMsg(mod, module, "create", 1);
        helper.reply(msg, i, n);
    }

    @Action("update")
    @Verify()
    @CommitSuccess
    public void update(ActionHelper helper) throws HongsException {
        String  module = (String) helper.getAttribute(MODULE);
        String  entity = (String) helper.getAttribute(ENTITY);
        Model   mod = DB.getInstance(module).getModel(entity);
        Map     req = getMyReq(helper, mod);
        int     i  = mod.update(req);
        String  msg = getMyMsg(mod, module, "update", i);
        helper.reply(msg, i);
    }

    @Action("delete")
    @CommitSuccess
    public void delete(ActionHelper helper) throws HongsException {
        String  module = (String) helper.getAttribute(MODULE);
        String  entity = (String) helper.getAttribute(ENTITY);
        Model   mod = DB.getInstance(module).getModel(entity);
        Map     req = getMyReq(helper, mod);
        int     i   = mod.delete(req);
        String  msg = getMyMsg(mod, module, "delete", i);
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

    public String getMyMsg(Model mod, String module, String action, int num) throws HongsException {
        CoreLanguage lang = CoreLanguage.getInstance().clone();
                     lang.loadIgnrFNF(module);
        Mview        view = new Mview(mod   );
        return lang.translate("fore."+action+".success", view.getTitle(), Integer.toString(num));
    }

}
