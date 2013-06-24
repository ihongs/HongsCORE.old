package app.hcrm.util;

import app.hongs.HongsException;
import java.util.Map;

/**
 * 写入器接口
 * @author Hong
 */
public interface Writer {
    /**
     * 设置要写入的字段
     * @param cols {当前表字段: 来源表字段}
     */
    public void setCols(Map cols);
    
    /**
     * 设置写入的区域名称
     * @param name 
     */
    public void setName(String name);
    
    /**
     * 设置写入的片段标识
     * @param name 
     */
    public void setFlag(String name);
    
    /**
     * 获取写入的起始位置
     * @return 
     */
    public int getPos();
    
    /**
     * 获取写入的数据长度
     * @return 
     */
    public int getLen();
    
    /**
     * 打开连接
     */
    public void open() throws HongsException;
    
    /**
     * 清理数据
     */
    public void clean() throws HongsException;
    
    /**
     * 写入数据
     */
    public void write(Map data) throws HongsException;
    
    /**
     * 断开连接
     */
    public void close() throws HongsException;
}
