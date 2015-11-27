package app.hongs.vali;

import app.hongs.HongsException;

/**
 * 亲属模型
 * @author Hongs
 */
public class IsFork extends Rule {
    @Override
    public Object verify(Object value) throws Wrongs, HongsException {
        return value;
    }
}
