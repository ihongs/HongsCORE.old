package app.hctm.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;

/**
 * 宏变量模型
 * @author Hong
 */
public class Var extends AbstractBaseModel {
    public Var() throws HongsException {
        super("tm", "a_hctm_var");
    }
}
