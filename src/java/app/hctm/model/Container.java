package app.hctm.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;

/**
 * 容器模型
 * @author Hong
 */
public class Container extends AbstractBaseModel {
    public Container() throws HongsException {
        super("tm", "a_hctm_container");
    }
}
