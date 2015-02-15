# HongsCORE Framework for Java

* 文档版本: 15.02.14
* 软件版本: 0.3.6-20150214
* 设计作者: 黄弘(Kevin Hongs)
* 技术支持: kevin.hongs@gmail.com

HongsCORE 即 Hong's Common Object Requesting Engine, 通用对象请求引擎, 拼凑的有些生硬. 在设计第一个原型框架时(PHP 版 2006 年), 我买了一台 Intel Core CPU 的笔记本电脑, 当时随意的给她取了个名字叫 Core, 后来觉得名字应该更有意义才扩展成了以上缩写.
另一个理由是, 从最初的 PHP 版一直到现在的 Java 版, 我都有设计一个核心工厂类, 主要作用就是用于请求和管理唯一对象, 实现  Singleton (单例模式), 在需要某个对象时只管请求, 使对象的使用效率更高. 具体到这个 Java 版本中, 利用了 Tomcat 等 Servlet 容器的单实例多线程特性来实现行之有效的单例模式.

原 PHP 版框架在 上海科捷信息技术有限公司(北京) 的 AFP8 系统(已被其他公司收购)中使用, 其部分模块是在科捷的工作期间开发的, 本人已于 2011 年离开科捷, 故不再对其更新. 原科捷公司和其 AFP8 系统及其衍生品拥有原 PHP 版框架代码的全部处置权.

感谢 林叶,杨林,袁杰,赵征 等朋友过去对我的支持和理解, 谢谢你们!

## 使用方法

下载 HongsCORE-x.x.x.war 更名为 HongsCORE-x.x.x.zip, 解压到任意目录, 打开命令行(Linux,Mac的终端)并切换到该目录下, 先执行 WEB-INF/run common:setup 设置数据库, 再执行 WEB-INF/run server:start 启动服务器, 然后打开浏览器在地址栏输入 http://127.0.0.1:8080 即可进入; 如需停止服务, 关闭命令窗口或按 Ctrl+C 即可. 注意, 本软件需要 JDK(非JRE,Java) 才能运行, 使用前请确保 JDK 已安装并加入 PATH 环境变量(如果是在官网下载并使用安装程序安装的则一般已自动设置好了).

> JDK 下载地址: http://www.oracle.com/technetwork/java/javase/downloads/index.html

## 许可说明

在 MIT License 下发布，作者编写的源码开放使用和修改，依赖的库请参阅对应的许可声明。请在源码中保留作者（黄弘）的署名。

> 被授权人权利：  
> 被授权人有权利使用、复制、修改、合并、出版发行、散布、再授权及贩售软件及软件的副本。  
> 被授权人可根据程序的需要修改授权条款为适当的内容。

> 被授权人义务：  
> 在软件和软件的副本中都必须包含版权声明和许可声明。

## 特性概叙

1. 支持库表配置, 支持多数据库;
2. 支持 IN,JOIN 的自动关联查询, 可自动处理关联插入;
3. 支持 隶属,一对多,多对多 等关联模式并自动处理关系;
4. 支持简单存取模型基类(对于常规的列表/增/删/改等操作无需编写大量代码, 只需继承或实现指定方法即可);
5. 简单有效的动作权限解决方案;
6. 支持统一的服务器端维护程序;
7. 可与前端应用共享配置及模型;
8. 与对应的 HongsCORE4JS (for Javascript) 配合实现高效WEB应用开发方案;
9. 默认内置 jetty,sqlite,lucene 等, 除 Java 外无需安装其他软件即可运行.

## 更新日志

