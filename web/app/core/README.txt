<<HongsCORE Framework for javascript>>

文档版本: 13.01.01
软件版本: 13.02.15
设计作者: 黄弘(Kevin Hongs)
技术支持:
  Phone: +8618621523173
  Email: kevin.hongs@gmail.com

本工具集与HongsCORE(Java)配套使用, 使用jQuery作为核心辅助库, 使用jQueryTools作为UI
组件库, 使用Bootstrap作为UI主题库. 以hs开头的为普通函数, 以Hs开头的为伪类函数,
this指向调用的容器对象.
依赖的jQueryTools组件: tabs, overlay, tooltip, validator, dateinput, expose
依赖的Bootstrap组件: .btn, .alert, .tooltip, .popover, .pagination,
                     .dropdown, .arrow, .caret, .close, .active,
                     .fade, .[in|out], .[position], .has-error,
                     // 其他场合使用:
                     .container, .row, .col, .nav, .navbar,
                     .table, .form, .input, .label, .badge

[ID]

note-box        全局消息
node-[ID]       节点编号, 在.tree-node上

[Class]

open            在浮窗中打开href对应的页
close           关闭浮窗的图标按钮
cancel          取消表单并关闭浮窗或区域的按钮
ensure          提交表单的按钮
load-ing
load-box
open-box
note-box
list-box        用于HsList中, 下同
page-box
check-one
check-all
tree-box        用于HsTree中, 下同
tree-list
tree-node
tree-root
tree-curr
tree-opened
tree-folded
tree-hand       节点开关图标
tree-name       节点名称
tree-cnum       节点子级数量
node-[TP]       节点类型, 在.tree-node上

[Attr]

data-fn         HsForm和HsList中的field-name
data-pn         HsForm中的param-name, HsList中的page-num
data-vk         HsForm中的value-key
data-tk         HsForm中的text-key
data-ft         HsList中的field-type
data-eval       执行JS, this指向当前节点
data-load       在当前节点中加载, 属性值为url
data-open       在当前节点中打开, 属性值为url
data-load-in    在指定区域中加载, 属性值为selector, 由href指定url
data-open-in    在指定区域中打开, 属性值为selector, 由href指定url
data-repeat     重复输入, 属性值为input-name
data-unique     限制唯一, 属性值为url, url中可用{input-name}指定其他表单项的值
data-toggle     关联触发, 可以为: overlay,alert,modal,tooltip,overlay,dropdown
data-placement  tooltip的相对位置, 参阅bootstrip的tooltip

[Data]

url             用于HsLoad中, 下同
data
baks            用于JsOpen中, 下同
tabs
oldTab          之前选中的Tab对象
curTab          现在选中的Tab对象
overlay
trigger         用于从tip上获取trigger对象

[Event]

HsReady:
    hsReady
HsClose:
    hsClose
HsForm:
    loadBack    绑定在formBox上
    loadError   绑定在formBox上, 加载错误触发, 参数同ajaxError
    saveBack    绑定在formBox上, 默认关闭表单
    saveFail    绑定在formBox上, 提交失败触发
HsList:
    loadBack    绑定在listBox上
    loadError   绑定在listBox上, 加载错误触发, 参数同ajaxError
    sendBack    绑定在btn上, 默认刷新列表
    sendError   绑定在btn上, 发送错误触发, 参数同ajaxError
    openBack    绑定在btn上
    saveBack    绑定在btn上, 默认刷新列表
HsTree:
    loadBack    绑定在treeBox上
    loadError   绑定在treeBox上, 加载错误触发, 参数同ajaxError
    sendBack    绑定在btn上, 默认刷新节点
    sendError   绑定在btn上, 发送错误触发, 参数同ajaxError
    openBack    绑定在btn上
    saveBack    绑定在btn上, 默认刷新节点
    select      绑定在节点上(选中)
    taggle      绑定在节点上(开关)
