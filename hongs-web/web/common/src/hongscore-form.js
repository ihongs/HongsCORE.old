
/**
 * 表单组件
 * @param {Object} opts 选项
 * @param {Element} context 容器
 */
function HsForm(opts, context) {
    context = jQuery (context);
    context.data("HsForm", this);
    context.addClass( "HsForm" );

    var loadBox  = context.closest(".loadbox");
    var formBox  = context.find   ( "form"   );
    var loadUrl  = hsGetValue(opts, "loadUrl");
    var saveUrl  = hsGetValue(opts, "saveUrl");
    var idKey    = hsGetValue(opts, "idKey", "id"); // id参数名, 用于判断编辑还是创建
    var mdKey    = hsGetValue(opts, "mdKey", "md"); // md参数名, 用于判断是否要枚举表

    if (!formBox.length) formBox = context;

    this.context = context;
    this.loadBox = loadBox;
    this.formBox = formBox;
    this._url  = "";
    this._data = [];

    if (opts) for ( var k in opts ) {
        if ('_'===k.substring(0,1 )
        ||  this[k] !== undefined ) {
            this[k]  =  opts[k];
        } else
        if ('$'===k.substring(0,1)) {
            this.rules[k.substring(1)] = opts[k];
        } else
        if (':'===k.substring(0,1)) {
            this.rmsgs[k.substring(1)] = opts[k];
        }
    }

    var loadArr = hsSerialArr(loadBox);
    if (loadUrl) {
        loadUrl = hsFixPms(loadUrl, loadArr);
    }
    if (saveUrl) {
        saveUrl = hsFixPms(saveUrl, loadArr);
    }

    /**
     * 使用初始化数据填充表单
     * 在打开表单窗口时, 可能指定一些参数(如父ID, 初始选中项等)
     * 这时有必要将这些参数值填写入对应的表单项, 方便初始化过程
     */
    var i , n, v;
    for(i = 0; i < loadArr.length; i++) {
        n = loadArr[i].name ;
        v = loadArr[i].value;
        if ( n === idKey && v === "0" ) continue;
        formBox.find("[name='"   +n+"']").val(v);
        formBox.find("[data-fn='"+n+"']").val(v);
        formBox.find("[data-pn='"+n+"']").val(v);
    }

    /**
     * 如果存在 id 或 md 则进行数据加载
     * 否则调用 loadBack 进行选项初始化
     */
    if (loadUrl
    && (hsGetParam(loadUrl, idKey)
    ||  hsGetParam(loadUrl, mdKey))) {
        this.load (loadUrl, loadArr);
    } else {
        this.loadBack( {} );
    }

    this.valiInit(/*all*/ );
    this.saveInit(saveUrl );
}
HsForm.prototype = {
    load     : function(url, data) {
        if (url ) this._url  = url;
        if (data) this._data = hsSerialArr(data);
        jQuery.hsAjax({
            "url"       : this._url ,
            "data"      : this._data,
            "type"      : "POST",
            "dataType"  : "json",
            "funcName"  : "load",
            "async"     : false,
            "cache"     : false,
            "context"   : this,
            "success"   : this.loadBack
        });
    },
    loadBack : function(rst) {
        rst = hsResponObj(rst);
        if (rst["ok"] === false) return;

        this.formBox.trigger("loadOver", [rst, this]);

        if (rst["enum"]) this.fillEnum(rst["enum"]);
        if (rst["info"]) this.fillInfo(rst["info"]);
        else if (rst.list && rst.list[0]) {
            this.fillInfo (  rst.list[0]); // retrieve 可能仅提供 list
        }

        this.formBox.trigger("loadBack", [rst, this]);
    },
    fillEnum : function(enam) {
        var nodes, enams, i, n, t, v, inp;
        nodes = this.formBox.find("[data-fn],select[name]");
        enams = {};
        for(i = 0; i < nodes.length; i ++) {
            n = jQuery(nodes[i]).attr("name");
            if (! n) n = jQuery(nodes[i]).attr("data-fn");
            v = enam [n];
            if (! n) continue;
            if (! v) v = hsGetValue(enam , n);
            enams[n] = v;
        }

        this._enum = enam;
        for(n in enams) {
            v =  enams[n];
            i = 1;
            inp = this.formBox.find('[name="'+n+'"]');
            if (inp.length == 0) {
                i = 0;
                inp = this.formBox.find('[data-fn="'+n+'"]');
            }

            if (typeof(this["_fill_"+n]) !== "undefined") {
                v = this["_fill_"+n].call(this, inp, v, n, "enum");
            }
            // 按类型填充
            else if (inp.attr("data-ft")) {
                t =  inp.attr("data-ft");
            if (typeof(this["_fill_"+t]) !== "undefined") {
                v = this["_fill_"+t].call(this, inp, v, n, "enum");
            }}
            if (! v) continue;

            if (i == 0) {
                this._fill__review(inp, v, n, "enum");
            }
            else if (inp.prop("tagName") == "SELECT") {
                this._fill__select(inp, v, n, "enum");
            }
        }
        delete this._enum;
    },
    fillInfo : function(info) {
        var nodes, infos, i, n, t, v, inp;
        nodes = this.formBox.find("[data-fn],input[name],select[name],textarea[name]");
        infos = {};
        for(i = 0; i < nodes.length; i ++) {
            n = jQuery(nodes[i]).attr("name");
            if (! n) n = jQuery(nodes[i]).attr("data-fn");
            if (! n) continue;
            v = info [n];
            if (! v) v = hsGetValue(info , n);
            infos[n] = v;
        }

        this._info = info;
        for(n in infos) {
            v =  infos[n];
            i = 1;
            inp = this.formBox.find('[name="'+n+'"]');
            if (inp.length == 0) {
                i = 0;
                inp = this.formBox.find('[data-fn="'+n+'"]');
            }

            if (typeof(this["_fill_"+n]) !== "undefined") {
                v = this["_fill_"+n].call(this, inp, v, n, "info");
            }
            // 按类型填充
            else if (inp.attr("data-ft")) {
                t =  inp.attr("data-ft");
            if (typeof(this["_fill_"+t]) !== "undefined") {
                v = this["_fill_"+t].call(this, inp, v, n, "info");
            }}
            if (! v && (v !== 0 || v !== "")) continue;

            if (i == 0) {
                v = this._fill__review(inp, v, n, "info");
                if (! v && (v !== 0 || v !== "")) continue;
                if (inp.is("input,select,textarea")) {
                    inp.val (v);
                } else {
                    inp.text(v);
                }
            }
            else if (inp.attr("type") == "checkbox"
                 ||  inp.attr("type") == "radio") {
                jQuery.each(! jQuery.isArray(v) ? [v] : v ,
                function(i, u) {
                    inp.filter("[value='"+u+"']")
                       .prop  ("checked" , true )
                       .change();
                });
            }
            else if (inp.attr("type") == "file" ) {
                inp.attr("data-value",v).change();
            }
            else {
                inp.val(v).change();
            }
        }
        delete this._info;
    },

    saveInit : function(act) {
        var url  = this.formBox.attr("action" ) || act;
        var type = this.formBox.attr("method" );
        var enc  = this.formBox.attr("enctype");
        var data = this.formBox;
        var that = this;

        data.attr("action", hsFixUri( url ));

        if ( enc === "multipart/form-data" ) {
            if (data.attr("target") == null) {
                var name  = "_" + ( (new Date()).getTime() % 86400000 ) + "_" + Math.floor( Math.random( ) * 1000 );
                var style = "width:0; height:0; border:0; margin:0; padding:0; overflow:hidden; visibility:hidden;";
                var frame = jQuery('<iframe src="about:blank" name="' + name + '" style="' + style + '"></iframe>');
                data.attr("target", name).before(frame);
                frame.on ("load", function() {
                    var doc = frame[0].contentDocument || frame[0].contentWindow.document;
                    if (doc.location.href == "about:blank") return;
                    var rst = doc.body.innerHTML.replace( /(^<PRE.*?>|<\/PRE>$)/igm, '' );
                    that.saveBack(rst);
                });
            }
        } else {
            data.on("submit", function( evt ) {
                if (evt.isDefaultPrevented()) {
                    return;
                }
                evt.preventDefault( );
                jQuery.hsAjax({
                    "url"       : url,
                    "data"      : hsSerialArr(data),
                    "type"      : type || "POST",
                    "dataType"  : "json",
                    "funcName"  : "save",
                    "async"     : false,
                    "cache"     : false,
                    "global"    : false,
                    "context"   : that,
                    "complete"  : that.saveBack,
                    "error"     : function() { return false; }
                });
            });
        }
    },
    saveBack : function(rst) {
        rst = hsResponObj(rst, true);
        if (rst.ok === false) {
            var evt = jQuery.Event("saveFail");
            this.formBox.trigger(evt, [rst, this]);
            if (evt.isDefaultPrevented( )) return ;

            // 错误提示
            if (rst.errs) {
                this.seterror( rst.errs );
            } else
            if (!rst.msg) {
                 rst.msg  =  hsGetLang('error.unkwn');
            }
            if ( rst.msg) {
                jQuery.hsNote(rst.msg, 'alert-danger', -1);
            }
        } else {
            var evt = jQuery.Event("saveBack");
            this.formBox.trigger(evt, [rst, this]);
            if (evt.isDefaultPrevented( )) return ;

            // 关闭窗口
            this.loadBox.hsClose();

            // 完成提示
            if ( rst.msg) {
                jQuery.hsNote(rst.msg, 'alert-success', 0);
            }
        }
    },

    _fill__review : function(inp, v, n, t) {
        // 图片和链接
        if (inp.is("img")) {
            inp.attr("src" , v );
            return false;
        }
        if (inp.is( "a" )) {
            inp.attr("href", v );
            return false;
        }

        // 枚举
        if (t === "enum" ) {
            inp.data("enum", v );
            return v;
        }
        var a = inp.data("enum");
        if (a) {
            var k = inp.attr("data-vk"); if (! k) k = 0;
            var t = inp.attr("data-tk"); if (! t) t = 1;
            var i, c, e, m = { };
            inp.empty()
               .removeData( "enum" );
            if (! jQuery.isArray(v)) {
                v  =  [v];
            }
            for(i == 0; i < a.length; i ++) {
                e  = a[i];
                m[e[k]]=e[t];
            }
            for(i == 0; i < v.length; i ++) {
                c  = v[i];
                e  = m[v[i]];
                inp.append(jQuery('<li></li>').text(e)).attr("data-code", c);
            }
            return  false;
        }

        // 标签
        if (inp.is("ul")) {
            var v = inp.attr("data-vk"); if (! k) k = 0;
            var t = inp.attr("data-tk"); if (! t) t = 1;
            var i, c, e;
            inp.empty();
            for(i == 0; i < v.length; i ++) {
                c  = i;
                e  = v[i];
                if (jQuery.isPlainObject(e)
                ||  jQuery.isArray(e)) {
                    c = e[k];
                    e = e[t];
                }
                inp.append(jQuery('<li></li>').text(e)).data("data-code", c);
            }
            return  false;
        }

        return v;
    },
    _fill__select : function(inp, v, n, t) {
        if (t !== "enum")  return v;
        var vk = inp.attr("data-vk"); if(!vk) vk = 0;
        var tk = inp.attr("data-tk"); if(!tk) tk = 1;
        for (var i = 0; i < v.length; i ++) {
            var opt = jQuery('<option></option>');
            opt.val (hsGetValue(v[i], vk))
               .text(hsGetValue(v[i], tk))
               .data("data", v[i]);
            inp.append(opt);
        }
        inp.change().click(); // multiple 必须触发 click 才初始化
    },
    _fill__radio : function(inp, v, n, t) {
        if (t !== "enum") return v;
        var vk = inp.attr("data-vk"); if(!vk) vk = 0;
        var tk = inp.attr("data-tk"); if(!tk) tk = 1;
        for(var i = 0; i < v.length; i ++) {
            var lab = jQuery('<label><input type="radio"/><span></span></label>');
            lab.find("input").attr("name", n).data(v[i])
                             .val (hsGetValue(v[i], vk));
            lab.find("span" ).text(hsGetValue(v[i], tk));
            inp.append(lab);
        }
        inp.find(":radio").first().change();
    },
    _fill__check : function(inp, v, n, t) {
        if (t !== "enum") return v;
        var vk = inp.attr("data-vk"); if(!vk) vk = 0;
        var tk = inp.attr("data-tk"); if(!tk) tk = 1;
        for(var i = 0; i < v.length; i ++) {
            var lab = jQuery('<label><input type="checkbox"/><span></span></label>');
            lab.find("input").attr("name", n).data(v[i])
                             .val (hsGetValue(v[i], vk));
            lab.find("span" ).text(hsGetValue(v[i], tk));
            inp.append(lab);
        }
        inp.find(":checkbox").first().change();
    },
    _fill__checkset : function(inp, v, n, t) {
        if (t !== "enum") return v;

        var vk = inp.attr("data-vk"); if(!vk) vk = 0;
        var tk = inp.attr("data-tk"); if(!tk) tk = 1;
        var vl = inp.attr("data-vl"); if(!vl) vl = 0; // Value List
        var tl = inp.attr("data-tl"); if(!tl) tl = 1; // Title Line
        var bc = this._fill__checkset_body_class || "checkbox";
        var ic = this._fill__checkset_item_class || "col-md-6";

        if (v !== undefined) {
        for(var i = 0; i < v.length; i ++) {
            var u = v[ i ][vl];
            var s = v[ i ][tl];
            var set = jQuery('<fieldset>'
                            +'<legend class="dropdown-toggle">'
                            +'<input type="checkbox" class="checkall dropdown-deny"/>'
                            +'&nbsp;<span></span><b class="caret"></b>'
                            +'</legend>'
                            +'<div class="dropdown-body '+bc+'"></div>'
                            +'</fieldset>');
            set.find("span").first().text(s);
            inp.append(set );
            set = set.find ( "div" );

            for(var j = 0; j < u.length; j ++) {
                var w = u[ j ];
                var lab = jQuery('<label class="'+ic+'"><input type="checkbox"/>'
                                +'<span></span></label>');
                lab.find("input").attr("name", n).data(w)
                                 .val (hsGetValue(w, vk));
                lab.find("span" ).text(hsGetValue(w, tk));
                set.append(lab);
            }
        }}

        inp.find(":checkbox").first().change();
        inp.hsReady();
    },

    _fill__htime : function(td, v, n) {
        if (v === undefined) return v;
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
        if (v === undefined) return v;
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
        if (v === undefined) return v;
        return hsFmtDate(v, hsGetLang("datetime.format"));
    },
    _fill__date : function(td, v, n) {
        if (v === undefined) return v;
        return hsFmtDate(v, hsGetLang("date.format"));
    },
    _fill__time : function(td, v, n) {
        if (v === undefined) return v;
        return hsFmtDate(v, hsGetLang("time.format"));
    },
    _fill__html : function(td, v, n) {
        if (v === undefined) return v;
        td.html(v); return false;
    },

    valiInit : function() {
        var that = this;
        this.formBox.attr("novalidate", "novalidate");
        this.formBox.on( "reset", function() {
            return that.verified();
        });
        this.formBox.on("submit", function() {
            return that.verifies();
        });
        this.formBox.on("change","input,select,textarea,[data-fn]",
        function() {
            var inp = jQuery(this);
            that.validate(inp.attr("name") || inp.attr("data-fn"));
        });
    },
    verified : function() {
        this.formBox.find(".form-group").removeClass("has-error" );
        this.formBox.find(".help-block").   addClass("invisible" );
        return true;
    },
    verifies : function() {
        this.verified();
        var vali = true;
        var inps = {  };
        this.formBox.find("input,select,textarea,[data-fn]").each(
        function() {
            var inp = jQuery(this);
            var nam = inp.attr("name")||inp.attr("data-fn" );
            if (!nam) return true ;
            if (inps[nam] === undefined) {
                inps[nam] = inp;
            } else {
                inps[nam] = inps[nam].add(inp);
            }
        });
        for(var nam in inps ) {
            var val = this.validate(inps[nam]);
            if (val == false) {
                vali = false;
            }
        }
        if ( !  vali ) {
            jQuery.hsNote(hsGetLang('form.invalid'), 'alert-danger', -1);
        }
        return  vali;
    },
    validate : function(inp) {
        if (inp === undefined) {
            this.verifies( );
            return;
        }

        if (typeof inp == "string") {
            inp = this.formBox.find('[name="' + inp + '"],[data-fn="' + inp + '"]');
        } else {
            inp = jQuery(inp);
        }
        for(var key in this.rules) {
            if (!inp.is(key)) {
                continue;
            }
            var err  =  this.rules[key].call(this, inp.val(), inp);
            if (err !== true) {
                err  =  err || hsGetLang ( "form.haserror" );
                this.haserror(inp, err);
                return false;
            } else {
                this.haserror(inp);
            }
        }
        return  true;
    },
    seterror : function(err) {
        this.verified(   );
        for (var n in err) {
             var e  = err[n  ];
            this.haserror(n,e);
        }
    },
    haserror : function(inp, err) {
        if (err === undefined && jQuery.isPlainObject(inp)) {
            this.invalids(inp);
            return;
        }

        if (typeof inp == "string") {
            inp = this.formBox.find('[name="'+inp+'"],[data-fn="'+inp+'"]');
        } else {
            inp = jQuery(inp);
        }
        var grp = inp.closest(".form-group");
        var blk = grp.find   (".help-block");
        if (blk.size() == 0 ) {
            blk = jQuery('<p class="help-block"></p>').appendTo(grp);
        }
        if (err == undefined) {
            grp.removeClass("has-error");
            blk.   addClass("invisible");
        } else {
            grp.   addClass("has-error");
            blk.removeClass("invisible");
            blk.text(err);
        }
    },
    geterror : function(inp, err, rep) {
        var msg = err.replace(/^form\./, "").replace(/\./g, "-");
            msg = "data-"+ msg+"-error";
            msg = inp.attr(msg)
               || inp.attr("data-error");
        if (msg) {
            err = msg;
        }
        if (this.rmsgs[err]) {
            err = this.rmsgs[err];
        }

        // 放入字段标签
        var lab = inp.attr("data-label");
        if (lab == null) {
            lab = inp.closest(".form-group")
                     .find(".control-label")
                     .text();
        }
        if (lab) {
            if(jQuery.isArray(rep)) {
                var rap = {};
                for(var i = 0; i < rep.length; i ++) {
                    rap[i + "" ] = rep[i];
                }
                rep = rap;
            } else
            if (rep == null) {
                rep = { };
            }
            rep._ = hsGetLang(lab);
        }

        return hsGetLang(err, rep);
    },
    rmsgs : {
    },
    rules : {
        "[required],[data-required]" : function(val, inp) {
            if (inp.is("select")) {
                if (!val) {
                    return this.geterror(inp, "form.requires");
                }
            } else if (inp.is(":file")) {
                if (!val && !inp.data("value")) {
                    return this.geterror(inp, "form.required");
                }
            } else if (inp.is(":checkbox,:radio" )) {
                if (!inp.filter(":checked").length) {
                    return this.geterror(inp, "form.requires");
                }
            } else if (inp.is(".checkbox")) {
                if (!inp. find (":checked").length) {
                    return this.geterror(inp, "form.requires");
                }
            } else if (inp.is(  "ul,div" )) {
                if (!inp. find (":hidden" ).length) {
                    return this.geterror(inp, "form.requires");
                }
            } else {
                if (!val) {
                    return this.geterror(inp, "form.required");
                }
            }
            return true;
        },
        "[pattern],[data-pattern]" : function(val, inp) {
            var pn = inp.attr("pattern") || inp.attr("data-pattern");
            var pm = /^\/(.*)\/([gim])?$/.exec(pn);
            if (pm) {
                pn = new RegExp(pm[1], pm[2]);
            } else {
                pn = new RegExp(pn);
            }
            if (! pn.test(val)) {
               return this.geterror(inp, "form.is.not.match");
            }
            return true;
        },
        "[maxlength],[data-maxlength]" : function(val, inp) {
            var max = inp.attr("maxlength") || inp.attr("data-maxlength");
            if (val.length > max) {
                return this.geterror(inp, "form.gt.maxlength", [max]);
            }
            return true;
        },
        "[minlength],[data-minlength]" : function(val, inp) {
            var min = inp.attr("minlength") || inp.attr("data-minlength");
            if (val.length < min) {
                return this.geterror(inp, "form.lt.minlength", [min]);
            }
            return true;
        },
        "[max],[data-max]" : function(val, inp) {
            var max = inp.attr("max") || inp.attr("data-max");
            if (val > max) {
                return this.geterror(inp, "form.lt.min", [max]);
            }
            return true;
        },
        "[min],[data-min]" : function(val, inp) {
            var min = inp.attr("min") || inp.attr("data-min");
            if (val < min) {
                return this.geterror(inp, "form.lt.min", [min]);
            }
            return true;
        },
        "[type=number]" : function(val, inp) {
            if (!/^-?[0-9]*(\.[0-9]+)?$/.test(val)) {
                return this.geterror(inp, "form.is.not.number");
            }
            return true;
        },
        "[type=email]" : function(val, inp) {
            if (!/^([a-z0-9_\.\-\+]+)@([\da-z\.\-]+)\.([a-z\.]{2,6})$/i.test(val)) {
                return this.geterror(inp, "form.is.not.email");
            }
            return true;
        },
        "[type=url]" : function(val, inp) {
            if (!/^(https?:\/\/)?[\da-z\.\-]+\.[a-z\.]{2,6}(:\d+)?(\/[^\s]*)?$/i.test(val)) {
                return this.geterror(inp, "form.is.not.url");
            }
            return true;
        },
        "[type=tel]" : function(val, inp) {
            if (!/^(\+\d{1,3})?\d{3,}$/i.test(val)) {
                return this.geterror(inp, "form.is.not.tel");
            }
            return true;
        },
        "[data-validate]" : function(val, inp) {
            var fn = inp.attr("data-validate");
            try {
                if (inp.data(fn)) {
                    inp.data(fn).call(this, val, inp);
                } else
                if ( window [fn]) {
                     window [fn].call(this, val, inp);
                } else {
                    if (window.console.error) {
                        window.console.error(fn+" not found!");
                    } else {
                        window.console.log  (fn+" not found!");
                    }
                }
            } catch (ex) {
                if (window.console) {
                    if (window.console.error) {
                        window.console.error(fn+" run error: "+ex, val, inp);
                    } else {
                        window.console.log  (fn+" run error: "+ex, val, inp);
                    }
                }
                return false;
            }
        },
        "[data-verify]" : function(val, inp, url) {
            if (! val) return true;
            var ret = true;
            var obj = this.formBox;
            var data = {
                "n" : inp.attr("name"),
                "v" : val
            };
            if (! url) url = inp.attr("data-verify");
            url = url.replace(/\$\{(.*?)\}/g,function(x, n) {
                return obj.find("[name='"+n+"']").val( ) || "";
            });
            jQuery.hsAjax({
                "url": url,
                "data": data,
                "type": "POST",
                "dataType": "json",
                "funcName": "vali",
                "async": false,
                "cache": false,
                "context": this,
                "success": function(rst) {
                    if (rst["info"] !== undefined) {
                        ret = !jQuery.isEmptyObject(rst["info"]);
                    } else
                    if (rst["list"] !== undefined) {
                        ret = rst["list"].length > 0;
                    } else
                    if (rst["sure"] !== undefined) {
                        ret = rst["sure"] ? true :
                            ( rst["msg" ] ? rst["msg"] : false );
                    } else {
                        ret = rst[ "ok" ] ? true :
                            ( rst["msg" ] ? rst["msg"] : false );
                    }
                }
            });
            return ret;
        },
        "[data-unique]" : function(val, inp) {
            var ret = this.rules["[data-verify]"].call(this, val, inp, inp.attr("data-unique"));
            if (typeof ret === "string") {
                return ret;
            } else if (! ret) {
                return this.geterror(inp, "form.is.not.unique");
            }
            return true;
        },
        "[data-exists]" : function(val, inp) {
            var ret = this.rules["[data-verify]"].call(this, val, inp, inp.attr("data-exists"));
            if (typeof ret === "string") {
                return ret;
            } else if (! ret) {
                return this.geterror(inp, "form.is.not.exists");
            }
            return true;
        },
        "[data-repeat]" : function(val, inp) {
            var fn = inp.attr("data-repeat");
            var fd = this.formBox.find("[name=" + fn + "]");
            if (fd.val() != val) {
                return this.geterror(inp, "form.is.not.repeat");
            }
            return true;
        },
        "[data-relate]" : function(val, inp) {
            var fn = inp.attr("data-relate");
            var fd = this.formBox.find("[name=" + fn + "]");
            if (fd.val()) {
                this.validate(fd);
            }
            return true;
        }
    }
};