* 2015/02/14 增加 jetty,sqlite, 数据库默认使用 sqlite
* 2015/01/18 大规模重构, 重新规划 Maven 模块结构
* 2014/11/20 实现 REST 风格的 API, 重新规划了 Action 的组织结构
* 2014/11/10 切换到 Maven
* 2014/08/10 全面倒向 bootstrap; 大规模的使用 tab, 放弃使用浮层作为选择框
* 2014/08/09 为更好的实现自己的规则, 重写了前端验证程序
* 2014/05/17 增加后端验证助手类 app.hongs.action.VerifyHelper, 配合 Verify 注解可以更方便的对传递过来的表单数据进行验证
* 2014/04/04 使用 c3p0 作为数据源组件, 去掉原来的用 jdbc 直接连接的方式, 但保留了使用外部容器的数据源的方式
* 2014/04/03 引入动作注解链，对常规数据的注入、验证以及存储事务提供更方便的操作
* 2014/01/27 去掉全部模型属性的外部名称(id,pid等), 统一使用实际的属性名, 避免因名称转换(同一属性不同场合不同名称)导致理解困难
* 2014/01/25 重写CmdletHelper的args解析程序, 保留原来类似Perl的Getopt::Longs的解析规则, 去掉对短参的支持, 不再转换args的数组类型. cmdlet函数也与main函数参数一致
* 2013/12/26 使用InitFilter处理框架的初始化, 返回数据在InitFilter结束前不写入response对象, 方便动作注解和其他过滤器对返回数据进行处理
* 2013/10/20 在样式上统一使用bootstrap(不采取其JS, JS仍然使用jquery.tools)
* 2013/05/26 将项目托管到GitHub上进行管理
* 2013/03/08 完成hongs-core-js的jquery-tools的迁移, 前端组件支撑由原来的jquery-ui改为jquery-tools
* 2012/04/26 将app.hongs.util.Text里的数字操作部分拿出来放到app.hongs.util.Num里
* 2011/09/25 基本完成hongs-core-js的jquery重写实现, 大部分组件已可以使用(不支持拖拽、树搜索)
* 2011/03/23 新增UploadHelper上传助手类(使用apache的commons-fileupload)
* 2011/01/25 树模型支持搜索逐个定位(Javascript)
* 2011/01/01 表格列支持拖拽改变尺寸(Javascript)
* 2010/11/21 支持多表串联(JOIN模式)
* 2010/11/12 支持多表串联(IN模式)
* 2010/11/11 更改配置及消息体系(Javascript)
* 2010/09/20 增加日期选择功能(Javascript)
* 2010/08/15 增加浮动块功能(Javascript)
* 2010/06/30 支持JSP扩展标记库, 与Servlet共享配置和语言对象等
* 2010/05/01 支持动作过滤, 轻松实现权限过滤...

## 依赖列表

    Jquery      [JS]
    Respond     [JS]
    Bootstrap   [JS, CSS]
    Glyphicons  [Bootstrap]

适用 Java 版本 JDK 1.6 及以上; Java 库请参见 module 中 pom.xml 的依赖部分.

## 文件体系

### 目录结构:

    /
      + WEB-INF
        - etc           配置资源(配置/语言/权限/集合/数据库等)
        - lib           后端库
        - var           临时文件(可配置)
          - log         运行日志(可配置)
          - ser         缓存文件(可配置)
          - sqlite      Sqlite本地数据库目录
          - lucene      Lucene本地索引库目录
      + common          前端通用库(js)
        - css           前端样式
        - fonts         前端字体
        - img           前端图片
        - pages         通用页面(页头/页尾/组件等jsp,html文件)
        - auth          权限信息(虚拟目录)
        - conf          配置信息(虚拟目录)
        - lang          语言资源(虚拟目录)
      - compon          其他前端组件

### 类库结构:

    app.hongs           核心
    app.hongs.action    动作支持
    app.hongs.cmdlet    命令支持
    app.hongs.db        关系数据模型
    app.hongs.dl        文档仓库模型
    app.hongs.serv      内置服务库
    app.hongs.tags      JSP标签
    app.hongs.util      工具

以上仅列举了主要的包, 更多框架信息请参考 API 文档.

### 路径映射:

    xxx.foo:bar         对应标记 @Cmdlet("xxx.foo") 的类下 @Cmdlet("bar") 的方法
    xxx.foo             对应标记 @Cmdlet("xxx.foo") 的类下 @Cmdlet("__main__") 的方法
    xxx/foo/bar.act     对应标记 @Action("xxx/foo") 的类下 @Action("bar") 的方法
    xxx/foo.api         对应标记 @Action("xxx/foo") 的类下 @Action("retrieve,create,update或delete") 的方法(这四种动作分别对应 HTTP METHOD: GET,POST,PUT,DELETE)
    common/auth/name.js 读取 WBE-INF/conf/name.as.xml 中 actions+session 的组合
    common/conf/name.js 读取 WEB-INF/conf/name.properties 中 fore.xxxx. 开头的配置
    common/lang/name.js 读取 WEB-INF/conf/name.xx-XX.properties 中 fore.xxxx. 开头的配置

action 和 cmdlet 使用 @Action 和 @Cmdlet 注解来设置访问路径, 如果不指定则用类,方法名作为路径; 请在 _begin_.properties 中设置 core.load.serv 告知 Action,Cmdlet 类, 或用 xxx.foo.* 告知该包下存在 Action,Cmdlet 类.
最后3个路径, 将扩展名 .js 换成 .json 即可得到 JSON 格式的数据; 语言配置可在 name 后加语言区域标识, 如 example.zh-CN.js 为获取 example 的中文大陆简体的语言配置.

