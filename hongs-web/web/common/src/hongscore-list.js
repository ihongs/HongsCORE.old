
/**
 * 列表组件
 * @param {Object} opts 选项
 * @param {Element} context 容器
 */
function HsList(opts, context) {
    context = jQuery (context);
    context.data("HsList", this);
    context.addClass( "HsList" );

    var loadBox  = context.closest(".loadbox");
    var listBox  = context.find   (".listbox");
    var pageBox  = context.find   (".pagebox");
    var findBox  = context.find   (".findbox");
    var loadUrl  = hsGetValue(opts, "loadUrl");
    var sendUrls = hsGetValue(opts, "sendUrls");
    var openUrls = hsGetValue(opts, "openUrls");
    var loadMode = hsGetValue(opts, "loadMode", 1); // 加载模式, 0无 1附带上层数据

    this.pageKey = hsGetValue(opts, "pageKey", hsGetConf("page.key", "page"));
    this.sortKey = hsGetValue(opts, "sortKey", hsGetConf("sort.key", "sort"));
    this.rowsPerPage = hsGetConf("rows.per.page", 20);
    this.lnksPerPage = hsGetConf("lnks.per.page", 10);

    this.context = context;
    this.loadBox = loadBox;
    this.listBox = listBox;
    this.pageBox = pageBox;

    if (opts) for ( var k in opts ) {
        if ('_'===k.substring(0, 1)
        ||  this[k] !== undefined ) {
            this[k]  =  opts[k];
        }
    }

    var that = this;
    var m, n, u, a, i;

    //** 发送服务 **/

    function sendHand(evt) {
        var n = jQuery(this);
        var m = evt.data[1];
        var u = evt.data[2];

        var t = n.closest(".tooltip");
        if (t.length) {
            n = t.data   ( "trigger");
        }
        if (typeof(u) === "function") {
            u.call(n, m, that);
            return;
        }

        var cks;
        if (-1 != jQuery.inArray(listBox[0], n.parents())) {
            cks = that.getRow(n);
        } else {
            cks = that.getAll( );
        }
        if (cks == null) return ;

        that.send(n, m, u, cks );
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

        if (typeof(n) === "string") {
            context.on("click", n, [n, m, u], sendHand);
        } else if (n) {
            n.on("click", [n, m, u], sendHand);
        }
    });

    //** 打开服务 **/

    function openHand(evt) {
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

        var t = n.closest(".tooltip");
        if (t.length) {
            n = t.data   ( "trigger");
        }
        if (typeof(u) === "function") {
            u.call(n, m, that);
            return;
        }

        if (0 <= u.indexOf("{ID}")) {
            var sid;
            if (0 <= jQuery.inArray(listBox[0], n.parents())) {
                sid = that.getRow(n);
            }
            else {
                sid = that.getOne( );
            }
            if (sid == null) return ;
            sid = sid.val( );

            u  = u.replace("{ID}", encodeURIComponent( sid ));
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

        if (typeof(n) === "string") {
            context.on("click", n, [n, m, u], openHand);
        } else if (n) {
            n.on("click", [n, m, u], openHand);
        }
    });

    //** 搜索服务 **/

    if (findBox.length) {
        findBox.on("submit", function() {
            that.load( loadUrl , this );
            return false;
        });
    }

    //** 立即加载 **/

    if (loadUrl) {
        var loadData = [];
        loadMode = parseInt(loadMode);
        if (1 === ( 1 & loadMode ) ) {
            a = hsSerialArr(loadBox.data("url" ));
            for (i = 0; i < a.length; i ++ ) {
                loadData.push(a[i] );
            }
            a = hsSerialArr(loadBox.data("data"));
            for (i = 0; i < a.length; i ++ ) {
                loadData.push(a[i] );
            }
        }
        a = hsSerialArr(loadUrl);
        for (i = 0; i < a.length; i ++ ) {
            loadData.push(a[i] );
        }
        this.load(loadUrl, loadData);
    }
}
HsList.prototype = {
    load     : function(url, data) {
        if (url ) this._url  = url;
        if (data) this._data = hsSerialArr(data);
        jQuery.hsAjax({
            "url"       : this._url ,
            "data"      : this._data,
            "type"      : "POST",
            "dataType"  : "json",
            "action"    : "load",
            "async"     : false,
            "cache"     : false,
            "context"   : this,
            "success"   : this.loadBack
        });
    },
    loadBack : function(rst) {
        rst = hsResponObj(rst);
        if (rst.ok === false) return;

        this.listBox.trigger("loadOver", [rst, this]);

        if (rst.list) this.fillList(rst.list);
        if (rst.page) this.fillPage(rst.page);

        this.listBox.trigger("loadBack", [rst, this]);
    },
    fillList : function(list) {
        var tb, tr, td, tds, cls, fns, fts, i, j, n, t, v;
        tb  = this.listBox.find("tbody"); tb.empty( );
        tds = this.listBox.find("thead th, thead td");
        cls = []; fns = []; fts = {};
        for (i = 0; i < tds .length; i ++) {
            td = jQuery(tds[i]);
            cls.push(td.attr( "class" ));
            fns.push(td.attr("data-fn"));
            if (     td.attr("data-ft")) {
                fts [td.attr("data-fn")] = td.attr("data-ft");
            }

            // 排序
            if (td.hasClass("sortable")) {
                if (td.find(".sort-ico").size() == 0) {
                    var that = this;
                    td.append('<span class="sort-ico"></span>');
                    td.click(function( ) {
                        var td = jQuery ( this );
                        var fn = td.attr("data-ob")||td.attr("data-fn");
                        var sn = "";
                        if ( td.hasClass("sort-a-z")) {
                            sn = "-"+fn;
                        } else
                        if (!td.hasClass("sort-z-a")) {
                            sn =     fn;
                        }
                        hsSetSeria(that._data,that.sortKey, sn);
                        that.load();
                    });
                }
                var sn = hsGetSeria( this._data, this.sortKey );
                var fn = td.attr("data-ob")||td.attr("data-fn");
                td.removeClass("sort-a-z sort-z-a");
                if (sn ==     fn) {
                    td.addClass("sort-a-z");
                } else
                if (sn == '-'+fn) {
                    td.addClass("sort-z-a");
                }
            }
        }
        for (i = 0; i < list.length; i ++) {
            tr = jQuery('<tr></tr>');
            tb.append(tr);

            this._info = list[i];
            for (j = 0; j < fns .length; j ++) {
                td = jQuery('<td></td>');
                td.attr("class", cls[j]);
                tr.append(td);

                n = fns[j];
                if (!n) continue;
                v = hsGetValue(list[i] , n) || "";

                // 按名称填充
                if (typeof(this["_fill_"+n]) !== "undefined") {
                    v = this["_fill_"+n].call(this, td, v, n);
                    if(!v) continue;
                } else
                // 按类型填充
                if (typeof(fts[n]) !== "undefined") {
                    t =  fts[n];
                if (typeof(this["_fill_"+t]) !== "undefined") {
                    v = this["_fill_"+t].call(this, td, v, n);
                    if(!v) continue;
                }}

                td.text(v);
            }
        }
        if (typeof(this._info) !== "undefined")
            delete this._info;
    },
    fillPage : function(page) {
        switch (page.err) {
            case 1:
                this.pageBox.empty().append('<div class="alert alert-warning">'+hsGetLang('list.empty')+'</div>');
                this.listBox.children().hide();
                return;
            case 2:
                this.pageBox.empty().append('<div class="alert alert-warning">'+hsGetLang('list.outof')+'</div>');
                this.listBox.children().hide();
                hsSetSerial(this._data,"page",1);
                this.load();
                return;
            default:
                this.listBox.children().show();
        }

        var i, p, t, pn, pmin, pmax, that = this;
        p = page.page     || 1;
        t = page.pagecount|| 1;
        pmin = p - Math.floor(this.lnksPerPage / 2);
        if (pmin < 1) pmin = 1;
        pmax = pmin + this.lnksPerPage - 1;
        if (pmax > t) pmax = t;
        pmin = pmax - this.lnksPerPage + 1;
        if (pmin < 1) pmin = 1;

        this.pageBox.empty();
        //this.pageBox.addClass("clearfix");
        var pbox = jQuery('<ul class="pagination"></ul>').appendTo(this.pageBox);
        var btns = pbox;//jQuery('<ul class="pagination pull-left" ></ul>').appendTo(this.pageBox);
        var nums = pbox;//jQuery('<ul class="pagination pull-right"></ul>').appendTo(this.pageBox);

        if (1 < p) {
            btns.append(jQuery('<li class="page-prev"><a href="javascript:;" data-pn="'+(p-1)+'" title="'+hsGetLang("list.prev.page")+'">&lsaquo;</a></li>'));
        } else {
            btns.append(jQuery('<li class="page-prev disabled"><a href="javascript:;" title="'+hsGetLang("list.prev.page")+'">&lsaquo;</a></li>'));
        }

        if (1 < pmin) {
            btns.append(jQuery('<li class="page-home"><a href="javascript:;" data-pn="'+1+'" title="'+1+'">&laquo;</a></li>'));
        } else {
            btns.append(jQuery('<li class="page-home disabled"><a href="javascript:;" title="'+1+'">&laquo;</a></li>'));
        }

        for(i = pmin; i < pmax + 1; i ++) {
            nums.append(jQuery('<li class="page-link'+(i === p ? ' active' : '')+'"><a href="javascript:;" data-pn="'+i+'" title="'+i+'">'+i+'</a></li>'));
        }

        if (t > pmax) {
            btns.append(jQuery('<li class="page-last"><a href="javascript:;" data-pn="'+t+'" title="'+t+'">&raquo;</a></li>'));
        } else {
            btns.append(jQuery('<li class="page-last disabled"><a href="javascript:;" title="'+t+'">&raquo;</a></li>'));
        }

        if (t > p) {
            btns.append(jQuery('<li class="page-next"><a href="javascript:;" data-pn="'+(p+1)+'" title="'+hsGetLang("list.next.page")+'">&rsaquo;</a></li>'));
        } else {
            btns.append(jQuery('<li class="page-next disabled"><a href="javascript:;" title="'+hsGetLang("list.next.page")+'">&rsaquo;</a></li>'));
        }

        // 页码不确定则不在末页显示为更多
        if (page.uncertain && t == pmax + 1) {
            nums.find(".page-last").addClass("lnks-next").find("a").html("&hellip;");
        }

        this.pageBox.find("[data-pn="+p+"]").addClass("page-curr");
        this.pageBox.find("[data-pn]").on("click", function( evt ) {
            hsSetSeria(that._data, that.pageKey, jQuery(this).attr("data-pn"));
            evt.preventDefault();
            that.load();
        });
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
        if (rst.ok === false ) return;

        var evt = jQuery.Event( "sendBack" );
        btn.trigger(evt , [rst, data, this]);
        if (evt.isDefaultPrevented()) return;

        this.load();
    },

    open     : function(btn, box, url, data) {
        var that = this;
        var dat2 = jQuery.extend({}, hsSerialObj(url), hsSerialObj(data||{}));
        if (box == "@") box = jQuery(btn).closest(".loadbox");
        if (box) {
            box.data( "rel", btn.closest(".loadbox").get(0) );
            box.hsOpen(url, data, function() {
               that.openBack(btn, jQuery(this), dat2);
            });
        } else {
         jQuery.hsOpen(url, data, function() {
               that.openBack(btn, jQuery(this), dat2);
            });
        }
    },
    openBack : function(btn, box, data) {
        var that = this;
        btn.trigger("openBack", [box, data, this]);

        box.on("saveBack", function(evt, rst, rel) {
            var ext = jQuery.Event( "saveBack" );
            ext.relatedTarget = evt.target;
            ext.relatedHsInst = rel /****/;
            btn.trigger(ext , [rst, data, that]);
            if (ext.isDefaultPrevented()) return;

            that.load( );
        });
    },

    getRow   : function(o) {
        return o.closest("tr,.itembox").find(".checkone")
                .filter (  ":checkbox,:radio,:hidden"   );
    },
    getAll   : function() {
        var cks = this.context.find(".checkone").filter(":checked");
        if (cks.length == 0) {
            alert(hsGetLang("list.get.all"));
            return null;
        }
        else {
            return cks ;
        }
    },
    getOne   : function() {
        var cks = this.context.find(".checkone").filter(":checked");
        if (cks.length != 1) {
            alert(hsGetLang("list.get.one"));
            return null;
        }
        else {
            return cks ;
        }
    },

    // /** 填充函数 **/

    _fill__admin : function(td, v, n) {
        var th = this.listBox.find('thead [data-fn="'+n+'"]');
        td.append(th.find(".invisible").clone().removeClass("invisible")).hsInit();
        return false;
    },
    _fill__check : function(td, v, n) {
        jQuery('<input type="checkbox" class="input-checkbox checkone"/>')
            .attr("name", n).val(v)
            .appendTo(td);
        return false;
    },
    _fill__radio : function(td, v, n) {
        jQuery('<input type="radio" class="input-radio checkone"/>')
            .attr("name", n).val(v)
            .appendTo(td);
        return false;
    },
    _fill__htime : function(td, v, n) {
        var d1  =  new Date ();
        var d2  =  hsPrsDate(v, hsGetLang("datetime.format"));
        if (d1.getFullYear() == d2.getFullYear()
        &&  d1.getMonth() == d2.getMonth()
        &&  d1.getDate( ) == d2.getDate()) {
            return hsGetLang("time.today", [
                   hsFmtDate(v, hsGetLang("time.format"))
            ]);
        }
        else {
            return hsFmtDate(v, hsGetLang("date.format"));
        }
    },
    _fill__hdate : function(td, v, n) {
        var d1  =  new Date ();
        var d2  =  hsPrsDate(v, hsGetLang("date.format"));
        if (d1.getFullYear() == d2.getFullYear()
        &&  d1.getMonth() == d2.getMonth()
        &&  d1.getDate( ) == d2.getDate()) {
            return hsGetLang("date.today");
        }
        else {
            return hsFmtDate(v, hsGetLang("date.format"));
        }
    },
    _fill__datetime : function(td, v, n) {
        return hsFmtDate(v, hsGetLang("datetime.format"));
    },
    _fill__date : function(td, v, n) {
        return hsFmtDate(v, hsGetLang("date.format"));
    },
    _fill__time : function(td, v, n) {
        return hsFmtDate(v, hsGetLang("time.format"));
    },
    _fill__html : function(td, v, n) {
        td.html(v); return false;
    }
};

