
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
    var openUrls = hsGetValue(opts, "openUrls");
    var sendUrls = hsGetValue(opts, "sendUrls");

    this.sortKey = hsGetValue(opts, "sortKey", hsGetConf("model.sort.key", "sort"));
    this.pageKey = hsGetValue(opts, "pageKey", hsGetConf("model.page.key", "page"));
    this.firstOfPage = hsGetConf("first.of.page", 1 );
    this.rowsPerPage = hsGetConf("rows.per.page", 25);

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
            case "{OPENBOX}": m = _hsTarget(n, '%'); break;
            default: m = _hsTarget(n, m);
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

        var t = n.closest(".tooltip");
        if (t.length)
            n = t.data   ( "trigger");

        if (typeof(u) === "function") {
            u.call(n, m, that);
            return;
        }

        if (0 <= u.indexOf("{ID}")) {
            var cks;
            if (0 <= jQuery.inArray(listBox[0], n.parents())) {
                cks = that.getRow(n);
            }
            else {
                cks = that.getOne( );
            }
            if (cks == null) return ;
            var sid = cks.val();

            u = u.replace ("{ID}", encodeURIComponent( sid ));
        }

        that.open( n, m, u );
    }

    if (openUrls)
    for(i = 0; i < openUrls.length; i ++) {
        a = openUrls[i]; m = undefined;
        switch (a.length) {
        case 3:
            n = a[0];
            u = a[1];
            m = a[2];
            break;
        case 2:
            n = a[0];
            u = a[1];
            break;
        default:
            continue;
        }

        if (typeof(n) === "string")
            context.on("click", n, [n, m, u], openHand);
        else if (n)
            n.on("click", [n, m, u], openHand);
    }

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

        var t = n.closest(".tooltip");
        if (t.length)
            n = t.data   ( "trigger");

        if (typeof(u) === "function") {
            u.call(n, m, that);
            return;
        }

        var cks;
        if (-1 != jQuery.inArray(listBox[0], n.parents())) {
            cks = that.getRow(n);
        }
        else {
            cks = that.getAll( );
        }
        if (cks == null) return ;

        that.send(n, m, u, cks );
    }

    if (sendUrls)
    for(i = 0; i < sendUrls.length; i ++) {
        a = sendUrls[i]; m = undefined;
        switch (a.length) {
        case 3:
            n = a[0];
            u = a[1];
            m = a[2];
            break;
        case 2:
            n = a[0];
            u = a[1];
            break;
        default:
            continue;
        }

        if (typeof(n) === "string")
            context.on("click", n, [n, m, u], sendHand);
        else if (n)
            n.on("click", [n, m, u], sendHand);
    }

    if (findBox.length) {
        findBox.on("submit", function() {
            that.load( loadUrl , this );
            return false;
        });
    }

    if (loadUrl) this.load(loadUrl, hsSerialArr(loadUrl));
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
        if (rst.__success__ === false) return;
        if (rst.list) this.fillList( rst.list );
        if (rst.page) this.fillPage( rst.page );
        this.listBox.trigger("loadBack", [rst]);
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
                if (td.find(".caret").size() == 0) {
                    td.append('<span class="caret"></span>');
                    var that = this;
                    td.click(function( ) {
                        var td = jQuery(this);
                        var fn = td.attr("data-fn");
                        var sn = "";
                        if (td.hasClass("sort-a-z")) {
                            sn = "-"+fn;
                        } else
                        if (td.hasClass("sort-z-a") == false) {
                            sn = fn;
                        }
                        hsSetSeria(that._data, that.sortKey, sn);
                        that.load();
                    });
                }

                var sn = hsGetSeria(this._data, this.sortKey);
                var fn = td.attr("data-fn");
                td.removeClass("sort-a-z sort-z-a");
                if (sn == fn) {
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
                v = hsGetValue(list[i], n);

                if (typeof(this["_fill_"+n]) !== "undefined") {
                    v = this["_fill_"+n].call(this, td, v, n);
                    if(!v) continue;
                }
                // 按类型填充
                else if (typeof(fts[n]) !== "undefined") {
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
        switch (page.errno) {
            case 1:
                this.pageBox.empty().append('<div class="alert alert-warning">'+hsGetLang('list.empty')+'</div>');
                this.listBox.hide();
                return;
            case 2:
                this.pageBox.empty().append('<div class="alert alert-warning">'+hsGetLang('list.outof')+'</div>');
                this.listBox.hide();
                hsSetSerial(this._data, "page", page.total_pages);
                this.load();
                return;
            default:
                this.listBox.show();
        }

        var i, p, t, pn, pmin, pmax, that = this;
        p  = page.page || this.firstOfPage;
        t  = page.total_pages || 1 ;
        pn = this.pageBox.attr("data-pn" );
        pn = pn ? parseInt(pn) : 10;
        pmin = Math.floor((p - 1) / pn) * pn + 1;
        pmax = pmin+pn - 1; if (t<pmax) pmax = t;

        this.pageBox.empty();
        var btns = jQuery('<ul class="fl pagination"></ul>').appendTo(this.pageBox);
        var nums = jQuery('<ul class="fr pagination"></ul>').appendTo(this.pageBox);
        var rows = jQuery('<select class="fr"></select>');
        jQuery('<div class="cb"></div>').appendTo(this.pageBox);

        if (1 != p) {
            btns.append(jQuery('<li><a href="javascript:;" data-pn="'+(p-1)+'">'+hsGetLang("list.prev.page")+'</a></li>'));
        } else {
            btns.append(jQuery('<li class="disabled"><a href="javascript:;">'+hsGetLang("list.prev.page")+'</a></li>'));
        }
        if (t != p) {
            btns.append(jQuery('<li><a href="javascript:;" data-pn="'+(p+1)+'">'+hsGetLang("list.next.page")+'</a></li>'));
        } else {
            btns.append(jQuery('<li class="disabled"><a href="javascript:;">'+hsGetLang("list.next.page")+'</a></li>'));
        }
        if (1 < pmin-1) {
            nums.append(jQuery('<li><a href="javascript:;" data-pn="'+1+'">'+1+'</a></li>'));
            nums.append(jQuery('<li class="disabled" ><a href="javascript:;">...</a></li>'));
            nums.append(jQuery('<li><a href="javascript:;" data-pn="'+(pmin-1)+'">'+(pmin-1)+'</a></li>'));
        }
        for(i = pmin; i < pmax + 1; i ++) { var cl = i == p ? ' class="active"' : '';
            nums.append(jQuery('<li'+cl+'><a href="javascript:;" data-pn="'+i+'">'+i+'</a></li>'));
        }
        if (t > pmax+1) {
            nums.append(jQuery('<li><a href="javascript:;" data-pn="'+(pmax+1)+'">'+(pmax+1)+'</a></li>'));
            nums.append(jQuery('<li class="disabled" ><a href="javascript:;">...</a></li>'));
            nums.append(jQuery('<li><a href="javascript:;" data-pn="'+t+'">'+t+'</a></li>'));
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
        if (rst.__success__ === false) return;
        var evt = new jQuery.Event("sendBack");
        btn.trigger(evt, [rst, data]);
        if (evt.isDefaultPrevented()) return;
        this.load();
    },

    open     : function(btn, box, url, data) {
        var that = this;
        var dat2 = jQuery.extend({}, hsSerialObj(url), hsSerialObj(data||{}));
        if (box == "@") box = jQuery(btn).closest(".loadbox");
        if (box)
            box.hsOpen(url, data, function() {
               that.openBack(btn, box, dat2);
            }).data("rel", btn.closest(".loadbox")[0]);
        else
              $.hsOpen(url, data, function() {
               that.openBack(btn, box, dat2);
            });
    },
    openBack : function(btn, box, data) {
        var that = this;
        btn.trigger("openBack", [box, data]);
        box.on("saveBack", function(evt) {
            if(evt.isDefaultPrevented()) return;
            btn.trigger ( evt , [box, data]);
            if(evt.isDefaultPrevented()) return;
            that.load();
        });
    },

    getRow   : function(o) {
        return o.parents("tr").find(".check-one").filter(":checkbox,:radio,:hidden");
    },
    getAll   : function() {
        var cks = this.context.find(".check-one").filter(":checked");
        if (cks.length == 0) {
            alert(hsGetLang("list.get.all"));
            return null;
        }
        else {
            return cks ;
        }
    },
    getOne   : function() {
        var cks = this.context.find(".check-one").filter(":checked");
        if (cks.length != 1) {
            alert(hsGetLang("list.get.one"));
            return null;
        }
        else {
            return cks ;
        }
    },

    // /** 填充函数 **/

    _fill__check : function(td, v, n) {
        jQuery('<input type="checkbox" class="input-checkbox check-one"/>')
            .attr("name", n).val(v)
            .appendTo(td);
        return false;
    },
    _fill__radio : function(td, v, n) {
        jQuery('<input type="radio" class="input-radio check-one"/>')
            .attr("name", n).val(v)
            .appendTo(td);
        return false;
    },
    _fill__admin : function(td, v, n) {
        var th = this.listBox.find('thead [data-fn="'+n+'"]');
        td.append(th.find(".vh").clone( ).removeClass("vh")).hsInit();
        return false;
    },
    _fill__hdate : function(td, v, n) {
        var d1  =  new Date ();
        var d2  =  hsPrsDate(v, hsGetLang("date.format"));
        if (d1.getYear()  == d2.getYear()
        &&  d1.getMonth() == d2.getMonth()
        &&  d1.getDate()  == d2.getDate()) {
            return hsGetLang("date.today");
        }
        else {
            return hsFmtDate(v, hsGetLang("date.format"));
        }
    },
    _fill__htime : function(td, v, n) {
        var d1  =  new Date ();
        var d2  =  hsPrsDate(v, hsGetLang("datetime.format"));
        if (d1.getYear()  == d2.getYear()
        &&  d1.getMonth() == d2.getMonth()
        &&  d1.getDate()  == d2.getDate()) {
            return hsGetLang("time.today", {
            time : hsFmtDate(v, hsGetLang("time.format")) } );
        }
        else {
            return hsFmtDate(v, hsGetLang("datetime.format"));
        }
    }
};

jQuery.fn.hsList = function(opts) {
    return _hsConstr( this, opts, HsList );
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
        var ck = tr.find(".check-one");
        if (this !=  ck .closest("td"))
            tr.closest("tbody").find(".check-one").not(ck)
                               .prop( "checked"  , false );
        ck.prop("checked", ! ck.prop( "checked")).change();
    })
    .on("change", ".HsList .check-one",
    function() {
        var box = $(this).closest(".HsList");
        var siz = box.find(".check-one").length;
        var len = box.find(".check-one:checked").length;
        box.find(".check-all" ).prop("checked" , siz && siz == len );
        box.find(".for-select").prop("disabled", len != 1);
        box.find(".for-checks").prop("disabled", len == 0);
    })
    .on("change", ".HsList .check-all",
    function() {
        var box = $(this).closest(".HsList");
        var ckd = $(this).prop   ("checked");
        box.find(".check-one").prop("checked",ckd).trigger("change");
    })
    .on("loadBack", ".HsList .listbox",
    function() {
        var box = $(this).closest(".HsList");
        box.find(".check-all").prop("checked", false);
        box.find(".for-select,.for-checks").prop("disabled", true);
    });
})(jQuery);
