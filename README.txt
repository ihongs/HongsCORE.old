<< HongsCORE Framework for Java >>

文档版本: 10.09.25
软件版本: 0.1.1-20110925
设计作者: 黄弘(Kevin Hongs)
技术支持:
  Phone: +8618621523173
  Email: kevin.hongs@gmail.com

[名称解释]

HongsCORE
即: Hong's Common Object Requesting Engine, 通用对象请求引擎, 拼凑的有些生硬. 在设
计第一个原型框架时(PHP版), 我买了一台Intel Core CPU的笔记本电脑,当时随意的给她取了个
名字叫Core, 后来觉得名字应该更有意义才扩展成了以上缩写. 另一个理由是: 从最初的PHP版一
直到现在的Java版, 我都有设计一个核心工厂类, 主要作用就是用于请求和管理唯一对象, 实现
Singleton(单例模式), 在需要某个对象时只管请求就是, 无需实例化, 使对象的使用效率更高.
具体到这个Java版本中, 利用了Tomcat等Servlet容器的单实例多线程特性来实现行之有效的单例
模式.
原PHP版框架在科捷(http://www.kejet.com)的AFP8中使用, 其部分模块的开发在科捷公司进行, 本人于
2011年离开科捷, 故不再对其更新. 原科捷公司和其AFP8系统拥有原框架的PHP及JS等代码的全部
处置权.

[特性概叙]

1. 支持库表配置, 支持多数据库;
2. 支持IN或JOIN的自动关联查询;
3. 支持"隶属/一对一/一对多/多对一/多对多"关联方式;
4. 支持简单存取模型基类(对于常规的列表/增/删/改等操作无需编写大量代码, 只需继承或实现
   指定方法即可);
5. 简单有效的动作权限解决方案;
6. 支持统一的服务器端维护程序;
7. 可与前端应用共享配置及模型;
8. 与对应的HongsCORE4JS(for Javascript)配合实现高效WEB应用开发方案.

[更新日志]

[2013-10-20] 在样式上统一使用bootstrap(不采取其JS, JS仍然使用jquery.tools)
[2013-03-08] 完成hongs-core-js的jquery-tools的迁移, 前端组件支撑由原来的jquery-ui
             改为jquery-tools
[2012-04-26] 将app.hongs.util.Text里的数字操作部分拿出来放到app.hongs.util.Num里
[2011-09-25] 基本完成hongs-core-js的jquery重写实现, 大部分组件已可以使用
             (不支持拖拽、树搜索)
[2011-03-23] 新增UploadHelper上传助手类(使用apache的commons-fileupload)
[2011-01-25] 树模型支持搜索逐个定位(Javascript)
[2011-01-01] 表格列支持拖拽改变尺寸(Javascript)
[2010-11-21] 支持多表串联(JOIN模式)
[2010-11-12] 支持多表串联(LINK模式)
[2010-11-11] 更改配置及消息体系(Javascript)
[2010-09-20] 增加日期选择功能(Javascript)
[2010-08-15] 增加浮动块功能(Javascript)
[2010-06-30] 支持JSP扩展标记库, 与Servlet共享配置和语言对象等
[2010-05-01] 支持动作过滤, 轻松实现权限过滤...

[文件体系]

物理分层:
/
  + WEB-INF
    - conf          配置资源(常规配置/动作配置/数据库配置/自定义标签配置)
    - lang          语言资源
    - logs          运行日志(可配置)
    - tmps          临时文件(可配置)
  - xxxx            项目模块页面
  + lib             前端库(js,flash)
    + core          前端核心库
      - css         前端核心样式
      - img         前端核心图片
  - var             变化文件(如上传)

文件映射:
xxx/Class/Method.do 调用 app.xxx.action.Class.actionMethod
xxx/Class.api       调用 app.xxx.cmdlet.Class.action
URL.de              判断是否能访问该页面
name.js-conf        读取 WEB-INF/conf/name.properties 中 js.xxxx. 开头的配置
name.js-lang        读取 WEB-INF/lang/name.xx-xx.properties 中 js.xxxx. 开头的配置

框架结构:
app.hongs           框架核心库
app.hongs.Core      核心类
app.hongs.CoreConfig            核心配置类
app.hongs.CoreLanguage          核心语言类
app.hongs.CoreLogger            核心日志类
app.hongs.CoreSerially          核心缓存类(抽象序列化类)
app.hongs.HongsError            核心错误类
app.hongs.HongsException        核心异常类
app.hongs.action                控制动作库
app.hongs.action.ActionInit               动作初始化类
app.hongs.action.Action                   动作类
app.hongs.action.JsConfAction             JS配置类
app.hongs.action.jsLangAction             JS语言类
app.hongs.action.DetectAction             权限检测类
app.hongs.action.ActionFilter             权限过滤器
app.hongs.actoin.ActoinHelper             总之助手类
app.hongs.action.UploadHelper             上传助手类
app.hongs.action.annotation     动作注解库
app.hongs.action.annotation.CommitSuccess 提交失败则自动回滚
app.hongs.action.annotation.Datums        数据注入
app.hongs.action.annotation.Verify        数据验证
app.hongs.cmdlet    命令工具库
app.hongs.cmdlet.Cmdlet                   命令工具类
app.hongs.cmdlet.CmdletHelper             命令助手类
app.hongs.cmdlet.CmdletAction             命令动作类(高级应用接口)
app.hongs.db        模型库
app.hongs.db.AbstractBaseModel
app.hongs.db.AbstractTreeModel
app.hongs.db.DB
app.hongs.db.Table
app.hongs.tag       标签库
app.hongs.tag.ConfTag
app.hongs.tag.LangTag
app.hongs.tag.ActTag
app.hongs.tag.UidTag
app.hongs.util      工具库
app.hongs.util.Num
app.hongs.util.Str
app.hongs.util.JSON
app.xxx             用户模块
app.xxx.action      用户动作包
app.xxx.action.Xxxx 用户动作类
app.xxx.cmdlet      用户命令包
app.xxx.action.Xxxx 用户命令类
注: xxx 为用户模块名称, Xxxx 为用户动作/命令类名

[通用请求参数解释]

id      主键(单个)
id[]    主键(多个)
pid     上级id(树)
page    当前页码
rows    额定行数
cols[]  限定列名
sort    排序字段
find    搜索关键词

[特定请求参数规则]

字段等于        field=value, assoc_table.field=value
字段不等于      -field=value, -assoc_table.field=value
查找匹配的行    find=word1+word2, find.name=word1+word2
查找不匹配的行  -find=word1+word2, -find.name=word1+word2
排序(-表示逆序) sort=-field1+field2, sort=sub_table.field

注: "+" 在 URL 中为空格; 框架未提供 >,>=,<,<= 等条件, 因这些需求并不多, 请自行
  实现, 推荐使用 gt-,ge-,lt-,le- 作为参数前缀.
  之所以不使用后缀(如field!=value更易懂), 是因为 field,find 均可以作为数组传递
  (如field[]=value表示IN语句), 框架的解析器需要通过"."和"[]"来构建Map和List,
  用前缀方便解析(不用特殊处理[]的逻辑), 也方便过滤时做判断, 且"-"在URL中不需要
  转义.

[数据模型命名规范]

表名由"分区_模块_主题[_二级主题]"组成, 推荐最多两级主题.
分区分别为:
  a: 应用区, 存放应用数据
  b: 仓库区, 存放历史数据
  m: 市场区, 存放结果数据
  s: 缓冲区, 存放缓冲数据
字段名推荐:
  id        主键, CHAR(20)
  pid       父键, CHAR(20)
  x_id      外键, CHAR(20), x为关联表缩写
  dflag     删除标识, 0为正常, 1为删除
  ctime     创建时间, DATETIME或TIMESTAMP
  mtime     修改时间, DATETIME或TIMESTAMP
  btime     开始时间, DATETIME或TIMESTAMP
  etime     结束时间, DATETIME或TIMESTAMP

注: 因字段名可用于URL中作为过滤参数, 而部分参数已有特殊含义, 字段取名时请务必避
开这些名称: page,rows,cols,sort,find. 另外, 在Model中可以重新定义这些名称, 但并
不建议将这些参数名作为配置写入配置中.

<< HongsCORE Framework for javascript >>

请转至 https://github.com/ihongs/HongsCORE/blob/develop/web/lib/core/README.txt

<< KEEP IT SIMPLE, STUPID! >>

编写只做一件事情，并且要做好的程序；编写可以在一起工作的程序，编写处理文本流的程
序，因为这是通用的接口。这就是UNIX哲学.所有的哲学真 正的浓缩为一个铁一样的定律，
高明的工程师的神圣的“KISS 原则”无处不在。大部分隐式的UNIX哲学不是这些前辈所说
的，而是他们所做的和UNIX自身建立的例子。从整体上看，我们能够抽象出下面这些观点：

1、 模块性原则：写简单的，通过干净的接口可被连接的部件。
2、 清楚原则：清楚要比小聪明好。
3、 合并原则：设计能被其它程序连接的程序。
4、 分离原则：从机制分离从策略，从实现分离出接口。
5、 简单原则：设计要简单；只有当你需要的时候，增加复杂性。
6、 节俭原则：只有当被证实是清晰，其它什么也不做的时候，才写大的程序。
7、 透明原则：为使检查和调试明显更容易而设计。
8、 健壮性原则：健壮性是透明和简单的追随者。
9、 表现原则：把知识整理成资料，于是程序逻辑能变得易理解和精力充沛的。
10、最小意外原则：在接口设计中，总是做最小意外事情。
11、沉默原则：当一个程序令人吃惊什么也不说的时候，他应该就是什么也不说。
12、修补补救：当你必须失败的时候，尽可能快的吵闹地失败。
13、经济原则：程序员的时间是宝贵的；优先机器时间节约它。
14、产生原则：避免手工堆砌；当你可能的时候，编写可以写程序的程序。
15、优化原则：在雕琢之前先有原型；在你优化它之前，先让他可以运行。
16、差异原则：怀疑所有声称的“唯一真理“。
17、可扩展原则：为将来做设计，因为它可能比你认为来的要快。

