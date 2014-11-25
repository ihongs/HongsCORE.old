package app.hongs.serv;

import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Model4Crud;

/**
 * 通用信息模块处理类
 * @author Hongs
 */
public class HcimModule extends Model4Crud {

    public HcimModule() throws HongsException {
        super(DB.getInstance("hcim").getTable("a_hcim_module"));
    }

}
