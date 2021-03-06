
# 更新日志

* 2015/11/24 FetchCase 中组织 SQL 语句不再需要加 "." 前缀即可在关联查询中自动为字段添加当前表别名
* 2015/10/30 取消了 object.config 来初始化组件的方式, 改为 data-* 的配置方式
* 2015/10/13 建立 PackAction 用于一次请求多个 Action; 修复 SQL Like 生成语句问题; 将 FetchNext 更名为 DB.Roll, DBAssist 改回 Mview
* 2015/09/23 修复关联查询子表数据未获取的问题
* 2015/09/22 将常用的参数等集中放到 app.hongs.Cnst 类中, 这样就不用总去读配置了，此部分参数本就属于约定胜于配置的，并无配置的意义
* 2015/09/22 添加了 app.hongs.action.PickedHelper 助手类, 可以更方便的通过 ActionRunner 本地调用, 为 form 关联查询数据了
* 2015/09/20 完成模块管理功能; 更新、优化、修复其他 NullPointException, JS 等问题
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
