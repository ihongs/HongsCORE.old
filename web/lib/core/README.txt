<<HongsCORE Framework for javascript>>

文档版本: 13.01.01
软件版本: 13.02.15
设计作者: 黄弘(Kevin Hongs)
技术支持:
  Phone: +8618621523173
  Email: kevin.hongs@gmail.com

本工具集与HongsCORE(Java)配套使用, 使用jQuery作为核心辅助库, 使用jQueryTools作为UI
组件库. 以hs开头的为普通函数; 以Hs开头的为伪类函数, this指向调用的容器对象.
依赖的jQueryTools组件: tabs, overlay, tooltip, validator

[Class]

close
cancel
ensure
load-ing
load-box
open-box
note-box
list-box        用于List中, 下同
page-box
check-one
check-all
tree-box        用于Tree中, 下同
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

[ID]

note-box
node-[ID]       节点编号, 在.tree-node上

[Attr]

data-fn         用于Form和List中
data-vk         用于Form中, 下同
data-tk
data-pn         用于List中, 下同
data-pos
data-off

[Data]

url             用于Load中, 下同
data
baks            用于Open中, 下同
tabs
oldTab
curTab
overlay
trigger         用于从tip上获取trigger对象

[Event]

HsClose:
    hsClose
HsReady:
    hsReady
HsForm:
    loadBack    绑定在formBox上
    saveBack    绑定在formBox上, 默认关闭表单
HsList:
    loadBack    绑定在listBox上
    sendBack    绑定在btn上, 默认刷新列表
    openBack    绑定在btn上
    saveBack    绑定在btn上, 默认刷新列表
HsTree:
    loadBack    绑定在treeBox上
    sendBack    绑定在btn上, 默认刷新节点
    openBack    绑定在btn上
    saveBack    绑定在btn上, 默认刷新节点
    select      绑定在节点上(选中)
    taggle      绑定在节点上(开关)
