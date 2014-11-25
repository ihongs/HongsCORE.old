package app.hongs.serv.action;

import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.annotation.CommitSuccess;
import app.hongs.db.Model4Crud;
import app.hongs.serv.HaimBottom;
import static app.hongs.serv.action.HaimAccessFilter.CONFIG;
import static app.hongs.serv.action.HaimAccessFilter.ENTITY;
import java.util.Map;

/**
 * 自动动作处理
 * @author Hongs
 */
@Action("haim/bottom")
public class HaimBottomAction {
    @Action("list")
    public void getList(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute(CONFIG).toString();
        String name = helper.getRequest().getAttribute(ENTITY).toString();
        Model4Crud model = getModel(conf, name);
        Map     req = helper.getRequestData();
        Map     rst = model.getPage(req);
        helper.reply(rst);
    }

    @Action("info")
    public void getInfo(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute(CONFIG).toString();
        String name = helper.getRequest().getAttribute(ENTITY).toString();
        Model4Crud model = getModel(conf, name);
        Map     req = helper.getRequestData();
        Map     rst = model.getPage(req);
        helper.reply(rst);
    }

    @Action("create")
    @CommitSuccess
    public void doCreate(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute(CONFIG).toString();
        String name = helper.getRequest().getAttribute(ENTITY).toString();
        Model4Crud model = getModel(conf, name);
        Map     req = helper.getRequestData();
        String  id  = model.create(req);
        String  msg = getMsg(conf, name, "create", 1 );
        helper.reply(msg, id);
    }

    @Action("update")
    @CommitSuccess
    public void doUpdate(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute(CONFIG).toString();
        String name = helper.getRequest().getAttribute(ENTITY).toString();
        Model4Crud model = getModel(conf, name);
        Map     req = helper.getRequestData();
        int     rd  = model.update(req);
        String  msg = getMsg(conf, name, "update", rd);
        helper.reply(msg, rd);
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute("conf").toString();
        String name = helper.getRequest().getAttribute("name").toString();
        Model4Crud model = getModel(conf, name);
        Map     req = helper.getRequestData();
        int     rd  = model.delete(req);
        String  msg = getMsg(conf, name, "delete", rd);
        helper.reply(msg, rd);
    }

    @Action("exists")
    public void isExists(ActionHelper helper)
    throws HongsException {
        String conf = helper.getRequest().getAttribute("conf").toString();
        String name = helper.getRequest().getAttribute("name").toString();
        Model4Crud model = getModel(conf, name);
        boolean rst = model.exists(helper.getRequestData());
        helper.reply(rst);
    }

    private Model4Crud _model;
    private CoreLanguage _lang;

    protected Model4Crud getModel(String conf, String name) throws HongsException {
        if (_model == null) {
            _model = new HaimBottom(conf, name);
        }
        return _model;
    }

    protected String getMsg(String conf, String name, String key, int num) {
        if (_lang == null) {
            _lang = CoreLanguage.getInstance();
            _lang.load(conf);
        }
        return _lang.translate("core."+key+".success", name, Integer.toString(num));
    }

}
