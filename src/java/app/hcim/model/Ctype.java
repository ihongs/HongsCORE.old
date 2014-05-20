package app.hcim.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.FetchMore;
import java.util.List;
import java.util.Map;

/**
 * 通用信息模型处理类
 * @author Hongs
 */
public class Ctype extends AbstractBaseModel {

    public Ctype() throws HongsException {
        super("hcim", "a_hcim_ctype");
    }

}
