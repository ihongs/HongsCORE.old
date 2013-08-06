package app.hctm.model;

import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;

/**
 * 规则模型
 * @author Hong
 */
public class Rule extends AbstractBaseModel {
    public Rule() throws HongsException {
        super("tm", "a_hctm_rule");
    }
}
