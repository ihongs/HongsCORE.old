
/**
 * 组合选择工具之用法:
 * 在选择列表配置添加:
 * <param name="_fill__pick" value="(hsListFillPick)"/>
 * 在选择列表头部添加:
 * <td data-ft="_pick"><input type="checkbox" class="check-all"/></td>
 * 在表单配置区域添加:
 * <param name="_fill__pick" value="(hsFormFillPick)"/>
 * 在表单选项区域添加:
 * <ul data-fn="x" data-ft="_pick" requires="requires"></ul>
 * <button type="button" data-toggle="hsPick" data-pick="x/pick.html">Pick</button>
 */

/**
 * 选择控件
 * @param {String} url 要打开的选择页地址
 * @param {jQuery} tip 在哪打开
 * @param {jQuery} box 在哪填充
 * @param {Function} fil 填充函数
 * @returns {jQuery}
 */
jQuery.fn.hsPick = function(url, tip, box, fil) {
    if (fil == undefined
    &&  typeof url == "function") {
        fil  = url;
        url  = tip;
        tip  = null;
    } else if (url == undefined ) {
        url  = tip;
        tip  = null;
    }

    var v    = { };
    var n    = box.attr("name")||box.attr("data-fn");
    var form = box.closest(".HsForm").data("HsForm");
    var mul  = /(\[\]|\.)$/.test( n );
    var btn  = jQuery(this);

    if (! fil) {
        do {
            fil = form["_fill_"+ n];
            if (fil) break;

            var t;

            t = box.attr("data-ft");
            fil = form["_fill_"+ t];
            if (fil) break;

            t = box.attr("data-fn");
            fil = form["_fill_"+ t];
            if (fil) break;

            fil = hsFormFillPick;
        } while (false);
    }

    if (box.is("input")) {
        var val = box.val ( );
        var txt = btn.text( );
        v[val] = txt;
    } else {
        box.find("li").each(function() {
            var opt = jQuery(this);
            var val = opt.find(".pickval").val( );
            var txt = opt.find(".picktxt").text();
            v[val] = txt;
        });
    }

    function pickItem(val, txt) {
        var evt = jQuery.Event("pickItem");
        box.trigger( evt, arguments );
        if (evt.isDefaultPrevented()) {
            return false;
        }

        if (! mul) {
            for( var  key  in  v )
                delete v[key];
            if (txt !== undefined)
                v[val] = txt ;
        } else {
            if (txt !== undefined)
                v[val] = txt ;
            else
                delete v[val];
        }
        return true;
    }

    function pickBack() {
        var evt = jQuery.Event("pickBack");
        box.trigger(evt, [v]);
        if (evt.isDefaultPrevented()) {
            return false;
        }

        fil.call(form, box , v, n, "data");

        box.trigger("change");
        return true;
    }

    function pickOpen() {
        var tip = jQuery( this );
        tip.data("pickData", v )
           .addClass("pickbox")
        .toggleClass("pickmul", mul)
        .on("saveBack", function(evt, rst) {
            if (! rst || ! rst.back
            ||  ! pickItem.apply( self, rst.back )
            ||  ! pickBack())
                return;

            tip.hsClose();
        })
        .on("click", ".ensure", function() {
            var btn = jQuery(this);
            if (! btn.closest(".openbox").is(tip)
            ||  ! pickBack())
                return;

           tip.hsClose();
        })
        .on("change", ".check-one", function() {
            var chk = jQuery(this);
            if (chk.closest(".HsList").data("HsList")._info)
                return;

            var val = chk.val();
            var txt;
            var inf;

            do {
                if (! chk.prop("checked") ) break;

                // 获取额外数据
                inf = chk.data();
                if (! inf  ) {
                    inf = chk.attr("data-data");
                    if (inf) {
                        inf = eval('('+inf+')');
                    }  else  {
                        inf = null;
                    }
                }

                txt = chk.attr("data-name");
                if (txt) break;
                txt = chk.closest("tr").find(".name").text();
                if (txt) break;

                var thd = chk.closest("table").find("thead");
                var tds = chk.closest( "tr"  ).find( "td"  );
                var idx;

                idx = thd.find("[data-ft=name]").index();
                if (idx != -1) txt = tds.eq(idx).text( );
                if (txt) break;
                idx = thd.find("[data-fn=name]").index();
                if (idx != -1) txt = tds.eq(idx).text( );
            }
            while (false);

            pickItem(val, txt, inf);
        });
    };

    var win;
    if (tip) {
        win = tip.hsOpen(url, undefined, pickOpen );
        win.data("rel", btn.closest(".openbox")[0]);
    } else {
        win =   $.hsOpen(url, undefined, pickOpen );
    }
    return win;
};

