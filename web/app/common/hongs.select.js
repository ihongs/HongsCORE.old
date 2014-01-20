/**
 * 扩展-选择控件
 * 作者：黄弘 <kevin.hongs@gmail.com>
 * 创建: 2013/06/20
 * 修改: 2013/06/21
 * 依赖: jquery.js, jquery.tools.js, hongs.jquery.js
 */

(function($) {
    self.hsInitSelect = function(tip) {
    };
    self.hsFillSelect = function(box, v, n, t) {
        if (t != "info") return;
        box.empty();
        if (v ==  null ) return;
        $.each(v, function(v2, t2) {
            $('<div class="option-box"></div>')
              .append($('<input type="hidden"')
                .attr("name", n)
                .val ( v2 ))
              .append($('<span class="option-txt"></span>')
                .text( t2 ))
              .append($('<span class="option-btn"></span>'))
              .appendTo(box);
        });
        box.append($('<div class="cb"></div>'));
    };

    $.fn.hsSelect = function(conf) {
        /** 选择按钮 **/

        if(!$(this).hasClass("hsSelectBtn")) {
            $(this).addClass("hsSelectBtn");
        }
        else {
            return;
        }

        if (!conf) conf = {};

        var name = conf["name"] || $(this).attr("data-select-name");
        var url  = conf["url"]  || $(this).attr("data-select-url" );
        var box  = conf["box"]  || $(this).attr("data-select-box" );
        var btn = $(this);
        var hsFillSelect = conf["fill"] || self.hsFillSelect;

        if (! box)
            box = $(this).closest(".form-group" ).find(".select-box" );
        else if (typeof box == "string")
            box = /^\$/.test(box) ? $(box.substring(1), this) : $(box);

        $(this).on("click", function() {
            var tip = $.hsOpen(url);
            var ids = {};
            
            tip.find(".cancel").click(function() {
                tip.data("tooltip").hide();
                return false;
            });
            tip.find(".ensure").click(function() {
                btn.trigger("selectBack", [ids]);
                tip.data("tooltip").hide();
                return false;
            });
            tip.find(".check-one").change(function() {
                var id = $(this).val();
                if ($(this).prop("checked")) {
                    btn.trigger("selectItem", [id, ids]);
                    var txt = $(this).closest("tr").find(".name");
                    ids["_"+id] = [id,txt];
                }
                else {
                    if (ids["_"+id] != null) {
                        delete ids["_"+id];
                    }
                }
            });

            var checked = function() {
                var ids = tip.data("ids");
                tip.find(".check-one").each(function() {
                    if (ids["_"+$(this).val()]) {
                        $(this).prop("checked", true );
                    }
                });
            };
            tip.on("loadBack", function() {
                checked();
            }); checked();
        
            // 单选或多选
            if ($(this).hasClass( "single")) {
                    tip.addClass( "single")
                          .find ("._check")
                       .addClass( "_radio")
                    .removeClass( "_check")
                 .attr("data-fn", "_radio");
            }
            else {
                 tip.removeClass( "single")
                          .find ("._radio")
                       .addClass( "_check")
                    .removeClass( "_radio")
                 .attr("data-fn", "_check");
            }

            var ids = {};
            box.find(".option-box").each(function() {
                var id = $(this).find(":hidden").val();
                ids[ "_"+id ] = [ id, $(this).text() ];
            });

            tip.data("btn", $(this))
               .data("ids", ids)
               .load( url );

            $(this).tooltip({tip : tip}).show();
        });
        $(this).on("selectBack", function() {
            hsFillSelect(box, tip.data("ids"), name);
            return false;
        });
        $(this).closest("form").on("loadBack", function(evt, rst) {
            if (rst && rst.select && rst.select[name]) {
                hsFillSelect(box, rst.select[name], name);
            }
        });

        /** 选择菜单 **/

        if(!tip.hasClass("hsSelectTip")) {
            tip.addClass("hsSelectTip");
        }
        else {
            return;
        }

    };
    
    // 自动初始化
    $(document).on("hsReady", ".load-box", function() {
        $(this).find(".select-btn").hsSelect();
    }); $(this).find(".select-btn").hsSelect();
})( jQuery );