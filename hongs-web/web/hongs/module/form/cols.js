
function getTypeName(widgets, type) {
    return widgets.find( "[data-type='"+type+"'] label,legend span:first").text();
}
function getTypeItem(widgets, type) {
    return widgets.find( "[data-type='"+type+"']"   ).clone();
}
function getTypePane(context, type) {
    return context.find(".widget-pane-"+type).first().clone();
}

/**
 * 载入字段配置
 * @param {jQuery} modal
 * @param {jQuery} field
 */
function loadConf(modal, field) {
    modal.find("input").each(function() {
        var name = $(this).attr("name");
        var attr ;
        var pos  = name.indexOf("|");
        if (pos !== -1) {
            attr = name.substring(pos + 1);
            name = name.substring(0 , pos);
            if ($(this).is(":checkbox")) {
                if (/^data-/.test(attr)) {
                    $(this).prop("checked", field.find(name).attr(attr) ? true : false);
                } else {
                    $(this).prop("checked", field.find(name).prop(attr));
                }
            } else {
                $(this).val(field.find(name).attr(attr));
            }
        } else {
            $(this).val(field.find(name).text());
        }
    });
}

/**
 * 设置字段配置
 * @param {jQuery} modal
 * @param {jQuery} field
 */
function saveConf(modal, field) {
    modal.find("input").each(function() {
        var name = $(this).attr("name");
        var attr ;
        var pos  = name.indexOf("|");
        if (pos !== -1) {
            attr = name.substring(pos + 1);
            name = name.substring(0 , pos);
            if ($(this).is(":checkbox")) {
                if (/^data-/.test(attr)) {
                    field.find(name).attr(attr, $(this).prop("checked") ? "true" : "" );
                } else {
                    field.find(name).prop(attr, $(this).prop("checked"));
                }
            } else {
                field.find(name).attr(attr, $(this).val());
            }
        } else {
            field.find(name).text($(this).val());
        }
    });
}

/**
 * 从表单获取字段
 * @param {Array} fields
 * @param {jQuery} area
 */
function gainFlds(fields, area) {
    area.find(".form-group").each(function() {
        var label = $(this).find("label span:first");
        var input = $(this).find("input,select,textarea");
        var disp  = label.text();
        var name  = input.attr("name");
        var type  = input.attr("type");
        var required = input.prop("required") ? "true" : "";
        var repeated = input.prop("multiple") ? "true" : "";
        var params   = {};
        var a = input.get(0).attributes;
        for(var i = 0; i < a.length; i ++) {
            var k = a[i].nodeName ;
            var v = a[i].nodeValue;
            if (v !== ""
            &&  k.substr(0,5) === "data-") {
                params[k.substring(5)] = v;
            }
        }
        if (name === "-") name = "";
        fields.push($.extend({
            "__disp__": disp,
            "__name__": name,
            "__type__": type,
            "__required__": required,
            "__repeated__": repeated
        }, params));
    });
}

/**
 * 向表单设置字段
 * @param {Array} fields
 * @param {jQuery} area
 * @param {jQuery} wdgt
 */
function drawFlds(fields, area, wdgt, pre, suf) {
    for(var i = 0; i < fields.length; i ++) {
        var field = fields[i];
        var disp  = field["__disp__"];
        var name  = field['__name__'];
        var type  = field["__type__"];
        var required = field["__required__"];
        var repeated = field["__repeated__"];
        if (pre) {
            name  = pre + name;
        }
        if (suf) {
            name  = name + suf;
        }
        var group = getTypeItem(wdgt, type);
        var label = group.find("label span:first");
        var input = group.find("input,select,textarea");
        label.text(disp);
        input.attr("name", name);
        input.prop("required", !! required);
        input.prop("multiple", !! repeated);
        for(var k in field )  {
            if (/^_/.test(k)) {
                continue;
            }
            var v = field[k];
            k = "data-" + k ;
            input.attr(k, v);
        }
        area.append(group);
    }
}
