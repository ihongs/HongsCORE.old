package app.hongs;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * <h1>核心类</h1>
 * <pre>
 * 承担"唯一实例"(线程内唯一)的申请与注销操作
 * 必须存在一个无参构造函数或无参"getInstance"方法
 * 获取当前Core的唯一实例总是使用"Core.getInstance()"
 * </pre>
 *
 * <h2>特性解释:</h2>
 * <pre>
 * Servlet在众多实现中总是以单实例多线程的方式工作, 对于某一个请求有且仅有一个线程为
 * 其服务.
 * Core类为框架的对象请求器, 其分配原则为线程内分配, 除个别类通过getInstance自定规则
 * 外, 其他类的实例均在线程内唯一. 故不需要同步即可确保程序的线程安全, 而同时兼顾单例
 * 模式的特点(节省资源, 共享状态).
 * </pre>
 *
 * <h2>静态属性:</h2>
 * <pre>
 * IN_DEBUG_MODE    标识是否处于调试模式
 * IN_SHELL_MODE    标识是否处于动作模式
 * BASE_HREF        应用访问路径(WEB应用中为ContextPath)
 * BASE_PATH        应用目录路径(WEB应用中为WEB-INF目录)
 * CONF_PATH        配置信息存放目录
 * LANG_PATH        语言资源存放目录
 * LOGS_PATH        日志文件存放目录
 * TMPS_PATH        临时文件存放目录
 * SERVER_ID        服务器ID
 * 注: 以上属性需要在 Servlet/Filter/shell 初始化时进行设置.
 * </p>
 *
 * <h2>错误代码:</h2>
 * <pre>
 * 0x25 实例名称不能为空\
 * 0x27 无法获取对应的类
 * 0x28 禁止访问工厂方法
 * 0x2b 无法执行工厂方法
 * 0x2d 执行构造方法失败
 * </pre>
 *
 * @author Hongs
 */
