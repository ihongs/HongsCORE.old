package app.haim.action;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.annotation.CommitSuccess;
import app.hongs.db.AbstractBaseModel;
import java.util.Map;

/**
 *
 * @author Hongs
 */
public abstract class AbstractAction {

    protected abstract AbstractBaseModel getModel();
    protected abstract String getSaveSuccessMsg(String nms);
    protected abstract String getRemoveSuccessMsg(String nms);

    public void actionList(ActionHelper helper)
    throws HongsException {
        AbstractBaseModel model = this.getModel();
        Map data = model.getPage(helper.getRequestData());
        helper.back(data);
    }

    public void actionInfo(ActionHelper helper)
    throws HongsException {
        AbstractBaseModel model = this.getModel();
        Map data = model.getInfo(helper.getRequestData());
        helper.back(data);
    }

    @CommitSuccess
    public void actionSave(ActionHelper helper)
    throws HongsException {
        AbstractBaseModel model = this.getModel();
        String key = model.save(helper.getRequestData());
        String nms = model.getAffectedNames();
        String msg = this.getSaveSuccessMsg(nms);
        helper.back(msg, key, nms);
    }

    @CommitSuccess
    public void actionRemove(ActionHelper helper)
    throws HongsException {
        AbstractBaseModel model = this.getModel();
        model.remove(helper.getRequestData());
        String nms = model.getAffectedNames();
        String msg = this.getRemoveSuccessMsg(nms);
        helper.back(msg);
    }

    public void actionUnique(ActionHelper helper)
    throws HongsException {
        AbstractBaseModel model = this.getModel();
        boolean rst = model.unique(helper.getRequestData());
        helper.back(rst);
    }

}
