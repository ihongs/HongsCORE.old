package app.hcrm.util;

import app.hongs.HongsException;
import java.util.Map;

/**
 * 加载器接口
 * @author Hong
 */
public interface Loader {
    /**
     * 设置加载参数
     * @param conf datasrc,dataset,manual的并集
     */
    public void setConf(Map conf);
    
    /**
     * 设置加载字段
     */
    public void setCols(Map cols);
    
    /**
     * 获取标识
     * @return 
     */
    public String getFlag();
    
    /**
     * 打开连接
     */
    public void open() throws HongsException;
    
    /**
     * 加载数据, 并调用Writer写入
     * @param writer 
     */
    public void load(Writer writer) throws HongsException;
    
    /**
     * 关闭连接
     */
    public void close() throws HongsException;
}
