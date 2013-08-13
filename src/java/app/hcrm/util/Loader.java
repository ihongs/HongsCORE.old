package app.hcrm.util;

import app.hongs.HongsException;
import java.util.Date;
import java.util.Map;

/**
 * 加载器接口
 * @author Hong
 */
public interface Loader {
    /**
     * 设置加载参数
     * @param conf datasrc,dataset,execset的并集
     */
    public void setConf(Map conf);

    public void setTime(Date time);

    /**
     * 打开
     */
    public void open() throws HongsException;

    /**
     * 加载, 并调用Writer写入
     * @param writer
     */
    public void load(Writer writer) throws HongsException;

    /**
     * 关闭
     */
    public void close() throws HongsException;
}
