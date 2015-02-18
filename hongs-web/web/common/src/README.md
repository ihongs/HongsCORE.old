# HongsCORE framework for Javascript

文档版本: 15.02.19
软件版本: 15.02.18
设计作者: 黄弘(Kevin Hongs)
技术支持: kevin.hongs@gmail.com

本工具集与HongsCORE(Java)配套使用, 使用jQuery作为核心辅助库, 使用jQueryTools作为UI组件库, 使用Bootstrap作为UI主题库. 以hs开头的为普通函数; 以Hs开头的为伪类函数, this指向调用的容器对象.

## 环境设置

    <link rel="stylesheet" type="text/css" href="../common/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="../common/css/hongscore.min.css"/>
    <script type="text/javascript" src="../common/jquery.min.js"></script>
    <script type="text/javascript" src="../common/bootstrap.min.js"></script>
    <script type="text/javascript" src="../common/hongscore.min.js"></script>
    <script type="text/javascript" src="../common/conf/default.js"></script>
    <script type="text/javascript" src="../common/lang/default.js"></script>
    <script type="text/javascript" src="../common/auth/default.js"></script>

注: 将以上代码加入 head 中，注意 link 的 href 和 script 的 src 路.

## HsTree 树型组件的用法

    <div id="hcum_dept_tree">
        <object class="config" name="hsTree" data="">
            <param name="loadUrl" value="hcum/dept/list.act"/>
            <param name="linkUrls:0" value="['.main-context','hcum/user/list.html?dept_id={ID}']"/>
            <param name="openUrls:0" value="['.create','hcum/dept/form.html?pid={ID}','{TABSBOX}']"/>
            <param name="openUrls:1" value="['.modify','hcum/dept/form.html?id={ID}','{TABSBOX}']"/>
            <param name="sendUrls:0" value="['.delete','hcum/dept/delete.act','您确定要删除此部门?']"/>
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

    <div id="hcum-user-list">
        <object class="config" name="hsList" data="">
            <param name="loadUrl" value="('hcum/user/list.act?dept_id='+H$('&dept_id',this))"/>
            <param name="openUrls:0" value="['.create','hcum/user/form.html?dept_id='+H$('&dept_id',this),'{TABSBOX}']"/>
            <param name="openUrls:1" value="['.modify','hcum/user/form.html?id={ID}','{TABSBOX}']"/>
            <param name="sendUrls:0" value="['.delete','hcum/user/delete.act','您确定要删除此用户?']"/>
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

    <div id="hcum_user_form">
        <object class="config" name="hsForm" data="">
            <param name="loadUrl" value="hcum/user/info.act"/>
            <param name="saveUrl" value="hcum/user/save.act"/>
        </object>
        <form action="" method="POST">
            <input type="hidden" name="id"/>
            <input type="hidden" name="depts.0.dept_id" data-pn="dept_id"/>
            <div class="row">
                <div class="col-md-4">
                    <div class="form-group">
                        <label class="control-label">邮箱</label>
                        <input type="email" name="username" class="form-control" required="required" data-unique="hcum/user/unique.act?id={id}"/>
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
                        <input type="text" name="name" class="form-control" required="required" data-unique="hcum/user/unique.act?id={id}"/>
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
            $("#hcum_user_form").on("loadBack", function() {
                if ($(this).find("[name=id]").val()) {
                    $(this).find(":password").removeAttr("required");
                }
            });
        })(jQuery);
    </script>
