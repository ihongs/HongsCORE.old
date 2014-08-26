package app.hcim.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;

/**
 * 通用信息模型处理类
 * @author Hongs
 */
public class Domain extends AbstractBaseModel {

    public Domain() throws HongsException {
        super("hcim", "a_hcim_domain");
    }

}