jQuery.fn.hsForm = function(opts) {
    return this._hsModule(HsForm, opts);
};

(function($) {
    $(document)
    .on("submit", "form",
    function(evt) {
        if (evt.isDefaultPrevented()) {
            return;
        }
        var btn = $(this).find( ":submit" );
        btn.prop("disabled", true );
        btn.data("txt", btn.text());
        btn.text(hsGetLang("form.sending"));
    })
    .on("saveBack saveFail", "form",
    function() {
        var btn = $(this).find( ":submit" );
        var txt = btn.data( "txt" );
        if (txt)  btn.text(  txt  );
        btn.prop("disabled", false);
    })
    .on("change", "fieldset .checkall",
    function(evt) {
        this.indeterminate = false;
        var box = $(this).closest("fieldset");
        var ckd = $(this).prop   ("checked" );
        box.find(":checkbox:not(.checkall)").prop("checked", ckd).trigger("change");
    })
    .on("change", "fieldset :checkbox:not(.checkall)",
    function(evt) {
        var box = $(this).closest("fieldset");
        var siz = box.find(":checkbox:not(.checkall)").length;
        var len = box.find(":checkbox:not(.checkall):checked").length;
        var ckd = siz && siz == len ? true : (len && siz != len ? null : false);
        box.find(".checkall").prop("choosed", ckd);
    })
    .on("click", "[data-toggle=hsEdit]",
    function(evt) {
        var that = $(this).closest(".HsForm").data("HsForm");
        var func = function() {
            $(this).on("saveBack", function(evt, rst, rel) {
                var ext = jQuery.Event("saveBack");
                ext.relatedTarget = evt.target;
                ext.relatedHsInst = rel /****/;
                that.formBox.trigger(ext, [rst, that]);
                if (ext.isDefaultPrevented( )) return ;

                that.load( );
            });
        };

        var box = $(this).attr("data-target");
        var url = $(this).attr("data-href");
            url = hsFixPms(url, this);
        if (box) {
            box = $(this).hsFind(box);
            box.hsOpen(url, func);
        } else {
              $.hsOpen(url, func);
        }
        evt.stopPropagation();
    });
})(jQuery);
