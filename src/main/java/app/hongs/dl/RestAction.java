package app.hongs.dl;

import app.hongs.action.ActionHelper;
import app.hongs.annotation.Action;

/**
 * CRUD 动作
 * @author Hongs
 */
@Action
public interface RestAction {

    @Action("list")
    public void retrieve(ActionHelper helper);

    @Action("create")
    public void doCreate(ActionHelper helper);

    @Action("update")
    public void doUpdate(ActionHelper helper);

    @Action("delete")
    public void doDelete(ActionHelper helper);

}
