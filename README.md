# HongsCORE framework for Java

* 文档版本: 15.05.28
* 软件版本: 0.3.6-20150528
* 设计作者: 黄弘(Kevin Hongs)
* 技术支持: kevin.hongs@gmail.com

**HongsCORE** 即 **Hong's Common Object Requesting Engine**, 通用对象请求引擎, 拼凑的有些生硬. 在设计第一个原型框架时(PHP 版 2006 年), 我买了一台 Intel Core CPU 的笔记本电脑, 当时随意的给她取了个名字叫 Core, 后来觉得名字应该更有意义才扩展成了以上缩写.
另一个原因是: 从最初的 PHP 版一直到现在的 Java 版, 我都有设计一个核心工厂类, 主要作用就是用于请求和管理唯一对象, 实现  Singleton (单例模式), 在需要某个对象时只管请求, 使对象的使用效率更高. 具体到这个 Java 版本中, 利用了 Tomcat 等 Servlet 容器的单实例多线程特性来实现行之有效的单例模式.

原 PHP 版框架在 上海科捷信息技术有限公司(北京) 的 AFP8 系统(已被其他公司收购)中使用, 其部分模块是在科捷的工作期间开发的, 本人已于 2011 年离开科捷, 故不再对其更新. 原科捷公司和其 AFP8 系统及其衍生品拥有原 PHP 版框架代码的全部处置权.

感谢 **林叶,杨林,袁杰,赵征** 等朋友过去对我的支持和理解, 谢谢你们!

## 使用方法

下载 Hongs-CORE-x.x.x.tar.gz 后解压到任意目录, 打开命令行(Linux,Mac的终端)并切换到该目录下, 先执行 `bin/run system:setup` 设置数据库, 再执行 `bin/run server:start` 启动服务器, 然后打开浏览器在地址栏输入 http://localhost:8080/ 即可进入; 登录账号 `admin@xxx.com` 口令 `123456`; 如需停止服务, 关闭命令窗口或按 Ctrl+C 即可; Linux,Mac 系统需要检查 run 是否有执行权限(`chmod +x etc/*`).

同时为 windows 用户提供了 setup.bat 和 start.bat 两个快捷命令来执行上面的两条命令, windows 用户只需双击即可设置和启动.

注意: 需要 JDK 而非 JRE(Java) 才能运行, 使用前请确保 JDK 已安装并加入 PATH 环境变量, 或设置 JAVA_HOME 环境变量为 jdk 的安装目录(Windows 必须设置). Windows,Linux,Mac 系统在官网下载并使用安装程序安装的通常已自动设置好了.

> JDK 下载地址: http://www.oracle.com/technetwork/java/javase/downloads/index.html

## 前端开发

如果你是一位前端开发人员，别去想什么 Node.js,MongoDB 等 javascript 一统前后端的方案了, 你应该做的就是把体验做好, 神马增/删/改/查/接口/权限就不用费劲考虑了, 你只需:

1. 复制 etc/demo/dl.form.xml 到 etc 下并更名为 mytest.form.xml
2. 复制 etc/demo/dl.menu.xml 到 etc 下并更名为 mytest.menu.xml
3. 用文本编辑器打开 mytest.menu.xml 将 demo/dl 全部替换为 mytest, 并将"文档库测试"改成"我的测试"
4. 用文本编辑器打开 default.menu.xml 在 root>menu 下加入[code]<import>mytest</import>[/code]
5. 按照使用方法启动服务并打开浏览器登录后, 就能在右上角下拉菜单找到"我的测试"了

现在, 你可以在项目目录下创建以下文件重建页面体系

    mytest/default.html     索引页
    mytest/list.html        列表页
    mytest/list4select.html 选择页
    mytest/form.html        创建页
    mytest/form4update.html 修改页

一个简单的方法是通过 http://localhost:8080/mytest/form.html 这样的 url 来获取 html 文件, 然后存下来后在这个基础上改. 以后会增加一个按钮来比较方便的固化这个 html.

## 后端开发

> 后续补充

## 类库依赖

    jQuery      [JS]
    Respond     [JS]
    Bootstrap   [JS, CSS]
    Glyphicons  [Bootstrap]

适用 Java 版本 JDK 1.5 及以上, 推荐使用 1.7; Java 库依赖情况请参见各个 module 中 pom.xml 的 dependencies 部分.

## 许可说明

本软件及源码在 [**MIT License**](LICENSE.txt) 下发布，源码开放使用和修改，依赖的库请参阅其对应的许可声明。请在源码中保留作者（**黄弘**）的署名。

