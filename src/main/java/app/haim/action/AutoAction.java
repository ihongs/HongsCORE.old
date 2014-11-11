package app.haim.action;

import app.hongs.CoreConfig;
import app.hongs.db.AbstractBaseModel;

/**
 *
 * @author Hongs
 */
public class AutoAction extends AbstractAction {

    public AutoAction(CoreConfig conf) {
        
    }

    @Override
    protected AbstractBaseModel getModel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getSaveSuccessMsg(String nms) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getRemoveSuccessMsg(String nms) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
