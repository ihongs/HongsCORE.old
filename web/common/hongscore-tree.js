
/**
 * 树型组件
 * @param {Object} opts 选项
 * @param {Element} context 容器
 */
function HsTree(opts, context) {
    context = jQuery (context);
    context.data("HsTree", this);
    context.addClass( "HsTree" );

    var loadBox  = context.closest(".loadbox");
    var treeBox  = context.find   (".treebox");
    var findBox  = context.find   (".findbox");
    var loadUrl  = hsGetValue(opts, "loadUrl");
    var openUrls = hsGetValue(opts, "openUrls");
    var sendUrls = hsGetValue(opts, "sendUrls");
    var linkUrls = hsGetValue(opts, "linkUrls");
    var loadMode = hsGetValue(opts, "loadMode", 0); // 加载模式, 0无 1附带上层数据

    // 数据的节点属性的键
    this.idKey   = hsGetValue(opts, "idKey"  , "id"  );
    this.pidKey  = hsGetValue(opts, "pidKey" , "pid" );
    this.nameKey = hsGetValue(opts, "nameKey", "name");
    this.noteKey = hsGetValue(opts, "noteKey");
    this.typeKey = hsGetValue(opts, "typeKey");
    this.cnumKey = hsGetValue(opts, "cnumKey");

    // 根节点信息
    var rootInfo = {
            id   : hsGetValue(opts, "rootId", hsGetConf("tree.root.id",  "0" )),
            name : hsGetValue(opts, "rootName", hsGetLang("tree.root.name")),
            note : hsGetValue(opts, "rootNote", hsGetLang("tree.root.note"))
        };

    this.context = context;
    this.loadBox = loadBox;
    this.treeBox = treeBox;

    if (opts) for ( var k in opts ) {
        if ('_'===k.substring(0, 1)
        ||  this[k] !== undefined ) {
            this[k]  =  opts[k];
        }
    }

    var i, a, n, m, u;
    var that = this;

    function openHand(evt) {
        //var n = evt.data[0];
        var n = jQuery(this);
        var m = evt.data[1];
        var u = evt.data[2];

        switch (m) {
            case "{CONTEXT}": m = context; break;
            case "{LOADBOX}": m = loadBox; break;
            case "{AUTOBOX}": m = n._hsTarget('@'); break;
            case "{TABSBOX}":
                m = context.closest(".panes").data("rel");
                m = m.hsTaba(context.attr("id"));
                m = m[0];
                break;
            default: m = n._hsTarget(m);
        }

        /*
        if (typeof(n) === "string")
            n = loadBox.find(n);
        else if (n)
            n = jQuery(n);
        */
        /*
        if (typeof(m) === "string")
            m = loadBox.find(m);
        else*/ if (m)
            m = jQuery(m);

        var tip = n.closest(".tooltip");
        if (tip.length)
            n   = tip.data ( "trigger");

        if (typeof(u) === "function") {
            u.call( that, n, m );
            return;
        }

        if (0 <= u.indexOf("{ID}")) {
            var sid;
            if (0 <= jQuery.inArray(treeBox[0], n.parents())) {
                sid = that.getId (n);
            }
            else {
                sid = that.getSid( );
            }
            if (sid == null) return ;

            u = u.replace ("{ID}", encodeURIComponent( sid ));
        }

        that.open( n, m, u );
    }

    if (openUrls) jQuery.each(openUrls, function(i, a) {
        switch (a.length) {
        case 3:
            n = a[0];
            u = a[1];
            m = a[2];
            break;
        case 2:
            n = a[0];
            u = a[1];
            m = undefined;
            break;
        default:
            return;
        }

        if (typeof(n) === "string")
            context.on("click", n, [n, m, u], openHand);
        else if (n)
            n.on("click", [n, m, u], openHand);
    });

    function sendHand(evt) {
        //var n = evt.data[0];
        var n = jQuery(this);
        var m = evt.data[1];
        var u = evt.data[2];

        /*
        if (typeof(n) === "string")
            n = loadBox.find(n);
        else if (n)
            n = jQuery(n);
        */

        var tip = n.closest(".tooltip");
        if (tip.length)
            n   = tip.data ( "trigger");

        if (typeof(u) === "function") {
            u.call( that, n, m );
            return;
        }

        var sid;
        if (-1 != jQuery.inArray(treeBox[0], n.parents())) {
            sid = that.getId (n);
        }
        else {
            sid = that.getSid( );
        }
        if (sid == null) return ;

        var dat = {};
        dat[that.idKey] =  sid  ;

        that.send(n, m, u, dat );
    }

    if (sendUrls) jQuery.each(sendUrls, function(i, a) {
        switch (a.length) {
        case 3:
            n = a[0];
            u = a[1];
            m = a[2];
            break;
        case 2:
            n = a[0];
            u = a[1];
            m = undefined;
            break;
        default:
            return;
        }

        if (typeof(n) === "string")
            context.on("click", n, [n, m, u], sendHand);
        else if (n)
            n.on("click", [n, m, u], sendHand);
    });

    // 当选中时, 在指定区域加载指定页面, 并附带树节点ID
    if (linkUrls) {
        treeBox.on("select", function(evt, id) {
            for (var i = 0; i < linkUrls.length; i ++) {
                jQuery(linkUrls[i][0])
               .hsLoad(linkUrls[i][1].replace('{ID}', encodeURIComponent(id)));
            }
        });
    }

    if (findBox.length) {
        findBox.on("submit", function() {
            that.find( loadUrl , this );
            return true;
        });
    }

    treeBox.on("click", ".tree-node td.tree-hand", function() {
        that.toggle(jQuery(this).closest(".tree-node"));
    });
    treeBox.on("click", ".tree-node td:not(.tree-hand)", function() {
        that.select(jQuery(this).closest(".tree-node"));
    });

    var  rootBox = jQuery('<div class="tree-node tree-root" id="tree-node-'
                  +rootInfo["id"]+'"></div>')
                  .appendTo( treeBox );
    this.fillInfo( rootInfo, rootBox );
    this.select  (     rootInfo["id"]);
    this.load(loadUrl, rootInfo["id"]);
}
HsTree.prototype = {
    load     : function(url, pid) {
        if (url ) this._url = url;
        if (pid ) this._pid = pid;
        var data = {};
        data[this.pidKey] = this._pid;
        jQuery.hsAjax({
            "url"       : this._url ,
            "data"      : data,
            "type"      : "POST",
            "dataType"  : "json",
            "action"    : "load",
            "async"     : false,
            "cache"     : false,
            "context"   : this,
            "success"   : function(rst) {
                this.loadBack(rst, pid);
            }
        });
    },
    loadBack : function(rst, pid) {
        rst = hsResponObj(rst );
        var sid = this.getSid();
        if (rst.__success__ === false) return;
        if (rst.list) this.fillList( rst.list, pid );
        this.treeBox.trigger("loadBack", [rst, pid]);
        if (this.treeBox.find("#tree-node-"+sid).length == 0) {
            this.select ( pid );
        }
    },
    fillList : function(list, pid) {
        var lst, nod, i, id;
        nod = this.treeBox.find("#tree-node-"+pid);
        lst = nod .children(".tree-list:first");
        if (list.length == 0) {
            nod.removeClass("tree-fold")
               .removeClass("tree-open");
            lst.remove();
            return;
        }
        nod.addClass("tree-open");

        if (lst.length == 0 ) {
            lst = jQuery('<div class="tree-list"></div>');
            lst.appendTo(nod);
        }

        var pid2, lst2, lsts = {}; lsts[pid] = lst;
        for(i = list.length -1; i > -1; i --) {
            id  = hsGetValue(list[i], this.idKey);
            nod = this.treeBox.find("#tree-node-"+id);
            if (nod.length == 0) {
                nod = jQuery('<div class="tree-node"></div>');
                nod.attr("id", "tree-node-"+id );
            }
            else {
                nod.find("table:first").empty( );
                pid2 = this.getPid(nod);
                lst2 = nod.closest(".tree-list");
                if (pid2 != pid) lsts[pid2] = lst2;
            }
            nod.prependTo(lst);
            this.fillInfo(list[i], nod);
        }
        for(i in lsts) {
            this.fillCnum(list.length, lsts[i]);
        }
    },
    fillInfo : function(info, nod) {
        var tab = jQuery('<table><tbody><tr>'
            + '<td class="tree-hand"><span class="caret"></span></td>'
            + '<td class="tree-name"></td>'
            + '<td class="tree-cnum"></td>'
            + '</tr></tbody></table>');
        var n, t;

        n = hsGetValue(info, this.nameKey);
        tab.find(".tree-name").text(n);
        if (typeof(this.noteKey) !== "undefined") {
            n = hsGetValue(info , this.noteKey);
            nod.find(".tree-name").attr("title", n);
        }
        if (typeof(this.typeKey) !== "undefined") {
            t = hsGetValue(info , this.typeKey);
            nod.addClass("tree-type-" + t);
        }
        if (typeof(this.cnumKey) !== "undefined") {
            n = hsGetValue(info , this.cnumKey);
            tab.find(".tree-cnum").text(n);
            if (n)
                nod.addClass("tree-fold");
        }
        else {
                nod.addClass("tree-fold");
        }

        if (! t) t = "info";
        if (typeof(this["_fill_"+t]) !== "undefined") {
            this["_fill_"+t].call(this, tab, info);
        }

        tab.prependTo(nod);
    },
    fillCnum : function(cnum, lst) {
        var nod = lst.closest (".tree-node");
        var arr = lst.children(".tree-node");

        if (typeof(cnum) === "undefined")
            cnum  = arr.length;
        if (cnum != arr.length)
            for (var i = arr.length-1; i > cnum-1; i --) {
                jQuery(arr[i]).remove();
            }

        if (cnum != 0)
            nod.find(".tree-cnum").text(cnum.toString());
        else {
            nod.find(".tree-cnum").hide();
            nod.removeClass("tree-fold")
               .removeClass("tree-open");
        }
    },

    find     : function(url, data) {
    },
    findBack : function(rst) {
    },

    send     : function(btn, msg, url, data) {
        if ( msg && !confirm(msg) ) return ;
        var dat2 = jQuery.extend({}, hsSerialObj(url), hsSerialObj(data||{}));
        jQuery.hsAjax({
            "url"       : url,
            "data"      : data,
            "type"      : "POST",
            "dataType"  : "json",
            "action"    : "send",
            "button"    : btn,
            "async"     : false,
            "cache"     : false,
            "context"   : this,
            "success"   : function(rst) {
                this.sendBack(btn, rst, dat2);
            }
        });
    },
    sendBack : function(btn, rst, data) {
        rst = hsResponObj(rst);
        if (rst.__success__ === false) return;
        var evt = new jQuery.Event("sendBack");
        btn.trigger(evt, [rst, data]);
        if (evt.isDefaultPrevented()) return;

        if (data[this.idKey ] !== undefined)
            this.load(null, this.getPid(data[this.idKey])); // 移动/删除
        if (data[this.pidKey] !== undefined)
            this.load(null, data[this.pidKey]); // 移动
    },

    open     : function(btn, box, url, data) {
        var that = this;
        var dat2 = jQuery.extend({}, hsSerialObj(url), hsSerialObj(data||{}));
        if (box == "@") box = jQuery(btn).parent(".loadbox");
        if (box)
            box.hsOpen(url, data, function( ) {
               that.openBack(btn, jQuery(this), dat2 );
            }).data("rel", btn.closest(".loadbox")[0]);
        else
              $.hsOpen(url, data, function( ) {
               that.openBack(btn, jQuery(this), dat2 );
            });
    },
    openBack : function(btn, box, data) {
        var that = this;
        btn.trigger("openBack", [box, data]);
        box.on("saveBack", function(evt,rst) {
            if (evt.isDefaultPrevented()) return;
            btn.trigger ( evt , [rst, data]);
            if (evt.isDefaultPrevented()) return;

            if (data[that.idKey] !== undefined)
                that.load(null, that.getPid(data[that.idKey])); // 修改
            else
                that.load(null, that.getSid( )); // 添加
        });
    },

    select   : function(id) {
        var nod = this.getNode(id);
            id  = this.getId (nod);
        this.treeBox.find(".tree-node")
            .removeClass ( "tree-curr");
        nod.addClass ( "tree-curr").trigger("select", [id]);
    },
    toggle   : function(id) {
        var nod = this.getNode(id);
            id  = this.getId (nod);
        var lst = nod.children(".tree-list");
        lst.toggle(); nod.trigger("toggle", [id]);
        if (lst.length == 0) this.load(null, id );
        else {
            nod.removeClass("tree-open tree-fold");
            lst.is(":visible") ? nod.addClass("tree-open")
                               : nod.addClass("tree-fold");
        }
    },

    getNode  : function(id) {
        if (typeof(id) === "object")
            return id.closest(".tree-node" );
        else
            return this.treeBox.find( "#tree-node-" + id );
    },
    getPnode : function(id) {
        return this.getNode(id).parent().closest(".tree-node");
    },
    getId    : function(id) {
        if (typeof(id) === "object")
            return this.getId(id.attr("id"));
        else
            return id.toString( ).substr(10);
    },
    getPid   : function(id) {
        return this.getId(this.getPnode(id));
    },
    getRid   : function() {
        return this.getId(this.treeBox.find(".tree-root"));
    },
    getSid   : function() {
        return this.getId(this.treeBox.find(".tree-curr"));
    }
};

jQuery.fn.hsTree = function(opts) {
    return this._hsConstr(opts, HsTree);
};

(function($) {
    $(document)
    .on("select loadBack", ".HsTree .tree-node",
    function() {
        // 当选中非根节点时, 开启工具按钮, 否则禁用相关按钮
        var box = $(this).closest(".HsTree");
        var obj =        box.data( "HsTree");
        box.find(".for-select").prop("disabled", obj.getSid()==obj.getRid());
    });
})(jQuery);
