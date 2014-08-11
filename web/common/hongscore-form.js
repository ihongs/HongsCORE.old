
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
    var idKey    = hsGetValue(opts, "idKey", "id"); // id参数名, 用于判断编辑还是创建
    var probeMode= hsGetValue(opts, "probeMode"  ); // 刺探模式, 无论有无id都请求信息
    var aloneMode= hsGetValue(opts, "aloneMode"  ); // 独立模式, 请求时不携带上层数据

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
    if ( ! aloneMode) {
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
    if (a||probeMode) {
        this.load(loadUrl, loadData);
    } else {
        this.fillData( { } );
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
        if (rst.__success__ === false) return;
        if (rst.data) this.fillData( rst.data );
        if (rst.info) this.fillInfo( rst.info );
        this.formBox.trigger("loadBack", [rst]);
    },
    fillData : function(data) {
        var nodes, datas, i, n, t, v, inp;
        nodes = this.formBox.find("select[name],[data-fn]");
        datas = {};
        for(i = 0; i < nodes.length; i ++) {
            n = jQuery(nodes[i]).attr( "name" );
            if (! n) n = jQuery(nodes[i]).attr( "data-fn" );
            v = hsGetValue(data, n);
            datas[n] = v;
        }

        this._data = data;
        for(n in datas) {
            v =  datas[n];
            i = 1;
            inp = this.formBox.find('[name="'+n+'"]');
            if (inp.length == 0) {
                i = 0;
                inp = this.formBox.find('[data-fn="'+n+'"]');
            }

            if (typeof(this["_fill_"+n]) !== "undefined") {
                v = this["_fill_"+n].call(this, inp, v, n, "data");
            }
            // 按类型填充
            else if (inp.attr("data-ft")) {
                t =  inp.attr("data-ft");
            if (typeof(this["_fill_"+t]) !== "undefined") {
                v = this["_fill_"+t].call(this, inp, v, n, "data");
            }}
            if (! v) continue;

            if (i == 0) {
                this._fill__review(inp, v, n, "data");
            }
            else if (inp.prop("tagName") == "SELECT") {
                this._fill__select(inp, v, n, "data");
            }
        }
        delete this._data;
    },
    fillInfo : function(info) {
        var nodes, infos, i, n, t, v, inp;
        nodes = this.formBox.find("input[name],textarea[name],select[name],[data-fn]");
        infos = {};
        for(i = 0; i < nodes.length; i ++) {
            n = jQuery(nodes[i]).attr( "name" );
            if (! n) n = jQuery(nodes[i]).attr( "data-fn" );
            v = hsGetValue(info, n);
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
                v = this._fill__review( inp, v, n, "info" );
                inp.text(v );
            }
            else if (inp.attr("type") == "checkbox"
                 ||  inp.attr("type") == "radio") {
                inp.filter("[value='"+v+"']")
                   .prop("checked", true)
                   .change();
            }
            else {
                inp.val (v )
                   .change();
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
        rst = hsResponObj(rst, !!this.formBox.attr("target"));
        if (rst.__success__ === false) {
            if (typeof rst.errors !== "undefined") {
                for(var n in rst.errors) {
                    var e =  rst.errors[ n ];
                    this.haserror(n , e);
                }
            }
            var evt = new jQuery.Event("saveFail");
            this.formBox.trigger(evt, [rst]);
        } else {
            var evt = new jQuery.Event("saveBack");
            this.formBox.trigger(evt, [rst]);
            if (! evt.isDefaultPrevented( )) {
                this.loadBox.hsClose();
            }
        }
    },

    _fill__review : function(inp, v, n, t) {
        if (t === "data") {
            inp.data("data", v );
            return v;
        }
        var a = inp.data("data");
          inp.removeData("data");
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
            inp.append($('<div class="fl"></div>').text(b[v[i]]));
        }
        inp.append($('<div class="cb"></div>'));
    },
    _fill__select : function(inp, v, n, t) {
        if (t !== "data")  return v;
        var vk = inp.attr("data-vk"); if(!vk) vk = 0;
        var tk = inp.attr("data-tk"); if(!tk) tk = 1;
        for (var i = 0; i < v.length; i ++) {
            var opt = jQuery('<option></option>');
            opt.val (hsGetValue(v[i], vk))
               .text(hsGetValue(v[i], tk))
               .data("data", v[i]);
            inp.append(opt);
        }
        inp.change();
    },
    _fill__check : function(inp, v, n, t) {
        if (t !== "data") return v;
        var vk = inp.attr("data-vk"); if(!vk) vk = 0;
        var tk = inp.attr("data-tk"); if(!tk) tk = 1;
        for (var i = 0; i < v.length; i ++) {
            var lab = jQuery('<label><input type="checkbox"/><span></span></label>');
            lab.find("input").attr("name", n).data(v[i])
                             .val (hsGetValue(v[i], vk));
            lab.find("span" ).text(hsGetValue(v[i], tk));
            lab.data("data", v[i]);
            inp.append(lab);
        }
        inp.append($('<div class="cb"></div>'));
        inp.find(":checkbox").change();
    },
    _fill__radio : function(inp, v, n, t) {
        if (t !== "data") return v;
        var vk = inp.attr("data-vk"); if(!vk) vk = 0;
        var tk = inp.attr("data-tk"); if(!tk) tk = 1;
        for (var i = 0; i < v.length; i ++) {
            var lab = jQuery('<label><input type="radio"/><span></span></label>');
            lab.find("input").attr("name", n).data(v[i])
                             .val (hsGetValue(v[i], vk));
            lab.find("span" ).text(hsGetValue(v[i], tk));
            lab.data("data", v[i]);
            inp.append(lab);
        }
        inp.append($('<div class="cb"></div>'));
        inp.find(":radio").change();
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
            inp = this.formBox.find('[name="'+inp+'"],[data-fn="'+inp+'"]');
        } else {
            inp = jQuery(inp);
        }
        for(var key in this.rules) {
            if (!inp.is(key)) {
                continue;
            }
            var err = this.rules[key].call(this, inp.val(), inp);
            if (err !== true) {
                err = err || hsGetLang("form.haserror");
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
            blk.   addClass("vh");
        } else {
            grp.   addClass("has-error");
            blk.removeClass("vh");
            blk.text(err);
        }
    },
    rules : {
        "[required]" : function(val) {
            if (!val) {
                return hsGetLang("form.required");
            }
            return true;
        },
        "[requires]" : function(val, inp) {
            var rst;
            if (inp.prop("tagName") == "SELECT") {
                rst = !!val;
            }
            else if (inp.is(":checkbox,:radio")) {
                rst = !!inp.parent().parent()
                    .find(":checkbox,:radio").val( );
            }
            else {
                rst = !!inp.find( ":hidden" ).length;
            }
            if (! rst) {
                return hsGetLang("form.requires");
            }
            return true;
        },
        "[pattern]" : function(val, inp) {
            var pn = inp.attr("data-pattern");
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
        "[maxlength]" : function(val, inp) {
            var max = inp.attr("maxlength");
            if (val.length > max) {
                return hsGetLang("form.gt.maxlength", [max]);
            }
            return true;
        },
        "[minlength]" : function(val, inp) {
            var min = inp.attr("minlength");
            if (val.length < min) {
                return hsGetLang("form.lt.minlength", [min]);
            }
            return true;
        },
        "[max]" : function(val, inp) {
            var max = inp.attr("max");
            if (val > max) {
                return hsGetLang("form.lt.min", [max]);
            }
            return true;
        },
        "[min]" : function(val, inp) {
            var min = inp.attr("min");
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
            if (! url) url = inp.attr("data-server");
            url = url.replace(/\{(.*?)\}/, function(x, n) {
                return obj.find("[name='" +n+ "']").val();
            });
            jQuery.hsAjax({
                "url": url,
                "data": data,
                "type": "POST",
                "dataType": "json",
                "async": false,
                "cache": false,
                "success": function(rst) {
                    ret = rst["__success__"]
                       || rst["__message__"];
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
    return _hsConstr( this, opts, HsForm );
};

(function($) {
    $(document)
    .on("save", "form",
    function(evt) {
        if (evt.isDefaultPrevented()) {
            return;
        }
        var btn = $(this).find(":submit");
        btn.prop("disabled", true );
        btn.data("txt", btn.text());
        btn.text(hsGetLang("form.saving"));
    })
    .on("saveBack saveFail", "form",
    function() {
        var btn = $(this).find(":submit");
        var txt = btn.data("txt");
        if (txt)  btn.text( txt );
        btn.prop("disabled", false);
    });
})(jQuery);