> 被授权人权利：  
> 被授权人有权利使用、复制、修改、合并、出版发行、散布、再授权及贩售软件及软件的副本。  
> 被授权人可根据程序的需要修改授权条款为适当的内容。

> 被授权人义务：  
> 在软件和软件的副本中都必须包含版权声明和许可声明。

## 特性概叙

1. 支持库表配置, 支持多数据库;
2. 支持 JOIN,IN 的自动关联查询, 可自动处理关联插入;
3. 支持隶属/一对多/多对多等关联模式并能自动处理关系;
4. 有简单的存取模型基类, 对常规增删改查无需编写代码;
5. 统一的服务端维护程序, 可与前端应用共享配置及模型;
6. 简单有效的动作权限解决方案;
7. 与对应的 HongsCORE4JS(for Javascript) 配合实现高效 WEB 应用开发方案;
8. 默认嵌入 jetty,sqlite,lucene 等库, 除 JDK 外无需安装其他软件即可运行.

## 更新日志

* 2015/08/20 更新 create 方法的返回结构，同 retrieve[info] 避免前端需要识别不同数据结构
* 2015/05/28 强化并首选 jetty 作为嵌入式 web 容器; 因其他容器的 jsp 版本可能与 jetty 使用的冲突, 需要去掉 jsp 相关的包方可在 tomcat 等容器下正常打开页面
* 2015/04/24 完善 Async 并添加 Batch 异步操作类, 为异步任务和消息处理进行准备
* 2015/03/03 重构, 完善权限模块; 重写 Dict.get,Dict.put 及 JS 中的对应方法, 使程序逻辑更简单, 更接近 PHP 的关系数组操作方式
* 2015/02/18 将 ActionWarder 的 Filter 模式改回 0.3.1 的独立继承模式, 新类为 ActionDriver, 兼容 ActionWarder 的 Filter 初始化模式; 今天是我的公历生日, 祝自己生日快乐, 万事如意!
* 2015/02/14 增加 jetty,sqlite, 默认使用 jetty 运行, 数据库默认使用 sqlite
* 2015/01/18 大规模重构, 重新规划 Maven 模块结构
* 2014/11/20 实现 REST 风格的 API, 重新规划了 Action 的组织结构
* 2014/11/10 切换到 Maven
* 2014/08/10 全面倒向 bootstrap, 大规模的使用 tab, 放弃使用浮层作为选择框
* 2014/08/09 为更好的实现自己的规则, 重写了前端验证程序
* 2014/05/17 增加后端验证助手类 app.hongs.action.VerifyHelper, 配合 Verify 注解可以更方便的对传递过来的表单数据进行验证
* 2014/04/04 使用 c3p0 作为数据源组件, 去掉原来的用 jdbc 直接连接的方式, 但保留了使用外部容器的数据源的方式
* 2014/04/03 引入动作注解链，对常规数据的注入、验证以及存储事务提供更方便的操作
* 2014/01/27 去掉全部模型属性的外部名称(id,pid等), 统一使用实际的属性名, 避免因名称转换(同一属性不同场合不同名称)导致理解困难
* 2014/01/25 重写 CmdletHelper 的 args 解析程序, 保留原来类似 Perl 的 Getopt::Longs 的解析规则, 去掉对短参的支持, 不再转换 args 的数组类型. cmdlet 函数也与 main 函数参数一致
* 2013/12/26 使用 InitFilter 处理框架的初始化, 返回数据在 InitFilter 结束前不写入 response 对象, 方便动作注解和其他过滤器对返回数据进行处理
* 2013/10/20 在样式上统一使用 bootstrap(不采取其 JS, JS 仍然使用 jquery.tools)
* 2013/05/26 将项目托管到 GitHub 上进行管理
* 2013/03/08 完成 hongs-core-js 的 jquery-tools 的迁移, 前端组件支撑由原来的 jquery-ui 改为 jquery-tools
* 2012/04/26 将 app.hongs.util.Text 里的数字操作部分拿出来放到 app.hongs.util.Num 里
* 2011/09/25 基本完成 hongs-core-js 的jquery重写实现, 大部分组件已可以使用(不支持拖拽、树搜索)
* 2011/03/23 新增 UploadHelper 上传助手类(使用 apache 的 commons-fileupload)
* 2011/01/25 树模型支持搜索逐个定位(Javascript)
* 2011/01/01 表格列支持拖拽改变尺寸(Javascript)
* 2010/11/21 支持多表串联(JOIN模式)
* 2010/11/12 支持多表串联(IN模式)
* 2010/11/11 更改配置及消息体系(Javascript)
* 2010/09/20 增加日期选择功能(Javascript)
* 2010/08/15 增加浮动块功能(Javascript)
* 2010/06/30 增加 JSP 扩展标记库, 与 Servlet 共享配置和语言对象等
* 2010/05/01 增加动作权限过滤

