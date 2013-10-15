/**
 * 扩展-选择控件
 * 作者：黄弘 <kevin.hongs@gmail.com>
 * 创建: 2013/06/20
 * 修改: 2013/06/21
 * 依赖: jquery.js, jquery.tools.js, jquery.hongs.js
 */

(function($) {
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

        var box  = conf["box"]  || $(this).attr("data-select-box" );
        var tip  = conf["tip"]  || $(this).attr("data-select-tip" );
        var url  = conf["url"]  || $(this).attr("data-select-url" );
        var name = conf["name"] || $(this).attr("data-select-name");
        var hsFillSelect = conf["fill"] || self.hsFillSelect;

        if (box && typeof box == "string")
            box = /^\$/.test(box) ? $(box.substring(1), this) : $(box);
        else
            box = $(this).closest(".input-box").find(".select-box");
        if (tip && typeof tip == "string")
            tip = /^\$/.test(tip) ? $(tip.substring(1), this) : $(tip);
        else
            tip = $("#select-tip");

        $(this).on("click", function() {
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

            tip.data("ids", ids)
               .data("btn", $(this))
               .data("HsList").load(url);

            $(this).tooltip({tip : tip}).show();
        });
        $(this).on("selectBack", function() {
            hsFillSelect(box, tip.data("ids"), name);
            return false;
        });

        /** 选择菜单 **/

        if(!tip.hasClass("hsSelectTip")) {
            tip.addCalss("hsSelectTip");
        }
        else {
            return;
        }

        tip.find(".ensure").click(function() {
            tip.data("btn").trigger("selectBack");
            tip.data("tooltip").hide();
            return false;
        });
        tip.find(".cancel").click(function() {
            tip.data("tooltip").hide();
            return false;
        });
        tip.find(".check-one").change(function() {
            var ids = tip.data("ids");
            var id  = $(this).val(  );
            if ($(this).prop("checked")) {
                var txt = $(this).closest("tr").find(".name");
                ids["_"+id] = [id,txt];
            }
            else {
                if (ids["_"+id] != null) {
                    delete ids["_"+id];
                }
            }
        });
        tip.on("loadBack", function() {
            var ids = tip.data("ids");
            tip.find(".check-one").each(function() {
                if (ids["_"+$(this).val()]) {
                    $(this).prop("checked", true );
                }
            });
        });
    };
})( jQuery );