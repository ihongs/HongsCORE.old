package app.hongs.vali;

import app.hongs.HongsException;

/**
 * 外键规则
 *
 * 原名 IsPick, 对应 form 类型名为 pick，改名 fork 意为 foreign key
 *
 * @author Hongs
 */
public class IsFork extends Rule {
    @Override
    public Object verify(Object value) throws Wrongs, HongsException {
        return value;
    }
}
