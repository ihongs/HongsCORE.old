<<HongsCORE Framework for javascript>>

文档版本: 13.01.01
软件版本: 14.01.20
设计作者: 黄弘(Kevin Hongs)
技术支持:
  Phone: +8618621523173
  Email: kevin.hongs@gmail.com

本工具集与HongsCORE(Java)配套使用, 使用jQuery作为核心辅助库, 使用jQueryTools作为UI组件库, 使用Bootstrap作为UI主题库. 以hs开头的为普通函数; 以Hs开头的为伪类函数, this指向调用的容器对象.

#### 环境设置 ####

<link rel="stylesheet" type="text/css" href="../common/css/bootstrap.css"/>
<link rel="stylesheet" type="text/css" href="../common/css/hongscore.css"/>
<script type="text/javascript" src="../common/jquery.min.js"></script>
<script type="text/javascript" src="../common/bootstrap.min.js"></script>
<script type="text/javascript" src="../common/conf/default.js"></script>
<script type="text/javascript" src="../common/lang/default.js"></script>
<script type="text/javascript" src="../common/auth/default.js"></script>
<script type="text/javascript" src="../common/hongscore.js"></script>
<script type="text/javascript" src="../common/hongscore-form.js"></script>
<script type="text/javascript" src="../common/hongscore-list.js"></script>
<script type="text/javascript" src="../common/hongscore-tree.js"></script>
<script type="text/javascript" src="../common/hongscore-pick.js"></script>

注: 将以上代码加入 head 中，注意 link 的 href 和 script 的 src 路. common/auth 为框架提供的权限列表.

#### HsTree 树型组件的用法 ####

<div id="hcum-dept-tree">
    <object class="config" name="hsTree" data="">
        <param name="loadUrl" value="hcum/Dept/Tree.act"/>
        <param name="linkUrls[]" value="['.main-context','hcum/user/list.html?dept_id={ID}']"/>
        <param name="openUrls[]" value="['.create','hcum/dept/form.html?pid={ID}','#hcum-user-pane']"/>
        <param name="openUrls[]" value="['.modify','hcum/dept/form.html?id={ID}','.#hcum-user-pane']"/>
        <param name="sendUrls[]" value="['.remove','hcum/Dept/Remove.act']"/>
    </object>
    <div class="tool-box btn-group">
        <button  type="button" class="create btn btn-default">添加部门</button>
        <button  type="button" class="modify for-select btn btn-default">修改</button>
        <button  type="button" class="remove for-select btn btn-danger" >删除</button>
    </div>
    <div class="tree-box"></div>
</div>

#### HsList 列表组件的用法 ####

<div id="hcum-user-list">
    <object class="config" name="hsList" data="">
        <param name="loadUrl" value="('hcum/User/List.act?dept_id='+H$('&dept_id',this))"/><!-- 列表数据加载动作地址 -->
        <param name="openUrls[]" value="['.create','hcum/user/form.html?dept_id='+H$('&dept_id',this),'#hcum-user-pane']"/><!-- 点击第1个参数的按钮, 打开第2参数的URL, 在第3个参数的区域中, 如果没有第3个参数则在浮窗中打开  -->
        <param name="openUrls[]" value="['.modify','hcum/user/form.html?id={ID}','#hcum-user-pane']"/><!-- 同上, 此为修改, 故传递了 ID 参数, ID 参数会从列表选中的行中提取 -->
        <param name="sendUrls[]" value="['.remove','hcum/User/Remove.act']"/><!-- 点击第1个参数的按钮, 将选中的行 ID 发送到第2个参数的URL -->
    </object>
    <div class="clearfix">
        <form class="toolbox btn-group pull-left">
            <button type="button" class="create btn btn-default">创建用户</button>
            <button type="button" class="modify for-select btn btn-default">修改</button>
            <button type="button" class="remove for-checks btn btn-danger" >删除</button>
        </form>
        <form class="findbox btn-group pull-right" action="" method="POST">
            <input type="search" name="wd" class="form-control input-search"/>
            <button type="submit" class="btn btn-default">查找</button>
        </form>
    </div>
    <div class="list-box">
        <table class="table table-hover table-striped">
            <thead>
                <tr>
                    <th data-fn="id[]" data-ft="_check" class="_check"><!-- 使用 data-fn 提取行数据的指定值, 使用 data-ft 指定填充的类型 -->
                        <input type="checkbox" class="checkall" name="id[]"/>
                    </th>
                    <th data-fn="username" class="sortable">邮箱</th><!-- class 为 sortable 的列可以在点击表头该列是排序 -->
                    <th data-fn="name" class="sortable">昵称</th>
                    <th data-fn="mtime" data-ft="_htime" class="_htime sortable">修改时间</th>
                    <th data-fn="ctime" data-ft="_htime" class="_htime sortable">创建时间</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
    <div class="page-box"></div>
</div>

#### HsForm 表单组件的用法 ####

<div id="hcum-user-form">
    <object class="config" name="hsForm" data="">
        <param name="loadUrl" value="hcum/User/Info.act"/><!-- 表单数据加载动作地址 -->
        <param name="saveUrl" value="hcum/User/Save.act"/><!-- 表单数据保存动作地址 -->
    </object>
    <form action="" method="POST">
        <input type="hidden" name="id"/>
        <input type="hidden" name="a_hcum_user_dept.0.dept_id" data-pn="dept_id"/><!-- 通过 data-pn 在打开此表单的参数中提取值 -->
        <div class="form-group">
            <label class="control-label">邮箱</label>
            <input type="email" name="username" class="form-control" required="required" data-unique="hcum/User/Unique.act?id={id}"/>
        </div>
        <div class="form-group">
            <label class="control-label">口令</label>
            <input type="password" name="password" class="form-control" data-validate="validate"/>
        </div>
        <div class="form-group">
            <label class="control-label">重复口令</label>
            <input type="password" name="password2" class="form-control" data-repeat="password" data-message="请重复输入口令" placeholder="请重复输入口令"/>
        </div>
        <div class="form-group">
            <label class="control-label">昵称</label>
            <input type="text" name="name" class="form-control" required="required" data-unique="hcum/User/Unique.act?id={id}"/>
        </div>
        <div class="form-group">
            <label class="control-label">备注</label>
            <textarea name="note" class="form-control"></textarea>
        </div>
        <div>
            <button type="submit" class="ensure btn btn-primary">提交</button>
            <button type="button" class="cancel btn btn-link">取消</button>
        </div>
    </form>
</div>
