package app.hctm.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;

/**
 * 代码模型
 * @author Hong
 */
public class Code extends AbstractBaseModel {
    public Code() throws HongsException {
        super("tm", "a_hctm_code");
    }
}
