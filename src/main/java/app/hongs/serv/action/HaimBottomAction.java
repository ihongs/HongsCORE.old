package app.hongs.serv.action;

import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.annotation.CommitSuccess;
import app.hongs.db.AbstractBaseModel;
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
        AbstractBaseModel model = getModel(conf, name);
        Map rst = model.getPage(helper.getRequestData());
        helper.reply(rst);
    }

    @Action("info")
    public void getInfo(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute(CONFIG).toString();
        String name = helper.getRequest().getAttribute(ENTITY).toString();
        AbstractBaseModel model = getModel(conf, name);
        Map rst = model.getPage(helper.getRequestData());
        helper.reply(rst);
    }

    @Action("create")
    @CommitSuccess
    public void doCreate(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute(CONFIG).toString();
        String name = helper.getRequest().getAttribute(ENTITY).toString();
        AbstractBaseModel model = getModel(conf, name);
        String id  = model.create(helper.getRequestData());
        String nms = model.getAffectedNames();
        String msg = getMsg(conf, name, "create", nms);
        helper.reply(msg, id, nms);
    }

    @Action("update")
    @CommitSuccess
    public void doUpdate(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute(CONFIG).toString();
        String name = helper.getRequest().getAttribute(ENTITY).toString();
        AbstractBaseModel model = getModel(conf, name);
        model.update(helper.getRequestData());
        String nms = model.getAffectedNames();
        String msg = getMsg(conf, name, "update", nms);
        helper.reply(msg);
    }
    
    @Action("remove")
    @CommitSuccess
    public void doRemove(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute("conf").toString();
        String name = helper.getRequest().getAttribute("name").toString();
        AbstractBaseModel model = getModel(conf, name);
        Map    req = helper.getRequestData (   );
        String nms = model.getOperableNames(req);
        model.remove(req);
        String msg = getMsg(conf, name, "remove", nms);
        helper.reply(msg);
    }

    private AbstractBaseModel _model;
    private CoreLanguage _lang;

    protected AbstractBaseModel getModel(String conf, String name) throws HongsException {
        if (_model == null) {
            _model = new HaimBottom(conf, name);
        }
        return _model;
    }

    protected String getMsg(String conf, String name, String key, String nms) {
        if (_lang == null) {
            _lang = CoreLanguage.getInstance();
            _lang.load(conf);
        }
        return _lang.translate("core."+key+".success", name, nms);
    }
}
