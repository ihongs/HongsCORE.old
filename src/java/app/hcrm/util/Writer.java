package app.hcrm.util;

import app.hongs.HongsException;
import app.hongs.db.DB;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 写入器接口
 * @author Hong
 */
public interface Writer {
    public void setCols(Map<String, String> cols);

    public void setDims(List<String> dims);
    
    public void setName(String name);
    
    public Set<String> getKeys();
    
    /**
     * 打开
     * @throws HongsException 
     */
    public void open() throws HongsException;

    /**
     * 写入(临时表)
     * @param data
     * @throws HongsException 
     */
    public void write(Map data) throws HongsException;

    /**
     * 应用(真实表)
     * @throws HongsException 
     */
    public void apply() throws HongsException;
    
    /**
     * 关闭
     * @throws HongsException 
     */
    public void close() throws HongsException;
}