/**
 * 这是比 fillList 更简单的卡片式展现方式
 * 需替代 fillList 时, 在初始化参数中加入
 * fillList: hsListFillItem
 * @param {array} list 返回的列表数据
 */
function hsListFillItem(list) {
    var that = this;
    var cb = this.context.find(".itembox:hidden:first");
    var tr, td, n, t, v ;
    this.listBox.empty();
    for(var i = 0; i < list.length; i ++ ) {
        this._info = list[i];
        tr = cb.clone();
        tr.appendTo(this.listBox);
        tr.find("[data-fn]").each(function() {
            td= jQuery(this);
            n = td.attr("data-fn");
            t = td.attr("data-ft");
            v = hsGetValue(list[i], n) || "";

            // 按名字填充
            if (typeof(that["_fill_"+n]) !== "undefined") {
                v = that["_fill_"+n].call(that, td, v, n);
                if (!v && (v !== 0 || v !== "")) return;
            } else
            // 按类型填充
            if (typeof(that["_fill_"+t]) !== "undefined") {
                v = that["_fill_"+t].call(that, td, v, n);
                if (!v && (v !== 0 || v !== "")) return;
            }
            // 无值不处理
            if (!v && (v !== 0 || v !== "")) {
                return;
            }

            if (td.is("input,select,textarea")) {
                td.val (v);
            } else {
                td.text(v);
            }
        }).show();
    }
    if (typeof(this._info) !== "undefined")
        delete this._info;
}

