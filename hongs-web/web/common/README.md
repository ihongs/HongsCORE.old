# HongsCORE framework for Javascript

* 文档版本: 15.02.19
* 软件版本: 15.02.18
* 设计作者: 黄弘(Kevin Hongs)
* 技术支持: kevin.hongs@gmail.com

本工具集与 HongsCORE(Java) 配套使用, 使用 jQuery 作为核心辅助库, 使用 Bootstrap 作为 UI 库. 源码为 src 下以 hongs- 开头的文件, 以 hs 开头的为普通函数, 以 Hs 开头的为伪类函数, this 指向调用的容器对象.

data 属性缩写:

    Form:
        data-fn     Field name
        data-ft     Field type
        data-pn     Param name
        data-vk     Value key
        data-tk     Text key
        data-lk     List key
        data-dk     Disp key
    List:
        data-fn     Field name
        data-ft     Field type
        data-pn     Page num
        data-ob     Order by
    Pick:
        data-tn     Table name

其他非缩写 data 属性通常可按字面意思理解, data-toggle,data-target 等为 bootstrap 定义的.

## 环境加载

    <base href="../">
    <link rel="stylesheet" type="text/css" href="common/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="common/css/hongscore.min.css"/>
    <script type="text/javascript" src="common/jquery.min.js"></script>
    <script type="text/javascript" src="common/bootstrap.min.js"></script>
    <script type="text/javascript" src="common/hongscore.min.js"></script>
    <script type="text/javascript" src="common/conf/default.js"></script>
    <script type="text/javascript" src="common/lang/default.js"></script>
    <script type="text/javascript" src="common/auth/default.js"></script>

注: 将以上代码加入 head 中, 注意 link 的 href 和 script 的 src 路径; 这里用 base 定义了基础路径, 以下的相对路径均基于此.

## HsTree 树型组件的用法

    <div id="member_dept_tree">
        <object class="config" name="hsTree" data="">
            <param name="loadUrl" value="hongs/member/dept/list.act"/>
            <param name="linkUrls#0" value="['.main-context','hongs/member/user/list.html?dept_id={ID}']"/>
            <param name="openUrls#0" value="['.create','hongs/member/dept/form.html?pid={ID}','{TABSBOX}']"/>
            <param name="openUrls#1" value="['.modify','hongs/member/dept/form.html?id={ID}','{TABSBOX}']"/>
            <param name="sendUrls#0" value="['.delete','hongs/member/dept/delete.act','您确定要删除此部门?']"/>
            <param name="rootName" value="组织架构"/>
        </object>
        <div class="toolbox btn-group">
            <button type="button" class="create btn btn-default">添加部门</button>
            <button type="button" class="modify for-select btn btn-default">修改</button>
            <button type="button" class="delete for-select btn btn-danger" >删除</button>
        </div>
        <div class="treebox"></div>
    </div>

## HsList 列表组件的用法

    <div id="member_user_list">
        <object class="config" name="hsList" data="">
            <param name="loadUrl" value="('hongs/member/user/list.act?dept_id='+H$('&dept_id',this))"/>
            <param name="openUrls#0" value="['.create','hongs/member/user/form.html?dept_id='+H$('&dept_id',this),'{TABSBOX}']"/>
            <param name="openUrls#1" value="['.modify','hongs/member/user/form.html?id={ID}','{TABSBOX}']"/>
            <param name="sendUrls#0" value="['.delete','hongs/member/user/delete.act','您确定要删除此用户?']"/>
        </object>
        <div>
            <div class="toolbox col-md-9 btn-group">
                <button type="button" class="create btn btn-default">创建用户</button>
                <button type="button" class="modify for-select btn btn-default">修改</button>
                <button type="button" class="delete for-checks btn btn-danger" >删除</button>
            </div>
            <form class="findbox col-md-3 input-group" action="" method="POST">
                <input type="search" name="wd" class="form-control input-search"/>
                <span class="input-group-btn">
                    <button type="submit" class="btn btn-default">查找</button>
                </span>
            </form>
        </div>
        <div class="listbox table-responsive">
            <table class="table table-hover table-striped">
                <thead>
                    <tr>
                        <th data-fn="id[]" data-ft="_check" class="_check">
                            <input type="checkbox" class="checkall" name="id[]"/>
                        </th>
                        <th data-fn="name" class="sortable">名称</th>
                        <th data-fn="username" class="sortable">账号</th>
                        <th data-fn="mtime" data-ft="_htime" class="_htime sortable">修改时间</th>
                        <th data-fn="ctime" data-ft="_htime" class="_htime sortable">创建时间</th>
                    </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
        <div class="pagebox"></div>
    </div>

## HsForm 表单组件的用法

    <div id="member_user_form">
        <object class="config" name="hsForm" data="">
            <param name="loadUrl" value="hongs/member/user/info.act"/>
            <param name="saveUrl" value="hongs/member/user/save.act"/>
        </object>
        <form action="" method="POST">
            <input type="hidden" name="id"/>
            <input type="hidden" name="depts.0.dept_id" data-pn="dept_id"/>
            <div class="row">
                <div class="col-md-4">
                    <div class="form-group">
                        <label class="control-label">邮箱</label>
                        <input type="email" name="username" class="form-control" required="required" data-unique="hongs/member/user/unique.act?id={id}"/>
                    </div>
                    <div class="form-group">
                        <label class="control-label">口令</label>
                        <input type="password" name="password" class="form-control" required="required" data-relate="passcode"/>
                    </div>
                    <div class="form-group">
                        <label class="control-label">重复口令</label>
                        <input type="password" name="passcode" class="form-control" required="required" data-repeat="password" data-message="请重复输入口令" placeholder="请重复输入口令"/>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="form-group">
                        <label class="control-label">昵称</label>
                        <input type="text" name="name" class="form-control" required="required" data-unique="hongs/member/user/unique.act?id={id}"/>
                    </div>
                    <div class="form-group">
                        <label class="control-label">备注</label>
                        <textarea name="note" class="form-control"></textarea>
                    </div>
                </div>
            </div>
            <div>
            <button type="submit" class="ensure btn btn-primary">提交</button>
            <button type="button" class="cancel btn btn-link">取消</button>
            </div>
        </form>
    </div>
    <script type="text/javascript">
        (function($) {
            $("#member_user_form").on("loadBack", function() {
                if ($(this).find("[name=id]").val()) {
                    $(this).find(":password").removeAttr("required");
                }
            });
        })(jQuery);
    </script>
