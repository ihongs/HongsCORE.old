package app.hcim.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.FetchCase;
import java.util.List;
import java.util.Map;

/**
 * 通用信息模型处理类
 * @author Hongs
 */
public class Entity extends AbstractBaseModel {

    public Entity() throws HongsException {
        super("hcim", "a_hcim_entity");
    }

}
