
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
    var saveUrl  = hsGetValue(opts, "saveUrl");
    var loadUrl  = hsGetValue(opts, "loadUrl");
    var loadMode = hsGetValue(opts, "loadMode", 1); // 加载模式, 0无 1附带上层数据 2总是进行加载
    var idKey    = hsGetValue(opts, "idKey", "id"); // id参数名, 用于判断编辑还是创建

    if (formBox.length === 0) formBox = context;

    this.context = context;
    this.loadBox = loadBox;
    this.formBox = formBox;

    if (opts) for ( var k in opts ) {
        if ('_'===k.substring(0, 1)
        ||  this[k] !== undefined ) {
            this[k]  =  opts[k];
        }
    }

    var a, i, n, v;

    /**
     * 获取並使用上层数据
     */
    var loadData = [];
    loadMode = parseInt(loadMode);
    if ( 1 & loadMode ) {
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
    a = hsGetSeria (loadData, idKey);
    if ((2 & loadMode) || a) {
        this.load(loadUrl, loadData);
    } else {
        this.fillEnum( { } );
    }

    /**
     * 使用初始化数据填充表单
     * 在打开表单窗口时, 可能指定一些参数(如父ID, 初始选中项等)
     * 这时有必要将这些参数值填写入对应的表单项, 方便初始化过程
     */
    var n, v;
    for(i = 0; i < loadData.length; i ++) {
        n = loadData[i].name ;
        v = loadData[i].value;
        if ( n === idKey && v === "0" ) continue;
        formBox.find("[name='"   +n+"']").val(v);
        formBox.find("[data-pn='"+n+"']").val(v);
    }

    this.valiInit();
    this.saveInit(saveUrl);
}
HsForm.prototype = {
    load     : function(url, data) {
        jQuery.hsAjax({
            "url"       : url,
            "data"      : data,
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
        if (rst["ok"] === false) return;
        this.formBox.trigger("loadOver", [rst]);
        if (rst["enum"]) this.fillEnum(rst["enum"]);
        if (rst["info"]) this.fillInfo(rst["info"]);
        else if (rst.list && rst.list[0]) {
            this.fillInfo (  rst.list[0]); // retrieve 可能仅提供 list
        }
        this.formBox.trigger("loadBack", [rst]);
    },
    fillEnum : function(enam) {
        var nodes, datas, i, n, t, v, inp;
        nodes = this.formBox.find("select[name],[data-fn]");
        datas = {};
        for(i = 0; i < nodes.length; i ++) {
            n = jQuery(nodes[i]).attr("name");
            if (! n) n = jQuery(nodes[i]).attr( "data-fn" );
            v = enam [n];
            if (! v) v = hsGetValue(enam , n);
            datas[n] = v;
        }

        this._enum = enam;
        for(n in datas) {
            v =  datas[n];
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
        nodes = this.formBox.find("select[name],[data-fn],input[name],textarea[name]");
        infos = {};
        for(i = 0; i < nodes.length; i ++) {
            n = jQuery(nodes[i]).attr("name");
            if (! n) n = jQuery(nodes[i]).attr( "data-fn" );
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
                inp.text(v);
            }
            else if (inp.attr("type") == "checkbox"
                 ||  inp.attr("type") == "radio") {
                jQuery.each(!jQuery.isArray(v) ? [v] : v,
                function(i, u) {
                    inp.filter("[value='"+u+"']")
                       .prop  ("checked" , true )
                       .change();
                });
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

        this.formBox.attr(  "action", url  );
        this.formBox.on("submit", function() {
            return that.verifies();
        });

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
            // 遵循URL规则, 补全URL
            this.formBox.attr("action", hsFixUri(url));
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
                    "action"    : "save",
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
        if (rst.ok  === false) {
            var evt = new jQuery.Event("saveFail");
            this.formBox.trigger(evt, [rst]);
            this.formBox.find(".form-group").removeClass("has-error");
            this.formBox.find(".help-block").   addClass("invisible");
            if (rst.errors) {
                for(var n in rst.errors) {
                    var e =  rst.errors[n];
                    this.haserror(n , e);
                }
            }

            // 错误提示
            if (rst.msg) {
                alert(rst.msg);
            } else {
                alert(hsGetLang("error.unkwn"));
            }
        } else {
            // 完成提示
            if (rst.msg) {
                jQuery.hsNote(rst.msg, 'alert-success');
            }

            var evt = new jQuery.Event("saveBack");
            this.formBox.trigger(evt, [rst]);
            if (! evt.isDefaultPrevented( )) {
                this.loadBox.hsClose();
            }
        }
    },

    _fill__review : function(inp, v, n, t) {
        if (t === "enum") {
            inp.data("enum", v );
            return v;
        }
        var a = inp.data("enum");
          inp.removeData("enum");
        if (! a)
            return v;

        var vk = inp.attr("data-vk"); if(!vk) vk = 0;
        var tk = inp.attr("data-tk"); if(!tk) tk = 1;
        var i, c, b = {};
        for(i == 0; i < a.length; i ++) {
            c = a[i]; b[c[vk]] = b[c[tk]];
        }
        if (! jQuery.isArray(v)) {
            v = [v];
        }
        inp.empty();
        for(i == 0; i < v.length; i ++) {
            inp.append(jQuery('<li></li>').text(b[v[i]]));
        }
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
    _fill__chack : function(inp, v, n, t) {
        if (t !== "enum") return v;

        var vk = inp.attr("data-vk"); if(!vk) vk = 0;
        var tk = inp.attr("data-tk"); if(!tk) tk = 1;
        var vl = inp.attr("data-vl"); if(!vl) vl = 0;
        var tl = inp.attr("data-tl"); if(!tl) tl = 1;

        for(var i = 0; i < v.length; i ++) {
            var u = v[ i ][vl];
            var s = v[ i ][tl];
            var set = jQuery('<fieldset><legend class="dropdown-toggle"><input type="checkbox" class="checkall dropdown-deny"/>'
                        +'&nbsp;<span></span><b class="caret"></b></legend>'
                    +'<div class="dropdown-body checkbox"></div></fieldset>');
            set.find("span").first().text(s);
            inp.append(set );
            set = set.find ( "div" );

            for(var j = 0; j < u.length; j ++) {
                var w = u[ j ];
                var lab = jQuery('<label class="col-md-2"><input type="checkbox"/><span></span></label>');
                lab.find("input").attr("name", n).data(w)
                                 .val (hsGetValue(w, vk));
                lab.find("span" ).text(hsGetValue(w, tk));
                set.append(lab);
            }
        }

        inp.find(":checkbox").first().change();
    },

    valiInit : function() {
        var that = this;
        this.formBox.attr("novalidate", "novalidate");
        this.formBox.on("change", "input,select,textarea,[data-fn]",
        function() {
            var inp = jQuery(this);
            that.validate(inp.attr("name") || inp.attr("data-fn"));
        });
    },
    verifies : function() {
        var vali = true;
        var inps = {  };
        this.formBox.find("input,select,textarea,[data-fn]").each(
        function() {
            var inp = jQuery( this );
            var nam = inp.attr( "name" ) || inp.attr( "data-fn" );
            if (! nam) return true  ;
            if (inps[nam] === undefined) {
                inps[nam]  =   inp  ;
            } else {
                inps[nam].add( inp );
            }
        });
        for(var nam in inps) {
            var val = this.validate(inps[nam]);
            if (val == false) {
                vali = false;
            }
        }
        return  vali;
    },
    validate : function(inp ) {
        if (typeof inp == "string") {
            inp = this.formBox.find('[name="' + inp + '"],[data-fn="' + inp + '"]');
        } else {
            inp = jQuery(inp);
        }
        for(var key in this.rules) {
            if (!inp.is(key)) {
                continue;
            }
            var err = this.rules[key].call(this, inp.val(), inp);
            if (err !== true) {
                err = inp.attr("data-message") || err || hsGetLang("form.haserror");
                this.haserror(inp, err);
                return false;
            } else {
                this.haserror(inp);
            }
        }
        return true ;
    },
    haserror : function(inp, err) {
        if (typeof inp == "string") {
            inp = this.formBox.find('[name="'+inp+'"],[data-fn="'+inp+'"]');
        } else {
            inp = jQuery(inp);
        }
        var grp = inp.closest(".form-group");
        var blk = grp.find   (".help-block");
        if (blk.size() == 0 ) {
            blk = jQuery('<div class="help-block"></div>').appendTo(grp);
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
    rules : {
        "[required],[data-required]" : function(val, inp) {
            if (inp.is("select")) {
                if (!val) {
                    return hsGetLang("form.requires");
                }
            } else if (inp.is(":checkbox,:radio" )) {
                if (!inp.filter(":checked").length) {
                    return hsGetLang("form.requires");
                }
            } else if (inp.is(".checkbox")) {
                if (!inp. find (":checked").length) {
                    return hsGetLang("form.requires");
                }
            } else if (inp.is(  "ul,div" )) {
                if (!inp. find (":hidden" ).length) {
                    return hsGetLang("form.requires");
                }
            } else {
                if (!val) {
                    return hsGetLang("form.required");
                }
            }
            return true;
        },
        "[pattern],[data-parttern]" : function(val, inp) {
            var pn = inp.attr("data-pattern") || inp.attr("pattern");
            var ms = inp.attr("data-message");
            var pm = /^\/(.*)\/([gim])?$/.exec(pn);
            if (pm) {
                pn = new RegExp(pm[1], pm[2]);
            } else {
                pn = new RegExp(pn);
            }
            if (! pn.test(val)) {
               return hsGetLang(ms);
            }
            return true;
        },
        "[maxlength],[data-maxlength]" : function(val, inp) {
            var max = inp.attr("maxlength") || inp.attr("data-maxlength");
            if (val.length > max) {
                return hsGetLang("form.gt.maxlength", [max]);
            }
            return true;
        },
        "[minlength],[data-minlength]" : function(val, inp) {
            var min = inp.attr("minlength") || inp.attr("data-minlength");
            if (val.length < min) {
                return hsGetLang("form.lt.minlength", [min]);
            }
            return true;
        },
        "[max],[data-max]" : function(val, inp) {
            var max = inp.attr("max") || inp.attr("data-max");
            if (val > max) {
                return hsGetLang("form.lt.min", [max]);
            }
            return true;
        },
        "[min],[data-min]" : function(val, inp) {
            var min = inp.attr("min") || inp.attr("data-min");
            if (val < min) {
                return hsGetLang("form.lt.min", [min]);
            }
            return true;
        },
        "[type=number]" : function(val) {
            if (!/^-?[0-9]*(\.[0-9]+)?$/.test(val)) {
                return hsGetLang("form.is.not.number");
            }
            return true;
        },
        "[type=email]" : function(val) {
            if (!/^([a-z0-9_\.\-\+]+)@([\da-z\.\-]+)\.([a-z\.]{2,6})$/i.test(val)) {
                return hsGetLang("form.is.not.email");
            }
            return true;
        },
        "[type=url]" : function(val) {
            if (!/^(https?:\/\/)?[\da-z\.\-]+\.[a-z\.]{2,6}[#&+_\?\/\w \.\-=]*$/i.test(val)) {
                return hsGetLang("form.is.not.url");
            }
            return true;
        },
        "[data-validate]" : function(val, inp) {
            var fn = inp.attr("data-validate");
            var fd = inp.data() ? inp.data(): window;
            try {
                return hsGetValue(fd, fn).call(this, val, inp);
            } catch (ex) {
                if (window.console)
                    window.console.log("Call "+ fn +" error: " + ex, val, inp);
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
            url = url.replace(/\{(.*?)\}/g, function(x, n) {
                return obj.find("[name='" +n+ "']").val( ) || "";
            });
            jQuery.hsAjax({
                "url": url,
                "data": data,
                "type": "POST",
                "dataType": "json",
                "async": false,
                "cache": false,
                "success": function(rst) {
                    if (rst["info"] && !jQuery.isEmptyObject(rst["info"])) {
                        ret = true;
                    } else
                    if (rst["list"] && rst["list"].length) {
                        ret = true;
                    } else {
                        ret = rst["ok"] || rst["msg"];
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
                return hsGetLang("form.is.not.unique");
            }
            return true;
        },
        "[data-exists]" : function(val, inp) {
            var ret = this.rules["[data-verify]"].call(this, val, inp, inp.attr("data-exists"));
            if (typeof ret === "string") {
                return ret;
            } else if (! ret) {
                return hsGetLang("form.is.not.exists");
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
        },
        "[data-repeat]" : function(val, inp) {
            var fn = inp.attr("data-repeat");
            var fd = this.formBox.find("[name=" + fn + "]");
            if (fd.val() != val) {
                return hsGetLang("form.is.not.repeat");
            }
            return true;
        }
    }
};

jQuery.fn.hsForm = function(opts) {
    return this._hsConstr(opts, HsForm);
};

(function($) {
    $(document)
    .on("save", "form",
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
        box.find(".checkall").prop("choosed", siz && siz==len ? true : (len && siz!=len ? null : false));
    });
})(jQuery);
