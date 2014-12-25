package app.hongs.dl;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;

/**
 * CRUD 动作模型
 * @author Hongs
 */
public interface IAction {

    public void retrieve(ActionHelper helper) throws HongsException;

    public void doCreate(ActionHelper helper) throws HongsException;

    public void doUpdate(ActionHelper helper) throws HongsException;

    public void doDelete(ActionHelper helper) throws HongsException;

}
