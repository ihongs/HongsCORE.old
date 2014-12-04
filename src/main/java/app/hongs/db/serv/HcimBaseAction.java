package app.hongs.db.serv;

import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.annotation.CommitSuccess;
import app.hongs.db.DB;
import app.hongs.db.Model;
import static app.hongs.serv.RiggerFilter.CONFIG;
import static app.hongs.serv.RiggerFilter.ENTITY;
import java.util.Map;

/**
 * 自动动作处理
 * @author Hongs
 */
@Action("hcim")
public class HcimBaseAction {

    @Action("list")
    public void getList(ActionHelper helper) throws HongsException {
        String conf = (String) helper.getRequest().getAttribute(CONFIG);
        String name = (String) helper.getRequest().getAttribute(ENTITY);
        Model model = getModel(conf, name);
        Map     req = helper.getRequestData();
        Map     rst = model.getPage( req );
        helper.reply(rst);
    }

    @Action("info")
    public void getInfo(ActionHelper helper) throws HongsException {
        String conf = (String) helper.getRequest().getAttribute(CONFIG);
        String name = (String) helper.getRequest().getAttribute(ENTITY);
        Model model = getModel(conf, name);
        Map     req = helper.getRequestData();
        Map     rst = model.getPage( req );
        helper.reply(rst);
    }

    @Action("create")
    @CommitSuccess
    public void doCreate(ActionHelper helper) throws HongsException {
        String conf = (String) helper.getRequest().getAttribute(CONFIG);
        String name = (String) helper.getRequest().getAttribute(ENTITY);
        Model model = getModel(conf, name);
        Map     req = helper.getRequestData();
        String  id  = model.create ( req );
        String  msg = getMsg(conf, name, "create", 1 );
        helper.reply(msg, id);
    }

    @Action("update")
    @CommitSuccess
    public void doUpdate(ActionHelper helper) throws HongsException {
        String conf = (String) helper.getRequest().getAttribute(CONFIG);
        String name = (String) helper.getRequest().getAttribute(ENTITY);
        Model model = getModel(conf, name);
        Map     req = helper.getRequestData();
        int     rd  = model.update ( req );
        String  msg = getMsg(conf, name, "update", rd);
        helper.reply(msg, rd);
    }

    @Action("delete")
    @CommitSuccess
    public void doDelete(ActionHelper helper) throws HongsException {
        String conf = (String) helper.getRequest().getAttribute(CONFIG);
        String name = (String) helper.getRequest().getAttribute(ENTITY);
        Model model = getModel(conf, name);
        Map     req = helper.getRequestData();
        int     rd  = model.delete ( req );
        String  msg = getMsg(conf, name, "delete", rd);
        helper.reply(msg, rd);
    }

    @Action("exists")
    public void isExists(ActionHelper helper)
    throws HongsException {
        String conf = (String) helper.getRequest().getAttribute(CONFIG);
        String name = (String) helper.getRequest().getAttribute(ENTITY);
        Model model = getModel(conf, name);
        Map     req = helper.getRequestData();
        boolean rst = model.exists ( req );
        helper.reply(rst);
    }

    private Model _model;
    private CoreLanguage _lang;

    protected Model getModel(String conf, String name) throws HongsException {
        if (_model == null) {
            _model = DB.getInstance(conf).getModel(name);
        }
        return _model;
    }

    protected String getMsg(String conf, String name, String key, int num) {
        if (_lang == null) {
            _lang = CoreLanguage.getInstance();
            _lang.load(conf);
        }
        name = (name.length()==0? conf: conf+"."+name).replace("/",".");
        name = _lang.containsKey( name ) ? _lang.translate( name ) : "";
        return _lang.translate("fore."+key+".success", name, Integer.toString(num));
    }

}
