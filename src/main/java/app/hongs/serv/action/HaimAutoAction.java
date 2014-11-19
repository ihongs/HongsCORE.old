package app.hongs.serv.action;

import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.db.AbstractBaseModel;
import java.util.Map;

/**
 * 自动动作处理
 * @author Hongs
 */
@Action("haim/auto")
public class HaimAutoAction {
    public void action_list(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute("conf").toString();
        String name = helper.getRequest().getAttribute("name").toString();
        AbstractBaseModel model = getModel(conf, name);
        Map data = model.getPage(helper.getRequestData());
        helper.reply(data);
    }

    public void action_info(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute("conf").toString();
        String name = helper.getRequest().getAttribute("name").toString();
        AbstractBaseModel model = getModel(conf, name);
        Map data = model.getPage(helper.getRequestData());
        helper.reply(data);
    }

    public void action_create(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute("conf").toString();
        String name = helper.getRequest().getAttribute("name").toString();
        AbstractBaseModel model = getModel(conf, name);
        String id = model.create(helper.getRequestData());
        String nms = model.getAffectedNames();
        String msg = getMsg(conf, name, "create", nms);
        helper.reply(msg, id, nms);
    }

    public void action_modify(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute("conf").toString();
        String name = helper.getRequest().getAttribute("name").toString();
        AbstractBaseModel model = getModel(conf, name);
        String id = model.modify(helper.getRequestData());
        String nms = model.getAffectedNames();
        String msg = getMsg(conf, name, "modify", nms);
        helper.reply(msg, id, nms);
    }

    public void action_remove(ActionHelper helper) throws HongsException {
        String conf = helper.getRequest().getAttribute("conf").toString();
        String name = helper.getRequest().getAttribute("name").toString();
        AbstractBaseModel model = getModel(conf, name);
        model.remove(helper.getRequestData());
        String nms = model.getAffectedNames();
        String msg = getMsg(conf, name, "remove", nms);
        helper.reply(msg);
    }

    private AbstractBaseModel _model;
    private CoreLanguage _lang;

    protected AbstractBaseModel getModel(String conf, String name) throws HongsException {
        if (_model == null) {
            _model = new app.hongs.serv.HaimCommon(conf, name);
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