## 文件体系

### 目录结构:

    /
        - lib               运行库
        - bin               运维脚本
        - etc               配置资源(启动时可指定)
        - var               数据文件(启动时可指定)
            - tmp           临时文件(启动时可指定)
                - upload    文件上传临时存放目录
            - log           运行日志(在log4j2配置)
            - serial        序列缓存数据文件目录
            - sqlite        Sqlite本地数据库目录
            - lucene        Lucene本地索引库目录
        - web
            + common        前端通用库
                - css       前端样式
                - fonts     前端字体
                - img       前端图片
                - pages     通用页面
                - auth      权限信息(虚拟目录)
                - conf      配置资源(虚拟目录)
                - lang      语言资源(虚拟目录)
            - compon        其他可选前端组件
            - hongs         内置样例页面组件

### 类库结构:

    app.hongs           核心
    app.hongs.action    动作支持
    app.hongs.cmdlet    命令支持
    app.hongs.db        关系数据模型
    app.hongs.dl        文档仓库模型
    app.hongs.tags      JSP标签
    app.hongs.util      工具
    app.hongs.serv      服务

以上仅列举了主要的包, 更多框架信息请参考 API 文档.

### 路径映射:

    xxx.foo:bar         对应标记 @Cmdlet("xxx.foo") 的类下 @Cmdlet("bar") 的方法
    xxx.foo             对应标记 @Cmdlet("xxx.foo") 的类下 @Cmdlet("__main__") 的方法
    xxx/foo/bar.act     对应标记 @Action("xxx/foo") 的类下 @Action("bar") 的方法
    xxx/foo.api         对应标记 @Action("xxx/foo") 的类下 @Action("retrieve,create,update或delete") 的方法(这四种动作分别对应 HTTP METHOD: GET,POST,PUT,DELETE)
    common/auth/name.js 读取 WBE-INF/conf/name.as.xml 中 actions+session 的组合
    common/conf/name.js 读取 WEB-INF/conf/name.properties 中 fore.xxxx. 开头的配置
    common/lang/name.js 读取 WEB-INF/conf/name.xx-XX.properties 中 fore.xxxx. 开头的配置

action 和 cmdlet 使用 @Action 和 @Cmdlet 注解来设置访问路径, 如果不指定则用类,方法名作为路径; 请在 WEB-INF/etc/\_begin\_.properties 中设置 core.load.serv 为 Action,Cmdlet 类, 或 xxx.foo.* 告知该包下存在 Action,Cmdlet 类, 多个类/包用";"分隔.
最后3个路径, 将扩展名 .js 换成 .json 即可得到 JSON 格式的数据; 语言配置可在 name 后加语言区域标识, 如 example.zh_CN.js 为获取 example 的中文大陆简体的 js 格式的语言配置.

## 请求规则

支持 Content-Type 为 application/x-www-form-urlencoded, multipart/form-data 和 application/json 的请求, 数据结构为:

    f1=1&f2.-eq=2&f3.-in.=30&f3.-in.=31&t1.f4.-gt=abc&ob=-f5+f6&wd=Hello+world

或兼容 PHP 的方式

    f1=1&f2[-eq]=2&f3[-in][]=30&f3[-in][]=31&t1[f4][-gt]=abc&ob=-f5+f6&wd=Hello+world

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
        "wd": "Hello world"
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

    pn      当前页码(page num)
    rn      额定行数(rows cnt)
    wd      搜索字词(word)
    ob      排序字段(order by)
    sf      查询字段(select for)
    or      或查询
    ar      多组或

## 响应数据

默认返回 JSON 格式的数据:

    {
        "ok": true成功 false失败,
        "err": "错误代码",
        "msg": "响应消息",
        其他...
    }

