package app.hongs.dl;

import app.hongs.HongsException;

/**
 * 提交事务
 * 当 Core.getInstance().containsKey("__IN_COMMIT_MODE__") 时启用事务, 否则即时提交
 * @author Hongs
 */
public interface ITrnsct {

    public void trnsct();

    public void commit() throws HongsException;

    public void rolbak() throws HongsException;

}
