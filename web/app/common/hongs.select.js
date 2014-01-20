/**
 * 扩展-选择控件
 * 作者：黄弘 <kevin.hongs@gmail.com>
 * 创建: 2013/06/20
 * 修改: 2014/01/20
 * 依赖: jquery.js, jquery.tools.js, hongs.common.js
 */

(function($) {
    var selectInit = function(box, dat, name) {
        box.find(".option-box").each(function() {
            dat["_"+$(this).find(":hidden").val()] = $(this).text();
        });
    };
    var selectFill = function(box, dat, name) {
        box.empty();
        for(var id in dat) {
            var txt = dat[id];
            id = id.substr(1);
            $('<div class="option-box"></div>')
            .append($('<input type="hidden"/>').attr("name", name).val(id))
            .append($('<span class="option-txt"></span>').text(txt))
            .append($('<span class="option-del"></span>'))
            .appendTo(box);
        }
        box.append($('<div class="cb"></div>'));
    };
    var selectLoad = function(tip, dat, name) {
        tip.find(".check-one").each(function() {
            if (dat["_"+$(this).val()]) {
                $(this).prop("checked", true );
            }
        });
    };

    /**
     * 选择组件
     * 用于在表单中选择关联、依赖的数据
     * @param {Object} conf 配置, 可选
     * conf 结构:
     * {
     *   name: 输入项名称, 默认取属性 data-select-name
     *   url : 选择窗地址, 默认取属性 data-select-url
     *   box : 已选项区域, 默认取属性 data-select-box, 都没有则自动找相邻的 .select-box
     *   init: 初始函数, 解析已选数据, 在构建选择组件时执行
     *   fill: 填充函数, 填充选中数据, 在选择窗确认选择时执行
     *   load: 加载函数, 选中已选的列, 在选择窗加载数据后执行
     * }
     */
    $.fn.hsSelect = function(conf) {
        if(!$(this).hasClass("hsSelectBtn")) {
            $(this).addClass("hsSelectBtn");
        }
        else {
            return;
        }

        if (!conf) conf = {};

        var init = conf["init"] || selectInit;
        var fill = conf["fill"] || selectFill;
        var load = conf["load"] || selectLoad;
        var url  = conf["url"]  || $(this).attr("data-select-url");
        var box  = conf["box"]  || $(this).attr("data-select-box");
        var btn  = $(this);
        var dat  = {};

        if (! box)
            box = btn.closest( ".form-group").find( ".select-box");
        else if (typeof box == "string")
            box = /^\$/.test(box) ? $(box.substr(1), btn) : $(box);
        box.data("init", init);
        box.data("fill", fill);
        
        var name = box.attr("data-fn");

        init.call(btn, box, dat, name);

        btn.on("click", function( ) {
            var tip = $.hsOpen(url)
            .toggleClass("single-select", btn.hasClass("single-select"))
            .on("loadBack", function() {
                load.call(btn, tip, dat, name);
            })
            .on("selectBack", function() {
                fill.call(btn, box, dat, name);
            })
            .on("selectItem", function(id, txt) {
                if (btn.hasClass("single-select")) {
                    dat = {};
                    if (txt !== undefined) {
                        dat[id] = txt ;
                    }
                } else {
                    if (txt !== undefined) {
                        dat[id] = txt ;
                    } else {
                        delete dat[id];
                    }
                }
            })
            .on("click", ".cancel", function() {
                tip.hsClose();
                return false ;
            })
            .on("click", ".ensure", function() {
                tip.trigger("selectBack");
                tip.hsClose();
                return false ;
            })
            .on("change", ".check-one", function() {
                var id  = $(this).val();
                var txt = $(this).prop("checked")
                        ? $(this).closest("tr").find(".name").text()
                        : undefined;
                tip.trigger("selectItem", [id, txt]);
                return false ;
            });
        });
    };
    
    // 自动初始化组件
    $(document).on("hsReady", ".load-box", function() {
        $(this).find(".select-btn").hsSelect();
    }); $(this).find(".select-btn").hsSelect();
    
    /** 扩展方法 **/
    
    /**
     * 用于表单的 HsForm 组件的 _select 项填充函数
     */
    self.hsFillFormSelect = function(inp, v, n) {
        inp.data("fill")(inp, v, n);
    };
    
    /**
     * 用于选择窗 HsList 组件的 _select 列填充函数
     * 用法:
     * 在 HsList 的 object.config 中增加参数：
     * <param name="_fill__select" value="(_fill__select)" />
     * 然后在列表 head 的选择列加上属性:
     * data-tn="_select"
     * @param {Element} td 当前要填充的格子
     * @param {String} v 在此无意义
     * @param {String} n 在此无意义
     * @context HsList 实例
     */
    self.hsFillListSelect = function(td, v, n) {
        if (this._single_select === undefined) {
            this._single_select = td.closest(".load-box").hasClass("single-select");
        }
        if (this._single_select) {
            HsList.prototype._fill__radio.call(this, td, v, n);
        } else {
            HsList.prototype._fill__check.call(this, td, v, n);
        }
    };
})( jQuery );
