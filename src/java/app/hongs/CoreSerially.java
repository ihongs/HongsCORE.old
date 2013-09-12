package app.hongs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
 * <h1>临时数据工具</h1>
 *
 * <h2>特别注意:</h2>
 * <pre>
 * 只有"当前类"的"非" static 类型的 public 的属性会被存储, 特殊情况可重载
 * saveData(data) 和 loadData(data) 来实现.
 * entrySet(),keySet(),values() 返回的对象无法被序列化,
 * 可 new HashSet(x.entrySet()) 预处理后再存入当前对象.
 * 详见: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4756277
 * </pre>
 *
 * <h2>异常代码:</h2>
 * <pre>
 * 区间: 0x10d0~0x10df
 * 0x10d0 创建临时文件失败
 * 0x10d2 写入临时文件失败
 * 0x10d4 读取临时文件失败
 * 0x10d6 找不到临时文件
 * 0x10d8 找不到对应的类
 * 0x10da 无法读取属性
 * 0x10dc 无法写入属性
 * </pre>
 *
 * @author Hongs
 */
public abstract class CoreSerially
  implements Serializable
{
    
  private static ReadWriteLock lock = new ReentrantReadWriteLock();  

  /**
   * 以有效到期时间方式构造实例
   * @param path
   * @param name
   * @param date
   * @throws app.hongs.HongsException
   */
  public CoreSerially(String path, String name, Date date)
    throws HongsException
  {
    this.init(path, name, date);
  }

  /**
   * 以有效时间间隔方式构造实例
   * @param path
   * @param name
   * @param time
   * @throws app.hongs.HongsException
   */
  public CoreSerially(String path, String name, long time)
    throws HongsException
  {
    this.init(path, name, time);
  }

  /**
   * 以有效到期时间方式构造实例
   * @param name
   * @param date
   * @throws app.hongs.HongsException
   */
  public CoreSerially(String name, Date date)
    throws HongsException
  {
    this.init(name, date);
  }

  /**
   * 以有效时间间隔方式构造实例
   * @param name
   * @param time
   * @throws app.hongs.HongsException
   */
  public CoreSerially(String name, long time)
    throws HongsException
  {
    this.init(name, time);
  }

  /**
   * 构造无限期的实例
   * @param name
   * @throws app.hongs.HongsException
   */
  public CoreSerially(String name)
    throws HongsException
  {
    this.init(name);
  }

  /**
   * 空构造器(请自行执行init方法)
   */
  public CoreSerially()
  {
    // TODO: 请自行执行init方法
  }

  /**
   * 初始化方法(有效到期时间方式)
   * @param path
   * @param name
   * @param date
   * @throws app.hongs.HongsException
   */
  protected void init(String path, String name, Date date)
    throws HongsException
  {
    if (path == null)
    {
      path = Core.TMPS_PATH;
    }

    /**
     * 如果文件不存在, 则加载数据并存入缓存文件
     * 如果文件已到期, 则加载数据并存入缓存文件
     * 否则从文件加载对象数据
     */

    File file = new File(path + File.separator + name + ".ser");

    if (!file.exists())
    {
      try
      {
        file.createNewFile();
      }
      catch (IOException ex)
      {
        throw new HongsException(0x10d0, ex);
      }

      this.loadData();
      this.saveToFile(file);
    }
    else
    if (this.isExpired(date != null ? date.getTime() : 0))
    {
      this.loadData();
      this.saveToFile(file);
    }
    else
    {
      this.loadByFile(file);
    }
  }

  /**
   * 初始化方法(有效时间间隔方式)
   * @param path
   * @param name
   * @param time
   * @throws app.hongs.HongsException
   */
  protected void init(String path, String name, long time)
    throws HongsException
  {
    if (path == null)
    {
      path = Core.TMPS_PATH;
    }

    /**
     * 如果文件不存在, 则加载数据并存入缓存文件
     * 如果文件已到期, 则加载数据并存入缓存文件
     * 否则从文件加载对象数据
     */

    File file = new File(path + File.separator + name + ".ser");

    if (!file.exists())
    {
      try
      {
        file.createNewFile();
      }
      catch (IOException ex)
      {
        throw new HongsException(0x10d0, ex);
      }

      this.loadData();
      this.saveToFile(file);
    }
    else
    if (this.isExpired(time + file.lastModified()))
    {
      this.loadData();
      this.saveToFile(file);
    }
    else
    {
      this.loadByFile(file);
    }
  }

  protected void init(String name, Date date)
    throws HongsException
  {
    this.init(null, name, date);
  }

  protected void init(String name, long time)
    throws HongsException
  {
    this.init(null, name, time);
  }

  protected void init(String name)
    throws HongsException
  {
    this.init(null, name, null);
  }

  /**
   * 是否已经过期
   * @param time
   * @return 返回true将重建缓存
   * @throws app.hongs.HongsException
   */
  protected boolean isExpired(long time)
    throws HongsException
  {
    return time != 0 && time < System.currentTimeMillis();
  }

  /**
   * 将对象存入文件
   * @param file
   * @throws app.hongs.HongsException
   */
  protected void saveToFile(File file)
    throws HongsException
  {
    try
    {
      lock.writeLock().lock(); 

        FileOutputStream fos = new   FileOutputStream(file);
      ObjectOutputStream oos = new ObjectOutputStream(fos );

//    fos.getChannel().tryLock();
      Map map = new HashMap();
        this.saveData(map);
      oos.writeObject(map);

      oos.flush();
      oos.close();
    }
    catch (FileNotFoundException ex)
    {
      throw new HongsException(0x10d6, ex);
    }
    catch (IOException ex)
    {
      throw new HongsException(0x10d2, ex);
    }
    finally
    {
      lock.writeLock().unlock();
    }
  }

  /**
   * 从文件加载对象
   * @param file
   * @throws app.hongs.HongsException
   */
  protected void loadByFile(File file)
    throws HongsException
  {
    try
    {
      lock.readLock().lock();

        FileInputStream fis = new   FileInputStream(file);
      ObjectInputStream ois = new ObjectInputStream(fis );

      Map map = (Map)ois.readObject();
        this.loadData(map);

      ois.close();
    }
    catch (ClassNotFoundException ex)
    {
      throw new HongsException(0x10d8, ex);
    }
    catch (FileNotFoundException ex)
    {
      throw new HongsException(0x10d6, ex);
    }
    catch (IOException ex)
    {
      throw new HongsException(0x10d4, ex);
    }
    finally
    {
      lock.readLock().unlock();
    }
  }

  /**
   * 从当前对象获取属性写入缓存
   * @param map
   * @throws HongsException
   */
  protected void saveData(Map<String, Object> map)
    throws HongsException
  {
    Field[] fields = this.getClass().getDeclaredFields();
    for (Field field : fields)
    {
      int ms = field.getModifiers();
      if (Modifier.isStatic(ms)
      || !Modifier.isPublic(ms))
      {
        continue;
      }

      String name = field.getName();

      try
      {
        map.put(name, field.get(this));
      }
      catch (IllegalArgumentException ex)
      {
        throw new HongsException(0x10da, ex);
      }
      catch (IllegalAccessException ex)
      {
        throw new HongsException(0x10da, ex);
      }
    }
  }

  /**
   * 从缓存获取属性写入当前对象
   * @param map
   * @throws app.hongs.HongsException
   */
  protected void loadData(Map<String, Object> map)
    throws HongsException
  {
    Field[] fields = this.getClass().getDeclaredFields();
    for (Field field : fields)
    {
      int ms = field.getModifiers();
      if (Modifier.isStatic(ms)
      || !Modifier.isPublic(ms))
      {
        continue;
      }

      String name = field.getName();

      try
      {
        field.set(this, map.get(name));
      }
      catch (IllegalArgumentException ex)
      {
        throw new HongsException(0x10da, ex);
      }
      catch (IllegalAccessException ex)
      {
        throw new HongsException(0x10da, ex);
      }
    }
  }

  /**
   * 从外部获取属性写入当前对象
   * @throws app.hongs.HongsException
   */
  abstract protected void loadData()
    throws HongsException;

}
