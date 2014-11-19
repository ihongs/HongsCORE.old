package app.hongs.serv;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;

/**
 * 通用信息模块处理类
 * @author Hongs
 */
public class HcimModule extends AbstractBaseModel {

    public HcimModule() throws HongsException {
        super("hcim", "a_hcim_module");
    }

}
