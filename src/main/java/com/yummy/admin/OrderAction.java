package com.yummy.admin;

import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.db.DB;
import app.hongs.db.Model;
import app.hongs.dl.IAction;
import java.util.Map;

/**
 * 商品动作
 * @author Hongs
 */
@Action("yummy/admin/order")
public class OrderAction implements IAction {

    Model model;
    CoreLanguage lang;
    
    public OrderAction() throws HongsException {
        model = DB.getInstance("yummy").getModel("order");
        lang  = CoreLanguage.getInstance().clone();
        lang.load("yummy.admin");
    }
    
    @Action("retrieve")
    public void retrieve(ActionHelper helper) throws HongsException {
        Map    rd = helper.getRequestData();
        String id = (String) rd.get("id");
        Map    sd;
        if (null != id && !"".equals(id)) {
            sd = model.getList(rd);
        } else {
            sd = model.getInfo(rd);
        }
        helper.reply(sd);
    }

    @Action("create")
    public void doCreate(ActionHelper helper) throws HongsException {
        Map    rd = helper.getRequestData();
        String id = model.create(rd);
        String nm = (String) rd.get("name");
        helper.reply(lang.translate("core.create.success"), id, nm);
    }

    @Action("update")
    public void doUpdate(ActionHelper helper) throws HongsException {
        Map rd = helper.getRequestData();
        int an = model.update(rd);
        helper.reply(lang.translate("core.update.success", Integer.toString(an)));
    }

    @Action("delete")
    public void doDelete(ActionHelper helper) throws HongsException {
        Map rd = helper.getRequestData();
        int an = model.delete(rd);
        helper.reply(lang.translate("core.delete.success", Integer.toString(an)));
    }
    
}
