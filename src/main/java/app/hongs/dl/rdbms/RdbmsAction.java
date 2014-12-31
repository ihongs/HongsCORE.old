package app.hongs.dl.rdbms;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.StructConfig;
import app.hongs.serv.CommonFilter;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.dl.IAction;
import app.hongs.util.Tree;
import java.util.Map;

/**
 * 关系数据库存储引擎
 * @author Hongs
 */
public class RdbmsAction implements IAction {

    public void retrieve(ActionHelper helper) throws HongsException {
        String module = helper.getParameter(CommonFilter.MODULE);
        String entity = helper.getParameter(CommonFilter.ENTITY);
        Map form = StructConfig.getInstance(module).getForm(entity);
        String record = Tree.getValue(form, entity, "_record");
        DB  db   = DB.getInstance(module);
        FetchCase caze = new FetchCase( );
    }

    public void doCreate(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void doUpdate(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void doDelete(ActionHelper helper) throws HongsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected void filter(String module, String entity, FetchCase caze, Map rd) {
        
    }
    
    protected void permit(String module, String entity, FetchCase caze, String id) {
        
    }
    
}