其他数据通常有:

    // 列表数据, 在 retrieve,list 动作返回
    "list": [
        {
            "字段": "取值",
            ...
        },
        ...
    ],

    // 信息单元, 在 retrieve,info,create 动作返回
    "info": {
        "字段": "取值",
        ...
    }

    // 枚举列表, 在 retrieve,list,info 动作返回
    "enum": {
        "字段": [
            ["取值", "名称"],
            ...
        ],
        ...
    }

    // 分页信息, 在 retrieve,list 动作返回
    "page": {
        "pagecount": 总的页数,
        "rowscount": 当前行数
    }

    // 数量信息, 在 update,delete 动作返回
    "size": 操作数量

在调用 API(REST) 时, 可在 url 后加请求参数 --api-wrap=包裹其他数据的键名, 可加请求参数 --api-conv.= 来转换基本数据类型, 其取值可以为:

    all2str     全部转为字串
    num2str     数字转为字串
    null2str    空转为空字串
    bool2str    true转为字串1, false转为空串
    bool2num    true转为数字1, false转为数字0
    date2sec    转为时间戳(秒)
    date2mic    转为时间戳(毫秒)

dete2mic 或 date2sec 搭配 all2str 则将转换后的时间戳数字再转为字符串; 如果仅指定 all2str 则时间/日期会转为"年-月-日"格式的字符串.

## 模型规范

推荐在实体关系模型(ERM)设计上遵循规范: 表名由 "分区\_模块\_主题\_子主题" 组成, 主题可以有多级, 但推荐最多两级, 模块关系设计成类似树形拓扑的结构.

分区分别为:

    a       应用区, 存放应用数据
    b       仓库区, 存放历史数据
    m       市场区, 存放结果数据
    s       缓冲区, 存放缓冲数据

字段名推荐:

    id      主键, CHAR(20)
    pid     父键, CHAR(20)
    xx_id   外键, CHAR(20), xx为关联表缩写
    ctime   创建时间, DATETIME,TIMESTAMP,BIGINT,INTEGER
    mtime   修改时间, DATETIME,TIMESTAMP,BIGINT,INTEGER
    etime   结束时间, DATETIME,TIMESTAMP,BIGINT,INTEGER
    state   状态标识, TINYINT, 1为正常, 0为删除, 可用其他数字表示其他状态

因字段名可用于 URL 中作为过滤参数, 而部分参数已有特殊含义, 字段取名时请尽量避开这些名称: pn,rn,wd,ob,sf,or,ar. 另, 在配置文件和 Model 中可以重新定义这些名称, 但并不建议修改(我信奉少量的约定胜于过多的配置).

# HongsCORE framework for Javascript

> 请转至: https://github.com/ihongs/HongsCORE/tree/develop/hongs-web/web/common

# KEEP IT SIMPLE, STUPID!

> 编写只做一件事情，并且要做好的程序；编写可以在一起工作的程序，编写处理文本流的程序，因为这是通用的接口。这就是UNIX哲学。所有的哲学真正的浓缩为一个铁一样的定律，高明的工程师的神圣的“KISS 原则”无处不在。大部分隐式的UNIX哲学不是这些前辈所说的，而是他们所做的和UNIX自身建立的例子。从整体上看，我们能够抽象出下面这些观点：

> 1.  模块原则：写简单的，通过干净的接口可以被连接的部件。  
> 2.  清楚原则：清楚要比小聪明好。  
> 3.  合并原则：设计能被其它程序连接的程序。  
> 4.  分离原则：从机制分离从策略，从实现分离出接口。  
> 5.  简单原则：设计要简单，仅当你需要的时候才增加复杂性。  
> 6.  节俭原则：只有当被证实是清晰，其它什么也不做的时候，才写大的程序。  
> 7.  透明原则：为使检查和调试更容易而设计。  
> 8.  健壮原则：健壮性是透明和简单的追随者。  
> 9.  表现原则：把知识整理成资料，于是程序逻辑能变得易理解和精力充沛的。  
> 10. 意外原则：在接口设计中，总是做最小意外的事情。  
> 11. 沉默原则：一个程序令人吃惊什么也不说的时候，他应该就是什么也不说。  
> 12. 补救原则：当你必须失败的时候，尽可能快的吵闹地失败。  
> 13. 经济原则：程序员的时间很宝贵，优先机器时间来节约它。  
> 14. 产生原则：避免手工堆砌，当你可能的时候，编写可以写程序的程序。  
> 15. 优化原则：在雕琢之前先有原型；在你优化它之前，先让他可以运行。  
> 16. 差异原则：怀疑所有声称的“唯一真理”。  
> 17. 扩展原则：为将来设计，因为它可能比你认为的来得要快。
