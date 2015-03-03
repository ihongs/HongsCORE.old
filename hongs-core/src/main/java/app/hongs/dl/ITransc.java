package app.hongs.dl;

import app.hongs.HongsException;

/**
 * 事务处理
 * 当 Core.getInstance().containsKey("__IN_TRANSC_MODE__") 时启用事务, 否则即时提交
 * @author Hongs
 */
public interface ITransc {

    public void transc();
    
    public void commit() throws HongsException;

    public void revoke() throws HongsException;

}