## 请求规则

    f1=1&f2.-eq=2&f3.-in.=30&f3.-in.=31&t1.f4.-gt=abc&ob=-f5+f6&wd=hello

或兼容 PHP 的方式

    f1=1&f2[-eq]=2&f3[-in][]=30&f3[-in][]=31&t1[f4][-gt]=abc&ob=-f5+f6&wd=hello

会转成 JSON 结构:

    {
        "f1": 1,
        "f2": {
            "-eq": 2
        },
        "f3": {
            "-in": [
                30,
                31
            ]
        },
        "t1": {
            "f4": {
                "-gt": "abc"
            }
        },
        "ob": "-f5 f6",
        "wd": "hello"
    }

注: "+" 在 URL 中为空格.

其中 .-eq 这样的标识为过滤操作符, 其含义为:

    eq      等于
    ne      不等于
    gt      大于
    ge      大于或等于
    lt      小于
    le      小于或等于
    in      包含
    ni      不包含

有一些参数名具有特定意义, 如:

    wd      搜索字词
    ob      排序字段
    pn      当前页码
    rn      额定行数
    cs      限定列名

## 模型规范

通常在关系模型(ERM)设计上遵循规范: 表名由 "分区_模块_主题_子主题" 组成, 主题可以有多级, 但推荐最多两级主题, 推荐设计成类似树形拓扑的结构.

分区分别为:

    a       应用区, 存放应用数据
    b       仓库区, 存放历史数据
    m       市场区, 存放结果数据
    s       缓冲区, 存放缓冲数据

字段名推荐:

    id      主键, CHAR(20)
    pid     父键, CHAR(20)
    xx_id   外键, CHAR(20), xx为关联表缩写
    ctime   创建时间, DATETIME或TIMESTAMP
    mtime   修改时间, DATETIME或TIMESTAMP
    etime   结束时间, DATETIME或TIMESTAMP
    state   状态标识, TINYINT, 1为正常, 0为删除, 可用其他数字表示其他状态

因字段名可用于URL中作为过滤参数, 而部分参数已有特殊含义, 字段取名时请务必避开这些名称: pn,rn,cs,ob,wd. 另, 在配置文件和Model中可以重新定义这些名称, 但并不建议修改(我信奉少量的约定胜于过多的配置).

# HongsCORE Framework for Javascript

> 请转至 https://github.com/ihongs/HongsCORE/tree/develop/hongs-web/web/common/src

# KEEP IT SIMPLE, STUPID!

编写只做一件事情，并且要做好的程序；编写可以在一起工作的程序，编写处理文本流的程序，因为这是通用的接口。这就是UNIX哲学。所有的哲学真正的浓缩为一个铁一样的定律，高明的工程师的神圣的“KISS 原则”无处不在。大部分隐式的UNIX哲学不是这些前辈所说的，而是他们所做的和UNIX自身建立的例子。从整体上看，我们能够抽象出下面这些观点：

1.  模块原则：写简单的，通过干净的接口可被连接的部件。
2.  清楚原则：清楚要比小聪明好。
3.  合并原则：设计能被其它程序连接的程序。
4.  分离原则：从机制分离从策略，从实现分离出接口。
5.  简单原则：设计要简单；只有当你需要的时候，增加复杂性。
6.  节俭原则：只有当被证实是清晰，其它什么也不做的时候，才写大的程序。
7.  透明原则：为使检查和调试更容易而设计。
8.  健壮原则：健壮性是透明和简单的追随者。
9.  表现原则：把知识整理成资料，于是程序逻辑能变得易理解和精力充沛的。
10. 意外原则：在接口设计中，总是做最小意外事情。
11. 沉默原则：一个程序令人吃惊什么也不说的时候，他应该就是什么也不说。
12. 修补补救：当你必须失败的时候，尽可能快的吵闹地失败。
13. 经济原则：程序员的时间是宝贵的；优先机器时间节约它。
14. 产生原则：避免手工堆砌；当你可能的时候，编写可以写程序的程序。
15. 优化原则：在雕琢之前先有原型；在你优化它之前，先让他可以运行。
16. 差异原则：怀疑所有声称的“唯一真理”。
17. 扩展原则：为将来做设计，因为它可能比你认为来的要快。