/**
 * 表单填充选项
 * @param {jQuery} box
 * @param {Object} v
 * @param {String} n
 * @param {String} t
 * @returns {undefined}
 */
function hsFormFillPick(box, v, n, t) {
    // 注意: 填充是用 data 而不理会 info
    if (t == "info") return;
    // 注意: 绑定当前函数用于选择后的填充
    box.data("pickFunc", hsFormFillPick);

    var btn = box.siblings("[data-toggle=hsPick]");
    var mul = /(\[\]|\.)$/.test(n);
    var vk  = box.attr("data-vk" );
    var tk  = box.attr("data-tk" );

    if (! vk) vk = 0;
    if (! tk) tk = 0;

    if (jQuery.isArray(v)) {
        var x = {};
        for(var i = 0; i < v.length; i++) {
            var j = v[i];
            x[j[vk]] = x[j[tk]];
        }
        v = x ;
    } else if (! jQuery.isPlainObject(v)) {
        v = {};
    }

    if (box.is("input") ) {
        function reset(box, btn) {
            var txt = btn.data("txt");
            var cls = btn.data("cls");
            box.val ( "");
            btn.text(txt);
            btn.attr( "class" , cls );
        }
        function inset(box, btn, val, txt) {
            box.val (val);
            btn.text(txt);
            btn.addClass("btn-default");
            btn.append('<span class="close">&times;</span>');
        }

        if (! btn.data("pickInited"))  {
            btn.data("pickInited", 1);
            btn.data("txt", btn.text( ) );
            btn.data("cls", btn.attr("class"));
            btn.on("click", ".close", box, function(evt) {
                var btn = jQuery(evt.delegateTarget);
                var box = evt.data;
                reset(box, btn);
                box.trigger("change");
                return false;
            });
        }

        if (jQuery.isEmptyObject(v)) {
            reset(box, btn);
        } else
        for(var val in v) {
            var txt  = v[val];
            inset(box, btn, val,txt);
        }
    } else {
        if (! box.data("pickInited"))  {
            box.data("pickInited", 1);
            box.on("click", ".close", btn, function(evt) {
                var opt = jQuery(this).closest("li");
                var val = opt.find(":hidden").val( );
                var btn = evt.data;
                delete v[val];
                opt.remove( );
                btn.show  ( );
                box.trigger("change");
                return false ;
            });
            if (! mul) {
                box.on("click", null, btn, function(evt) {
                    evt.data.click();
                });
            }
        }

        if (jQuery.isEmptyObject(v)) {
            btn.show();
        } else if (! mul) {
            btn.hide();
        }

        box.empty();
        for(var val in v) {
            var txt  = v[val];
            box.append(jQuery('<li class="btn btn-default form-control"></li>').attr("title", txt )
               .append(jQuery('<input class="pickval" type="hidden"/>').attr( "name", n ).val(val))
               .append(jQuery('<span  class="picktxt"></span>').text(   txt   ))
               .append(jQuery('<span  class="close pull-right">&times;</span>'))
            );
        }
    }
}

/**
 * 列表填充选择
 * @param {jQuery} cel
 * @param {String} v
 * @param {String} n
 * @returns {undefined}
 */
function hsListFillPick(cel, v, n) {
    var box = cel.closest(".pickbox");
    var mul = box.hasClass("pickmul");
    var dat = box.data("pickData");

    // 单选还是多选
    if (! mul) {
        box.find(".check-all").hide( );
    }

    // 填充选择控件
    if (! mul) {
        HsList.prototype._fill__radio.call( this, cel, v, n );
    } else {
        HsList.prototype._fill__check.call( this, cel, v, n );
    }

    // 判断是否选中
    if (dat[v] !== undefined ) {
        cel.find(".check-one").prop("checked", true).change();
    }
}

(function($) {
    $(document)
    .on("click", "[data-toggle=hsPick]",
    function() {
        var url = $(this).attr("href");
        var box = $(this).attr("data-target");
        if (box) {
            box = _hsTarget(this, box);
        } else {
            var nav = $(this).closest(".nav");
            if (nav.size()) {
                var idx = $(this).closest( "li" ).index();
                box = nav.data("tabs").getPanes().eq(idx);
            }
        }

        // 填充区域
        var inp = $(this).attr("data-fill-area");
        if (inp) {
            inp = inp.charAt(0) == ">" ? $(inp , this) : $(inp);
        } else {
            inp = $(this).siblings("[name],[data-fn]");
        }

        // 填充函数
        var fun = $(this).attr("data-fill-func");
        if (fun) {
            fun = eval ( '(' + fun + ')' );
        }

        $(this).hsPick(url, box, inp, fun);
        return false;
    });
})(jQuery);
