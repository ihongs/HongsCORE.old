package app.hongs.dh.serv;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.anno.Action;
import app.hongs.dh.IAction;

/**
 * 通用信息管理动作
 * @author Hongs
 */
@Action("haim/dh")
public class HaimAction implements IAction {

    @Action("retrieve")
    public void retrieve(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Action("create")
    public void create(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Action("update")
    public void update(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Action("delete")
    public void delete(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}