public class Core
extends HashMap<String, Object>
{

  /**
   * 以下部分属性需要在 Action 或 shell 执行时自行配置;
   * ACTION   动作名称(ACTION名称/SHELL名称/URI),
   * LANGUAGE 当前服务所采用的语言.
   */

  /**
   * 当前线程ID
   */
  public long ID;

  /**
   * 线程起始时间
   */
  public long TIME;

  /**
   * 动作
   */
  public String ACTION;

  /**
   * 语言
   */
  public String LANGUAGE;

  /**
   * 核心构造方法
   * 自动放入线程ID和起始时间
   */
  public Core()
  {
    this.ID       = Thread.currentThread().getId();
    this.TIME     = System.currentTimeMillis();
    this.ACTION   = "";
    this.LANGUAGE = "";
  }

  /**
   * 初始化核心
   *
   * @param action 动作
   * @param language 语言
   */
  public final void init(String action, String language)
  {
    this.ID       = Thread.currentThread().getId();
    this.TIME     = System.currentTimeMillis();
    this.ACTION   = action;
    this.LANGUAGE = language;
  }

  /**
   * 销毁核心
   *
   * Destory对象会执行destroy方法
   * GlobalSingleton和ThreadSingleton对象不会被移除
   */
  public final void destroy()
  {
    Iterator it;

    it = this.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry  et = (Map.Entry) it.next();
      Object object =  et.getValue();
      if (object instanceof Destroy)
      {
        ((Destroy) object).destroy();
      }
    }

    it = this.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry  et = (Map.Entry) it.next();
      Object object =  et.getValue();
      if (object instanceof GlobalSingleton
      ||  object instanceof ThreadSingleton)
      {
         continue;
      }
      it.remove();
    }
  }

  private Object check(Core core, String name)
  {
    if (super.containsKey(name))
    {
      return super.get(name);
    }

    if ( core.containsKey(name))
    {
      return  core.get(name);
    }

    if (name == null || name.length() == 0)
    {
      throw new HongsError(0x25, "Instance name can not be empty.");
    }

    return null;
  }

  private Object build(Core core, String name)
  {
    Class klass;

    // 获取类
    try
    {
      klass  =  Class.forName( name );
    }
    catch (ClassNotFoundException ex)
    {
      throw new HongsError(0x27, "Can not find class by name '" + name + "'.");
    }

    return build( core, name, klass );
  }

  private Object build(Core core, String name, Class klass)
  {
    try
    {
      // 获取工厂方法
      Method method = klass.getMethod("getInstance", new Class[] {});

      // 获取工厂对象
      try
      {
        Object object = method.invoke(null, new Object[] {});

        /**
         * 如果该对象被声明成全局单例,
         * 则将其放入顶层核心区
         */
        if (object instanceof GlobalSingleton)
        {
          core.put(name, object);
        }
        else
        {
          this.put(name, object);
        }

        return object;
      }
      catch (IllegalAccessException ex)
      {
        throw new HongsError(0x2b, ex);
      }
      catch (IllegalArgumentException ex)
      {
        throw new HongsError(0x2b, ex);
      }
      catch (InvocationTargetException ex)
      {
        throw new HongsError(0x2b, ex.getCause());
      }
    }
    catch (NoSuchMethodException ex2)
    {
      // 获取标准对象
      try
      {
        Object object = klass.newInstance();

        /**
         * 如果该对象被声明成全局单例,
         * 则将其放入顶层核心区
         */
        if (object instanceof GlobalSingleton)
        {
          core.put(name, object);
        }
        else
        {
          this.put(name, object);
        }

        return object;
      }
      catch (IllegalAccessException ex)
      {
        throw new HongsError(0x2d, ex);
      }
      catch (InstantiationException ex)
      {
        throw new HongsError(0x2d, ex);
      }
    }
    catch (SecurityException ex2)
    {
      throw new HongsError(0x29, ex2);
    }
  }

  /**
   * 获取类对应的唯一对象
   *
   * @param klass [包路径.]类名.class
   * @return 唯一对象
   */
  public Object get(Class klass)
  {
    Core core = Core.getInstance(0);
    String name = klass.getName ( );
    Object inst = check(core, name);
    return inst != null ? inst : build(core, name, klass);
  }

  /**
   * 获取名称对应的唯一对象
   *
   * @param name [包路径.]类名
   * @return 唯一对象
   */
  public Object get(String name)
  {
    Core core = Core.getInstance(0);
    Object inst = check(core, name);
    return inst != null ? inst : build(core, name);
  }

  /**
   * 不支持get(Object), 仅支持get(String)
   * @param name
   * @return 异常
   * @deprecated
   */
  @Override
  public Object get(Object name)
  {
    throw new HongsError(0x10,
      "May cause an error on 'get(Object)', use 'get(String)' or 'get(Class)'");
  }

  /*
  @Override
  public Object remove(Object name)
  {
    throw new HongsError(0x10,
      "May cause an error on 'remove', wait the 'destroy'");
  }

  @Override
  public void clear()
  {
    throw new HongsError(0x10,
      "May cause an error on 'clear', use the 'destroy'");
  }
  */

  /** 静态属性及方法 **/

  /**
   * 是否处于调试模式
   */
  public static boolean IN_DEBUG_MODE;

  /**
   * 是否处于外壳模式
   */
  public static boolean IN_SHELL_MODE;

  /**
   * 应用基础路径
   */
  public static String BASE_HREF;

  /**
   * 应用基础目录
   */
  public static String BASE_PATH;

  /**
   * 配置文件存放目录
   */
  public static String CONF_PATH;

  /**
   * 语言文件存放目录
   */
  public static String LANG_PATH;

  /**
   * 日志文件存放目录
   */
  public static String LOGS_PATH;

  /**
   * 临时数据存放目录
   */
  public static String TMPS_PATH;

  /**
   * 服务器编号
   */
  public static String SERVER_ID;

  /**
   * 核心实例表
   */
  public static Map<Long, Core> INSTANCES = new HashMap<>();

  /**
   * 获取核心实例
   *
   * @param id 线程ID
   * @return 核心实例
   */
  public static Core getInstance(long id)
  {
    if (Core.INSTANCES.containsKey(id))
    {
      return Core.INSTANCES.get(id);
    }
    else
    {
      Core core = new Core();
      Core.INSTANCES.put(id, core);
      return core;
    }
  }

  /**
   * 获取当前核心实例
   *
   * @return 核心实例
   */
  public static Core getInstance()
  {
    return Core.getInstance(Core.getThreadId());
  }

  /**
   * 获取应用实例
   *
   * @param id 线程ID
   * @param klass [包路径.]类名.class
   * @return 应用实例
   */
  public static Object getInstance(long id, Class klass)
  {
    return Core.getInstance(id).get(klass);
  }

  /**
   * 获取当前应用实例
   *
   * @param klass [包路径.]类名.class
   * @return 当前应用实例
   */
  public static Object getInstance(Class klass)
  {
    return Core.getInstance(Core.getThreadId()).get(klass);
  }

  /**
   * 获取应用实例
   *
   * @param id 线程ID
   * @param name [包路径.]类名
   * @return 应用实例
   */
  public static Object getInstance(long id, String name)
  {
    return Core.getInstance(id).get(name);
  }

  /**
   * 获取当前应用实例
   *
   * @param name [包路径.]类名
   * @return 当前应用实例
   */
  public static Object getInstance(String name)
  {
    return Core.getInstance(Core.getThreadId()).get(name);
  }

  /**
   * 获取唯一ID
   *
   * 36进制的12位字串(不包括服务器ID),
   * 至少支持到"2059/01/01 00:00:00".
   * 取值范围: 0~9A~Z
   *
   * @param svid 服务器ID
   * @return 唯一ID
   */
  public static String getUniqueId(String svid)
  {
    long n1 = System.currentTimeMillis();
    String s1 = String.format("%8s", app.hongs.util.Num.to36Radix(n1));

    long n2 = Thread.currentThread().getId();
    String s2 = String.format("%4s", app.hongs.util.Num.to36Radix(n2));

    long n3 = (long)(Math.random() * 1000000);
    String s3 = String.format("%4s", app.hongs.util.Num.to36Radix(n3));

    if (s1.length() > 8) s1 = s1.substring(s1.length() - 8);
    if (s2.length() > 4) s2 = s2.substring(s2.length() - 4);
    if (s3.length() > 4) s3 = s3.substring(s3.length() - 4);

    return (s1 + s2 + s3 + svid).replace(' ', '0');
  }

  /**
   * 获取唯一ID
   *
   * 采用当前服务器ID(Core.SERVER_ID)
   *
   * @return 唯一ID
   */
  public static String getUniqueId()
  {
    return Core.getUniqueId(Core.SERVER_ID);
  }

  /**
   * 获取当前核心ID(即线程ID)
   *
   * @return 核心ID(线程ID)
   */
  private static long getThreadId()
  {
    return Thread.currentThread().getId();
  }

  /**
   * 销毁全部核心对象
   */
  public static void destroyAll()
  {
    /**
     * 逐一销毁核心对象
     */
    Iterator it = Core.INSTANCES.entrySet().iterator();
    while (  it.hasNext()  )
    {
      Map.Entry et = (Map.Entry)it.next();
      Core    core = (Core)et.getValue();
              core.destroy();
    }

    /**
     * 清空重要对象集合
     */
    app.hongs.db.DB.instances = null;
    CoreLanguage.instances = null;
    CoreConfig.instance = null;
    Core.INSTANCES.clear();
  }

  /** 核心接口 **/

  /**
   * 核心处理结束事件接口
   * 当Core结束时(程序结束, Servlet请求结束)
   * 将执行实现了该接口的类的无参destroy方法
   */
  public static interface Destroy { public void destroy(); }

  /**
   * 全局唯一
   * 实现此接口, 则在全局范围内仅构造一次(常驻进程, 通过Core.getInstance获取)
   */
  public static interface GlobalSingleton {}

  /**
   * 线程唯一
   * 实现此接口, 则在线程范围内仅构造一次(常驻线程, 通过Core.getInstance获取)
   */
  public static interface ThreadSingleton {}

}
