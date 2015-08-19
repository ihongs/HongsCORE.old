package app.hongs.dl;

import app.hongs.HongsException;
import java.util.Map;

/**
 * CRUD 记录模型
 * @author Hongs
 */
public interface IRecord {

    public Map retrieve(Map rd) throws HongsException;

    public Map create(Map rd) throws HongsException;

    public int update(Map rd) throws HongsException;

    public int delete(Map rd) throws HongsException;

}