jQuery.fn.hsList = function(opts) {
    return this._hsConstr(opts, HsList);
};

(function($) {
    $(document)
    .on("click", ".listbox tbody td",
    function(evt) {
        // 当点击表格列时单选
        // 工具按钮有三类, 打开|打开选中|发送选中
        // 打开选中只能选一行, 发送选中可以选多行
        // 但是复选框太小不方便操作, 故让点击单元格即单选该行, 方便操作
        if (this != evt.target) return;
        var tr = $(this).closest("tr");
        var ck = tr.find(".checkone" );
        if (this !=  ck .closest("td"))
            tr.closest("tbody").find(".checkone").not(ck )
                               .prop( "checked"  , false );
        ck.prop("checked", ! ck.prop( "checked")).change();
    })
    .on("change", ".HsList .checkone",
    function() {
        var box = $(this ).closest(".HsList" );
        var siz = box.find(".checkone").length;
        var len = box.find(".checkone:checked").length;
        box.find(".for-select").prop("disabled", len != 1);
        box.find(".for-checks").prop("disabled", len == 0);
        box.find(".checkall"  ).prop("choosed" , siz && siz==len ? true : (len && siz!=len ? null : false));
    })
    .on("change", ".HsList .checkall",
    function() {
        this.indeterminate = false;
        var box = $(this ).closest(".HsList" );
        var ckd = $(this/**/).prop("checked" );
        box.find(".checkone").prop("checked", ckd).trigger("change");
    })
    .on("loadBack", ".HsList .listbox",
    function() {
        var box = $(this ).closest(".HsList" );
        box.find(".for-select,.for-checks").prop("disabled" , true );
        box.find(".checkone" ).change(/**/);
    });
})(jQuery);
