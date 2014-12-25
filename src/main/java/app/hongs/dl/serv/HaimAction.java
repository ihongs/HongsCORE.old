package app.hongs.dl.serv;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;
import app.hongs.dl.IAction;

/**
 * 通用信息管理动作
 * @author Hongs
 */
@Action("hcim")
public class HaimAction implements IAction {

    @Action("retrieve")
    public void retrieve(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Action("create")
    public void doCreate(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Action("update")
    public void doUpdate(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Action("delete")
    public void doDelete(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
