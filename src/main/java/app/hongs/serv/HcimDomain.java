package app.hongs.serv;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;

/**
 * 通用信息模型处理类
 * @author Hongs
 */
public class HcimDomain extends AbstractBaseModel {

    public HcimDomain() throws HongsException {
        super("hcim", "a_hcim_domain");
    }

}
