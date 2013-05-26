/*
HongsCORE(Javascript)
作者: 黄弘 <kevin.hongs@gmail.com>
创建: 2013/01/01
修改: 2013/05/25

自定义属性:
data-fn HsForm|HsList|HsTree中为field name
data-pn HsForm中为param name, HsList中为page num
data-vk HsForm中为value key
data-tk HsForm中为text key
data-tp HsList中为操作菜单的tooltip params
data-equals 表单验证的是否相等
data-unique 表单验证的是否唯一
data-eval 自动执行
data-load 自动加载
data-open 自动打开
data-load-in 点击后在指定区域加载
data-open-in 点击后在指定区域打开

功能类说明:
.load 点击后在所在区域加载
.open 点击后在所在区域打开
.open-new 点击后在浮窗打开
.close 浮窗关闭按钮
.cancel 表单取消按钮
.check-all
.check-one
.bind-to-select
.bind-to-single

定位类说明:
.form-checks
.form-radios
.list-box
.page-box
.find-box
.tree-box
.tree-list
.tree-node
.tree-hand
.tree-name
.tree-cnum
*/

if(typeof(HsCONF)=="undefined")HsCONF={};
if(typeof(HsLANG)=="undefined")HsLANG={};
if(typeof(HsAUTH)=="undefined")HsAUTH={};

/**
 * 快捷方式
 * 说明(首参数以下列字符开头的意义):
 * .    获取配置
 * :    获取语言
 * /    补全路径
 * ?    检查权限
 * %    获取树纵深值, 第二个参数指定树
 * &    获取单个参数值, 第二个参数指定参数容器
 * @    获取多个参数值, 第二个参数指定参数容器
 */
function H$() {
    var a = arguments[0];
    var b = a.charAt (0);
    arguments[0] = a.substring(1);
    switch (b) {
    case '.': return hsGetConf.apply(this, arguments);
    case ':': return hsGetLang.apply(this, arguments);
    case '?': return hsChkUri .apply(this, arguments);
    case '/': return hsFixUri .apply(this, arguments);
    case '%':
        if (arguments.length == 1)
            throw("H$: Argument 2 required by flag '%'!");
        return hsGetValue(  arguments[1], arguments[0]  );
    case '&':
    case '@':
        if (arguments.length == 1)
            arguments[1] = location.href;
        if (typeof arguments[1] != "string") {
            if (jQuery.isArray(arguments[1]) == false) {
                var c = jQuery(arguments[1]).closest(".load-box");
                var d = hsSerialArr(c.data("url" ));
                var e = hsSerialArr(c.data("data"));
                for (var i = 0; i < e.length; i ++)
                    d.push(e[i]);
                arguments[1] = d;
            }
            if (b == '@')
                return hsGetSerias(arguments[1], arguments[0]);
            else
                return hsGetSeria (arguments[1], arguments[0]);
        }
        else {
            if (b == '@')
                return hsGetParams(arguments[1], arguments[0]);
            else
                return hsGetParam (arguments[1], arguments[0]);
        }
    default: throw("H$: Wrong flag '"+b+"'");
    }
}

function hsResponObj(rst) {
    if (typeof rst.responseText != "undefined") {
        rst  = rst.responseText;
    }
    if (typeof rst == "string") {
    if (rst.charAt( 0 ) == '{') {
        if (typeof JSON != "undefined") {
            rst  = JSON.parse( rst );
        }
        else {
            rst  = eval('('+rst+')');
        }
    }
    else
    if (rst.charAt( 0 ) == '<') {
        hsWarn(rst);
        rst = {
            "__success__" : false
        };
    }
    else {
        rst = {
            "__success__" : false,
            "__message__" :  rst
        };
    }
    }
    if (typeof rst == "object") {
        if (typeof rst["__success__"] == "undefined") {
            rst["__success__"] = true;
        }
        if (typeof rst["__message__"] == "undefined") {
            rst["__message__"] =  "" ;
        }
        if (rst["__message__"]) {
        if (rst["__success__"]) {
            hsNote(rst["__message__"]);
        }
        else {
            alert (rst["__message__"]);
        }
        }
        if (typeof rst["__refresh__"] != "undefined") {
            window.location.href = rst["__refresh__"];
        }
    }
    return rst;
}

function hsSerialArr(obj) {
    var arr = [];
    if (typeof obj == "string") {
        var a1, a2;
        a1 = obj.split('#' , 2);
        if (a1.length > 1) obj = a1[0];
        a1 = obj.split('?' , 2);
        if (a1.length > 1) obj = a1[1];
        a1 = obj.split('&');
        for(var i = 0; i < a1.length; i++) {
            a2 = a1[i].split('=', 2 );
            if (a2.length < 2 || !a2[0] || !a2[1]) break;
            arr.push({name : decodeURIComponent(a2[0]),
                      value: decodeURIComponent(a2[1])});
        }
    }
    else if (jQuery.isPlainObject(obj)) {
        _hsWalkInto(obj, function(val,key) {
            if (key.length < 1 || val.length < 1) return;
            arr.push({name : key.join("."),
                      value: val});
        });
    }
    else if (!jQuery.isArray(obj) && typeof obj == "object") {
        arr = jQuery(obj).serializeArray();
    }
    return arr;
}
function hsSerialObj(obj) {
    var arr = hsSerialArr(obj);
    obj = {};
    for (var i = 0; i < arr.length; i ++) {
        obj[arr[i].name] = arr[i].value;
    }
    return obj;
}
function hsGetSerias(arr, name) {
    var val = [];
    for(var i = 0; i < arr.length ; i ++) {
        if (arr[i]["name"] == name) {
            val.push(arr[i]["value"]);
        }
    }
    return val;
}
function hsSetSerias(arr, name, value) {
    for(var j = arr.length-1; j > -1; j --) {
        if (arr[j]["name"] == name) {
            arr.splice(j, 1);
        }
    }
    for(var i = 0; i < value.length ; i ++) {
        arr.push({name: name, value: value[i]});
    }
}
function hsGetSeria (arr, name) {
    var val = hsGetSerias(arr, name);
    if (val.length) return val.pop();
    else            return "";
}
function hsSetSeria (arr, name, value) {
    hsSetSerias(arr, name, [value]);
}

function hsGetParams(url, name) {
    name = encodeURIComponent(name).replace('.', '\\.');
    var reg = new RegExp("[\\?&]"+_hsEscParam(name)+"=([^&]*)", "g");
    var arr = null;
    var val = [];
    while ( (arr = reg.exec(url)) != null ) {
        val.push(decodeURIComponent(arr[1]));
    }
    return val;
}
function hsSetParams(url, name, value) {
    name = encodeURIComponent(name).replace('.', '\\.');
    var reg = new RegExp("[\\?&]"+_hsEscParam(name)+"=([^&]*)", "g");
    url = url.replace(reg, "");
    for (var i = 0; i < value.length; i ++)
    {
        url += "&"+encodeURIComponent(name)
            +  "="+encodeURIComponent(value[i]);
    }
    if (url.indexOf("?") < 0 ) {
        url = url.replace("&", "?");
    }
    return url;
}
function hsGetParam (url, name) {
    var val = hsGetParams(url, name);
    if (val.length) return val.pop();
    else            return "";
}
function hsSetParam (url, name, value) {
    return hsSetParams(url, name, [value]);
}

function hsGetValue (obj, path, def) {
    if (typeof path != "string") {
        throw("hsGetValue: 'path' must be a string");
    }
    path = path.replace(/\]\[/g, ".")
               .replace(/\[/   , ".")
               .replace(/\]/   , "" )
               .replace(/\.+$/ , "" )
               .split  (/\./ );
    return hsGetArray(obj, path, def);
}
function hsGetArray (obj, keys, def) {
    if (!jQuery.isPlainObject(obj)) {
        throw("hsGetArray: 'obj' must be an object");
    }
    if (!jQuery.isArray(keys)) {
        throw("hsGetArray: 'keys' must be an array");
    }
    if (!keys.length) {
        throw("hsGetArray: 'keys' can not be empty");
    }

    var i , k;
    for(i = 0; i < keys.length; i++) {
        k = keys[i];
        if(typeof obj[k] != "undefined") {
           obj  = obj[k];
           continue;
        }
        return  def;
    }
        return  obj;
}
/**
 需要注意的键:
 a[1]   数字将作为字符串对待, 但可用hsSetArray完成
 a[][k] 空键将作为字符串对待, 但放在末尾可表示push
 */
function hsSetValue (obj, path, val) {
    if (typeof path != "string") {
        throw("hsSetValue: 'path' must be a string");
    }
    path = path.replace(/\]\[/g, ".")
               .replace(/\[/   , ".")
               .replace(/\]/   , "" )
               .split  (/\./ );
    hsSetArray(obj, path, val);
}
function hsSetArray (obj, keys, val) {
    if (!jQuery.isPlainObject(obj)) {
        throw("hsSetArray: 'obj' must be an object");
    }
    if (!jQuery.isArray(keys)) {
        throw("hsSetArray: 'keys' must be an array");
    }
    if (!keys.length) {
        throw("hsSetArray: 'keys' can not be empty");
    }

    var i , k, t = keys[0];
    for(i = 0; i < keys.length-1; i ++ ) {
        k = keys[ i ];
        t = keys[i+1];
        if (!t) t = 0;
        if (typeof t == "number")
            if (!jQuery.isArray(obj[k])) {
                obj[k] = [];
            }
        else
        if (typeof t == "string")
            if (!jQuery.isPlainObject(obj[k])) {
                obj[k] = {};
            }
        else
            throw("hsSetArray: key must be a string or number");
        obj = obj[k];
    }
    if (t) obj[t] = val ;
    else   obj.push(val);
}

function hsGetConf  (key, def) {
    if (typeof HsCONF[key] != "undefined") {
        return HsCONF[key];
    }
    else {
        return def;
    }
}
function hsGetLang  (key, rep) {
    if (typeof HsLANG[key] != "undefined") {
        key  = HsLANG[key];
    }

    if (rep instanceof Array ) {
        var i, x = {};
        for(i in rep) {
            x[i + ""] = rep[i];
        }
        rep = x;
    }
    if (rep instanceof Object) {
        key = key.replace( /\$(\{\w+\}|\w+)/gm, function(w) {
            if (w.substring(0 , 2) == "${") {
                w = w.substring(2, w.length - 1);
            }
            else {
                w = w.substring(1);
            }
            if (typeof(rep[w]) != "undefined") {
                return rep[w];
            }
            else {
                return "";
            }
        });
    }

    return key;
}
function hsChkUri   (uri, pms) {
    uri = hsFixUri(uri) + ".de";

    if (typeof HsAUTH[uri] == "undefined") {
        jQuery.ajax({
            "url"       :  uri,
            "data"      :  pms,
            "type"      : "POST",
            "async"     :  false,
            "cache"     :  false,
            "success"   :  function( rst ) {
                HsAUTH[uri] = rst == "OK";
            }
        });
    }
    return HsAUTH[uri];
}
function hsFixUri   (uri) {
    if (/^(https?:\/\/|\/|\.)/.test(uri) == false)
        return hsGetConf("BASE_HREF") + "/" + uri;
    else
        return uri;
}

/**
 * 格式化数字
 *
 * @author HuangHong
 * @param num:Number
 * @param len:Number 总长度(不含小数点)
 * @param dec:Number 小数位
 * @param sep:String 千分符
 * @param dot:String 小数点
 * @return String
 */
function hsFmtNum(num, len, dec, sep, dot) {
  if (typeof len == "undefined") {
    len = 0;
  }
  if (typeof dec == "undefined") {
    dec = 2;
  }
  if (typeof sep == "undefined") {
    sep = ",";
  }
  if (typeof dot == "undefined") {
    dot = ".";
  }

  // 四舍五入
  var o = hsParseInt(num);
  var p = Math.pow(10, dec);
  num = (Math.round(o * p) / p).toString();

  var a = num.split(".", 2);
  if (a.length < 2) {
    a[1] = "0";
  }
  var n = a[0];
  var d = a[1];

  // 右侧补零
  var nl = n.length;
  for (var i = nl; i < len; i ++) {
    n = "0" + n;
  }

  // 左侧补零
  var dl = d.length;
  for (var j = dl; j < dec; j ++) {
    d = d + "0";
  }

  num = "";
  dec = "";

  // 添加分隔符
  if (sep) {
    var k, s = "";
    // 整数部分从右往左每3位分割
    while (n != "") {
      k = n.length - 3;
      s = n.substring(k);
      n = n.substring(0, k);
      num = s + sep + num;
    }
    // 整数部分扔掉最右边一位
    if (num) {
      k = num.length - 1;
      num = num.substring(0, k);
    }
    // 小数部分从左往右每3位分割
    while (d != "") {
      s = d.substring(0, 3);
      d = d.substring(3);
      dec = dec + sep + s;
    }
    // 小数部分扔掉最左边一位
    if (dec) {
      dec = dec.substring(1);
    }
  }
  else {
    num = n;
    dec = d;
  }

  // 组合整数位和小数位
  return num + dot + dec;
}

/**
 * 解析日期
 *
 * @author HuangHong
 * @param text:String
 * @param format:String
 * @return Date
 */
function hsPrsDate(text, format) {
  var a = text.split(/\W+/);
  var b = format.match(/M+|d+|y+|H+|k+|K+|h+|m+|s+|S+|E+|a+/g);

  var i, j;
  var M, d, y, H = 0, m = 0, s = 0, A = 0;

  for (i = 0; i < b.length; i ++) {
    if (a[i] == null) continue;

    var wrd = a[i];
    var len = b[i].length;
    var flg = b[i].substring(0, 1);
    switch (flg) {
      case 'M':
        if (len >= 4) {
          for (j = 0; j < hsLang("date.format.LM").length; j ++) {
            if (wrd == hsLang("date.format.LM")[j]) {
              M = j;
              break;
            }
          }
        }
        else if (len == 3) {
          for (j = 0; j < hsLang("date.format.SM").length; j ++) {
            if (wrd == hsLang("date.format.SM")[j]) {
              M = j;
              break;
            }
          }
        }
        else {
          M = parseInt(wrd, 10);
        }
      break;
      case 'd':
        d = parseInt(wrd, 10);
      break;
      case 'y':
        y = parseInt(wrd, 10);
        if (len <= 2) {
          y += y > 29 ? 1900 : 2000;
        }
      break;
      case 'H':
      case 'K':
        H = parseInt(wrd, 10);
      break;
      case 'k':
      case 'h':
        H = parseInt(wrd, 10) - 1;
      break;
      case 'm':
        m = parseInt(wrd, 10);
      break;
      case 's':
        s = parseInt(wrd, 10);
      break;
      case 'a':
        if (len >= 4) {
          for (j = 0; j < hsLang("date.format.La").length; j ++) {
            if (wrd == hsLang("date.format.La")[j]) {
              A = j;
              break;
            }
          }
        }
        else {
          for (j = 0; j < hsLang("date.format.Sa").length; j ++) {
            if (wrd == hsLang("date.format.Sa")[j]) {
              A = j;
              break;
            }
          }
        }
      break;
    }
  }

  if (A == 1) {
    H += 12;
  }

  var text2;
  if (typeof(M) != "undefined"
  &&  typeof(d) != "undefined"
  &&  typeof(y) != "undefined") {
    text2 = M+"/"+d+"/"+y+" "+H+":"+m+":"+s;
  }
  else {
    text2 = H+":"+m+":"+s;
  }

  return new Date(Date.parse(text2));
}

/**
 * 格式化日期
 *
 * @author HuangHong
 * @param date:Date
 * @param format:String
 * @return String
 */
function hsFmtDate(date, format) {
  if (typeof(date) == "string") {
    if (/^\d*$/.test(date)) {
      date = parseInt(date);
    }
    else {
      date = Date.parse(date);
    }
  }
  if (typeof(date) == "number") {
    if (date <= 2147483647) {
      date = date * 1000;
    }
    date = new Date(date);
  }

  var y = date.getFullYear();
  var M = date.getMonth();
  var d = date.getDate();
  var H = date.getHours();
  var k = H + 1;
  var K = H > 11 ? H - 12 : H;
  var h = H > 12 ? H - 12 : H;
  var m = date.getMinutes();
  var s = date.getSeconds();
  var S = date.getMilliseconds();
  var E = date.getDay( );
  var a = H < 12 ? 0 : 1;

  if (K == 12) K = 0;
  if (h == 0) h = 12;

  function _addzero(num, len) {
    num = num ? num : "";
    num = num.toString();
    var gth = num.length;
    for (var i = gth; i < len; i ++) {
      num = "0" + num;
    }
    return num;
  }

  function _replace(mat) {
    var len = mat.length;
    var flg = mat.substring(0, 1);
    switch (flg) {
      case 'y':
        var x = _addzero(y, len);
        if (len <= 2) {
          return x.substring(x.length - len);
        }
        else {
          return x;
        }
      case 'M':
        if (len >= 4) {
          return hsLang("date.format.LM")[M];
        }
        else if (len == 3) {
          return hsLang("date.format.SM")[M];
        }
        else {
          return _addzero(M + 1, len);
        }
      case 'd':
        return _addzero(d, len);
      case 'H':
        return _addzero(H, len);
      case 'k':
        return _addzero(k, len);
      case 'K':
        return _addzero(K, len);
      case 'h':
        return _addzero(h, len);
      case 'm':
        return _addzero(m, len);
      case 's':
        return _addzero(s, len);
      case 'S':
        return _addzero(S, len);
      case 'E':
        if (len >= 4) {
          return hsLang("date.format.LE")[E];
        }
        else {
          return hsLang("date.format.SE")[E];
        }
      case 'a':
        if (len >= 4) {
          return hsLang("date.format.La")[a];
        }
        else {
          return hsLang("date.format.Sa")[a];
        }
    }
  }

  return format.replace(/M+|d+|y+|H+|k+|K+|h+|m+|s+|S+|E+|a+/g, _replace);
}

function hsNote(msg) {
    var box = jQuery("#note-box");
    var div = jQuery('<div class="note-msg""></div>');
    div.appendTo(box).append(msg).hide();
        div.effect("slide",{mode:"show",direction:"up"},500);
    setTimeout( function() {
        div.effect("slide",{mode:"hide",direction:"up"},500,
                function() {div.remove();});
    },  5000);
    return div;
}
function hsWarn(htm) {
    var div = jQuery('<div class="overlay warn-box"><div class="close"></div><div class="warn-msg"></div></div>');
    var box = div.find('.warn-msg');
    div.appendTo(document.body)
       .overlay({
        top     : "center",
        left    : "center",
        load    :  true ,
        mask    : {
            color       : "#000",
            opacity     : 0.8,
            loadSpeed   : 0
        },
          close : div.find(".close"),
        onClose : function() {
            HsClose.call  (   box   );
            box.removeData("overlay");
            div.remove();
        }
    });
    box.data("overlay", div.data("overlay"));
    box.html(htm);
    return box;
}

function hsOpen(url, data, callback) {
    var div = jQuery('<div class="overlay open-box"><div class="close"></div><div class="load-box"></div></div>');
    var box = div.find('.load-box');
    div.appendTo(document.body)
       .overlay({
        top     : "center",
        left    : "center",
        load    :  false,
        mask    : {
            color       : "#000",
            opacity     : 0.8,
            loadSpeed   : 0
        },
          close : div.find('.close' ),
        onClose : function() {
            HsClose.call  (   box   );
            box.removeData("overlay");
            div.remove();
        }
    });
    box.data("overlay", div.data("overlay"));
    box.load(url, data, callback);
    return box;
}
function HsOpen(url, data, callback) {
    var box = jQuery(this);
    if (box.closest(".tabs").length) {
        var tabs = box.closest(".tabs").data("tabs");
        var oldTab = tabs.getCurrentTab();
        box.show().click();
        var curTab = tabs.getCurrentTab();
        box = tabs.getCurrentPane();
        box.data("oldTab", oldTab );
        box.data("curTab", curTab );
        box.data("tabs", tabs);
    }
    else {
        var baks= box.contents().detach();
        box.data("baks", baks);
    }
    box.load(url, data, callback);
    return box;
}
function HsClose() {
    var box = jQuery(this);
    box.trigger("hsClose");
    if (box.data("overlay")) {
        box.data("overlay").close();
    }
    else if (box.data("tabs")) {
        var oldTab = box.data("oldTab");
        var curTab = box.data("curTab");
        oldTab.click( ); curTab.show( );
    }
    else if (box.data("baks")) {
        var baks = box.data("baks");
        box.contents(  ).remove(  );
        box.append(baks).removeData("baks");
    }
    return box;
}
function HsReady() {
    var box = jQuery(this);
    box.trigger("hsReady");
    box.find("show-in-overlay,.show-in-tabs,.show-in-load").hide();
    if (box.data( "overlay" )) {
        box.find(".show-in-overlay").show();
        box.data( "overlay" ).load();
    }
    else if (box.data("tabs")) {
        box.find(".show-in-tabs").show();
    }
    else {
        box.find(".show-in-load").show();
    }
    box.find(".close,.cancel").click(function() {
        HsClose.call(box );
    });
    return box;
}

function HsForm(opts, context) {
    var data = _HsInitOpts.call(this, opts, "HsForm");
    if (data)  return data;

        context  = jQuery( context);
    var loadBox  = context.closest(".load-box");
    var formBox  = context.find   ( "form"    );
    var saveUrl  = hsGetValue(opts, "saveUrl" );
    var loadUrl  = hsGetValue(opts, "loadUrl" );
    var loadNoLd = hsGetValue(opts, "loadNoLd");
    var loadNoId = hsGetValue(opts, "loadNoId");
    var idKey    = hsGetValue(opts, "idKey", "id");

    if (formBox.length === 0) formBox = context;

    this.context = context;
    this.loadBox = loadBox;
    this.formBox = formBox;

    var ld, id, a, i;

    ld = hsSerialArr(loadUrl );
    if (!loadNoLd) {
        a = hsSerialArr(loadBox.data("url" ));
        for(i = 0; i < a.length; i ++) {
            ld.push(a[i]);
        }
        a = hsSerialArr(loadBox.data("data"));
        for(i = 0; i < a.length; i ++) {
            ld.push(a[i]);
        }
    }
    id = hsGetSerias(ld,idKey);
    if (loadNoId || id.length) {
        this.load(loadUrl, ld);
    }
    else {
        this.fillData([ ]);
    }

    /**
     * 使用初始化数据填充表单
     * 在打开表单窗口时, 可能指定一些参数(如父ID, 初始选中项等)
     * 这时有必要将这些参数值填写入对应的表达项, 方便初始化过程
     */
    var n, v;
    for(i = 0; i < ld.length; i ++) {
        n = ld[i].name;
        v = ld[i].value;
        formBox.find("[name='"+n+"']").val(v);
        formBox.find("[data-pn='"+n+"']").val(v);
    }

    this.saveInit(saveUrl);

    this.validator = formBox.validator().data("validator");
}
HsForm.prototype = {
    load     : function(url, data) {
        jQuery.ajax({
            "url"       : url,
            "data"      : data,
            "type"      : "POST",
            "dataType"  : "json",
            "async"     : false,
            "context"   : this,
            "success"   : this.loadBack
        });
    },
    loadBack : function(rst) {
        rst = hsResponObj(rst);
        if (rst.__success__ == false) return;
        if (rst.data) this.fillData( rst.data );
        if (rst.info) this.fillInfo( rst.info );
        this.formBox.trigger("loadBack", [rst]);
    },
    fillData : function(data) {
        var nodes, datas, i, n, v, inp, opt, lab, vk, tk, tp;
        nodes = this.formBox.find("select[name],.form-checks[name],.form-radios[name]");
        datas = {};
        for(i = 0; i < nodes.length; i ++) {
            n = jQuery(nodes[i]).attr( "name" );
            if (typeof datas[n] != "undefined")
                continue;
            v = hsGetValue(data, n);
            if (typeof v == "undefined")
                continue;
            datas[n] = v;
        }

        this._data = data;
        for(n in datas) {
            v = datas[n];
            i = 0;
            inp = this.formBox.find( '[data-fn="'+n+'"]');
            if (inp.length == 0) {
                i = 1;
                inp = this.formBox.find('[name="'+n+'"]');
            }

            if (typeof this["fill_"+n] !="undefined") {
                v = this["fill_"+n].call(this, inp, v, n, "data");
                if (! v) continue;
            }

            if (inp.hasClass(".form-checks") || inp.hasClass(".form-radios")) {
                tp = inp.hasClass(".form-checks") ? "checkbox" : "radio";
                vk = inp.attr("data-vk"); if(!vk) vk = 0;
                tk = inp.attr("data-tk"); if(!tk) tk = 1;
                for (i = 0; i < v.length; i ++) {
                    lab = jQuery('<label><input type="'+tp+'"/><span></span></label>');
                    lab.find("input").attr("name", n)
                                     .val (hsGetValue(v[i], vk));
                    lab.find("span" ).text(hsGetValue(v[i], tk));
                    inp.append(lab);
                }
            }
            else if (inp.atrr("tagName") == "SELECT" && i == 1) {
                vk = inp.attr("data-vk"); if(!vk) vk = 0;
                tk = inp.attr("data-tk"); if(!tk) tk = 1;
                for (i = 0; i < v.length; i ++) {
                    opt = jQuery('<option></option>');
                    opt.val (hsGetValue(v[i], vk));
                    opt.text(hsGetValue(v[i], tk));
                    inp.append(opt);
                }
            }
        }
        delete this._data;
    },
    fillInfo : function(info) {
        var nodes, infos, i, n, v, inp;
        nodes = this.formBox.find("input[name],select[name],textarea[name],.form-checks[name],.form-radios[name]");
        infos = {};
        for(i = 0; i < nodes.length; i ++) {
            n = jQuery(nodes[i]).attr( "name" );
            if (typeof infos[n] != "undefined")
                continue;
            v = hsGetValue(info, n);
            if (typeof v == "undefined")
                continue;
            infos[n] = v;
        }

        this._info = info;
        for(n in infos) {
            v = infos[n];
            i = 0
            inp = this.formBox.find( '[data-fn="'+n+'"]');
            if (inp.length == 0) {
                i = 1;
                inp = this.formBox.find('[name="'+n+'"]');
            }

            if (typeof this["fill_"+n] !="undefined") {
                v = this["fill_"+n].call(this, inp, v, n, "info");
                if (! v) continue;
            }

            if (i == 0) {
                inp.text(v);
            }
            else {
                inp.val (v);
            }
        }
        delete this._info;
    },

    saveInit : function(act) {
        var url  = this.formBox.attr("action" ) || act;
        var type = this.formBox.attr("method" );
        var enct = this.formBox.attr("enctype");
        var data = this.formBox;
        var that = this;

        data.on("submit", function( evt ) {
            return that.validator.checkValidity();
        });

        if (enct ===  "multipart/form-data") {
            if (data.attr("target") == null) {
                var name  = "_" + Math.floor(Math.random() * 1000000000);
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
        }
        else {
            data.on("submit", function( evt ) {
                if (evt.isDefaultPrevented()) {
                    return;
                }
                evt.preventDefault( );
                jQuery.ajax({
                    "url"       : url,
                    "data"      : hsSerialArr(data),
                    "type"      : type?type: "POST",
                    "dataType"  :"json",
                    "async"     : false,
                    "cache"     : false,
                    "context"   : that,
                    "complete"  : that.saveBack
                });
            });
        }
    },
    saveBack : function(rst) {
        rst = hsResponObj(rst);
        if (typeof rst.__success__ != "undefined"
        &&         rst.__success__ ==  false     ) {
            if (typeof rst.errors  != "undefined") {
                this.invalidate ( rst.errors );
            }
            return;
        }
        var evt = new jQuery.Event("saveBack");
        this.formBox.trigger(evt, [rst]);
        if (evt.isDefaultPrevented() == false)
            HsClose.call( this.loadBox );
    }
};

function HsList(opts, context) {
    var data = _HsInitOpts.call(this, opts, "HsList");
    if (data)  return data;

        context  = jQuery( context);
    var loadBox  = context.closest(".load-box");
    var listBox  = context.find   (".list-box");
    var pageBox  = context.find   (".page-box");
    var findBox  = context.find   (".find-box");
    var loadUrl  = hsGetValue(opts, "loadUrl" );
    var openUrls = hsGetValue(opts, "openUrls");
    var sendUrls = hsGetValue(opts, "sendUrls");
    this.idKey   = hsGetValue(opts, "idKey"  , "id"  );
    this.pageKey = hsGetValue(opts, "pageKey", "page");

    this.context = context;
    this.loadBox = loadBox;
    this.listBox = listBox;
    this.pageBox = pageBox;

    var i, a, n, m, u;
    var that  =  this;

    function openHand(evt) {
        //var n = evt.data[0];
        var n = jQuery(this);
        var m = evt.data[1];
        var u = evt.data[2];

        /*
        if (typeof n == "string")
            n = loadBox.find(n);
        else if (n)
            n = jQuery(n);
        */
        if (typeof m == "string")
            m = loadBox.find(m);
        else if (m)
            m = jQuery(m);

        var t = n.closest(".tooltip");
        if (t.length)
            n = t.data   ( "trigger");

        if (typeof u == "function") {
            u.call(n, m, that);
            return;
        }

        if (0 <= u.indexOf("$ID") ) {
            var cks;
            if (0 <= jQuery.inArray(listBox[0], n.parents())) {
                cks = that.getRow(n);
            }
            else {
                cks = that.getOne( );
            }
            if (cks == null) return ;

            u = u.replace("$ID", encodeURIComponent(cks.val()));
        }

        that.open( n, m, u );
    }

    if (openUrls)
    for(i = 0; i < openUrls.length; i ++) {
        a = openUrls[i]; m = undefined;
        switch (a.length) {
        case 3:
            n = a[0];
            m = a[1];
            u = a[2];
            break;
        case 2:
            n = a[0];
            u = a[1];
            break;
        default:
            continue;
        }

        if (typeof n == "string")
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
        if (typeof n == "string")
            n = loadBox.find(n);
        else if (n)
            n = jQuery(n);
        */

        var t = n.closest(".tooltip");
        if (t.length)
            n = t.data   ( "trigger");

        if (typeof u == "function") {
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
            m = a[1];
            u = a[2];
            break;
        case 2:
            n = a[0];
            u = a[1];
            break;
        default:
            continue;
        }

        if (typeof n == "string")
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

    this.listBox.on("change", ".check-all:checkbox", function() {
        that.listBox.find(".check-one")
                    .prop("checked", jQuery(this).prop( "checked" ))
                    .trigger( "change");
    });
    this.listBox.on("change", ".check-one:checkbox", function() {
        that.listBox.find(".check-all")
                    .prop("checked", that.listBox.find(".check-one").length  ==
                                     that.listBox.find(".check-one:checked").length);
    });

    this["fill_"+this.idKey] = HsList.fill_check;

    if (loadUrl) this.load(loadUrl, []);
}
HsList.prototype = {
    load     : function(url, data) {
        if (url ) this._url  = url;
        if (data) this._data = hsSerialArr(data);
        jQuery.ajax({
            "url"       : this._url ,
            "data"      : this._data,
            "type"      :"POST",
            "dataType"  :"json",
            "context"   : this,
            "complete"  : this.loadBack
        });
    },
    loadBack : function(rst) {
        rst = hsResponObj(rst);
        if (rst.__success__ == false) return;
        if (rst.list) this.fillList( rst.list );
        if (rst.page) this.fillPage( rst.page );
        this.listBox.trigger("loadBack", [rst]);
    },
    fillList : function(list) {
        var tb, tr, td, tds, cls, fns, i, j, n, v, a ;
        tb  = this.listBox.find("tbody"); tb.empty( );
        tds = this.listBox.find("thead th, thead td");
        cls = []; fns = [];
        for (i = 0; i < tds .length; i ++) {
            td = jQuery(tds[i]);
            cls.push(td.attr( "class" ));
            fns.push(td.attr("data-fn"));
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

                if (typeof this["fill_"+n] != "undefined") {
                    v = this["fill_"+n].call(this, td,v,n);
                    if(!v) continue;
                }

                td.text(v);
            }
        }
        if (typeof this._info != "undefined")
            delete this._info;
    },
    fillPage : function(page) {
        if (page.errno == 1) {
            this.pageBox.empty().append('<span>'+hsGetLang('list.empty')+'</span>');
            return;
        }

        var i, p, t, pn, pmin, pmax, that = this;
        p  = page.page || 1;
        t  = page.total_pages || 1;
        pn = this.pageBox.attr("data-pn");
        pn = pn ? parseInt(pn) : 10;
        pmin = Math.floor((p - 1) / pn) * pn + 1;
        pmax = pmin+pn - 1; if (t<pmax) pmax = t;
        this.pageBox.empty();

        if (1 != p) {
            this.pageBox.append(jQuery('<button data-pn="'+(p-1)+'">'+hsGetLang("list.prev.page")+'</button>'));
        }
        if (1 < pmin-1) {
            this.pageBox.append(jQuery('<button data-pn="'+1+'">'+1+'</button>'));
            this.pageBox.append(jQuery('<span>...</span>'));
            this.pageBox.append(jQuery('<button data-pn="'+(pmin-1)+'">'+(pmin-1)+'</button>'));
        }
        for(i = pmin; i < pmax+1; i++) {
            this.pageBox.append(jQuery('<button data-pn="'+i+'">'+i+'</button>'));
        }
        if (t > pmax+1) {
            this.pageBox.append(jQuery('<button data-pn="'+(pmax+1)+'">'+(pmax+1)+'</button>'));
            this.pageBox.append(jQuery('<span>...</span>'));
            this.pageBox.append(jQuery('<button data-pn="'+t+'">'+t+'</button>'));
        }
        if (t != p) {
            this.pageBox.append(jQuery('<button data-pn="'+(p+1)+'">'+hsGetLang("list.next.page")+'</button>'));
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
        jQuery.ajax({
            "url"       : url,
            "data"      : data,
            "type"      : "POST",
            "dataType"  : "json",
            "context"   : this,
            "complete"  : function(rst) {
                this.sendBack(btn, rst, dat2);
            }
        });
    },
    sendBack : function(btn, rst, data) {
        rst = hsResponObj(rst);
        if (rst.__success__ == false) return;
        var evt = new jQuery.Event("sendBack");
        btn.trigger(evt, [rst, data]);
        if (evt.isDefaultPrevented()) return;
        this.load();
    },

    open     : function(btn, box, url, data) {
        var that = this;
        var dat2 = jQuery.extend({}, hsSerialObj(url), hsSerialObj(data||{}));
        if (box == "@") box = jQuery(btn).parent(".load-box");
        if (box)
            HsOpen.call(box, url, data, function() {
               that.openBack(btn, this, dat2);
            });
        else
            hsOpen     (     url, data, function() {
               that.openBack(btn, this, dat2);
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

    /** 填充函数 **/

    fill__check : function(td, v, n) {
        var ck = this.listBox.find('thead [data-fn="'+n+'"] .check-all');
        jQuery('<input type="checkbox" class="check-one"/>')
            .attr("name", ck.attr("name"))
            .val (hsGetValue(this._info, this.idKey))
            .appendTo(td);
    },
    fill__radio : function(td, v, n) {
        var ck = this.listBox.find('thead [data-fn="'+n+'"] .check-all');
        jQuery('<input type="radio" class="check-one"/>')
            .attr("name", ck.attr("name"))
            .val (hsGetValue(this._info, this.idKey))
            .appendTo(td);
    },
    fill__admin : function(td, v, n) {
        var box = this.loadBox.find('.'+n+'-btn');
        if (box.length) {
            box.contents( ).clone( ).appendTo(td);
        }
            box = this.loadBox.find('.'+n+'-tip');
        if (box.length) {
            box.click(function() { jQuery(this).hide(); });
            var th = this.listBox.find('thead [data-fn="'+n+'"]');
            var tp = eval ("("+(box.attr("data-tp") || "{}")+")");
            tp = jQuery.extend({}, {
                "position":"center left", "offset":[0, 0]
            }, tp, {
                "tip":box, "onShow":function() {
                    this.getTip().data("trigger", this.getTrigger());
                }
            });
            jQuery('<button class="admin-btn">'+hsGetLang('list.admin.txt')+'</button>')
                .attr("title", th.text())
                .tooltip (tp)
                .appendTo(td);
        }
    }
};

function HsTree(opts, context) {
    var data = _HsInitOpts.call(this, opts, "HsTree");
    if (data)  return data;

        context  = jQuery( context);
    var loadBox  = context.closest(".load-box");
    var treeBox  = context.find   (".tree-box");
    var findBox  = context.find   (".find-box");
    var loadUrl  = hsGetValue(opts, "loadUrl" );
    var openUrls = hsGetValue(opts, "openUrls");
    var sendUrls = hsGetValue(opts, "sendUrls");
    var linkUrls = hsGetValue(opts, "linkUrls");
    var rootInfo = hsGetValue(opts, "rootInfo",
        {id: "0" , name: hsGetLang("tree.root.name")});
    this.idKey   = hsGetValue(opts, "idKey"  , "id"  );
    this.nameKey = hsGetValue(opts, "nameKey", "name");
    this.noteKey = hsGetValue(opts, "noteKey");
    this.typeKey = hsGetValue(opts, "typeKey");
    this.cnumKey = hsGetValue(opts, "cnumKey");

    this.context = context;
    this.loadBox = loadBox;
    this.treeBox = treeBox;

    var i, a, n, m, u;
    var that  =  this;

    function openHand(evt) {
        //var n = evt.data[0];
        var n = jQuery(this);
        var m = evt.data[1];
        var u = evt.data[2];

        /*
        if (typeof n == "string")
            n = loadBox.find(n);
        else if (n)
            n = jQuery(n);
        */
        if (typeof m == "string")
            m = loadBox.find(m);
        else if (m)
            m = jQuery(m);

        var tip = n.closest(".tooltip");
        if (tip.length)
            n   = tip.data ( "trigger");

        if (typeof u == "function" ) {
            u.call( that, n, m );
            return;
        }

        if (0 <= u.search(/\$(ID|PID)/)) {
            var sid;
            if (0 <= jQuery.inArray(treeBox[0], n.parents())) {
                sid = that.getId (n);
            }
            else {
                sid = that.getSid( );
            }
            if (sid == null) return ;

            u = u.replace(/\$(ID|PID)/, encodeURIComponent(sid));
        }

        that.open( n, m, u );
    }

    if (openUrls)
    for(i = 0; i < openUrls.length; i ++) {
        a = openUrls[i]; m = undefined;
        switch (a.length) {
        case 3:
            n = a[0];
            m = a[1];
            u = a[2];
            break;
        case 2:
            n = a[0];
            u = a[1];
            break;
        default:
            continue;
        }

        if (typeof n == "string")
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
        if (typeof n == "string")
            n = loadBox.find(n);
        else if (n)
            n = jQuery(n);
        */

        var tip = n.closest(".tooltip");
        if (tip.length)
            n   = tip.data ( "trigger");

        if (typeof u == "function" ) {
            u.call( that, n, m );
            return;
        }

        var sid;
        if (-1 != jQuery.inArray(treeBox[0], n.parents())) {
            sid = that.getId (n);
        }
        else {
            sid = that.getSid( );
        }
        if (sid == null) return ;

        var dat = {};
        dat[that.idKey] =  sid  ;

        that.send(n, m, u, dat );
    }

    if (sendUrls)
    for(i = 0; i < sendUrls.length; i ++) {
        a = sendUrls[i]; m = undefined;
        switch (a.length) {
        case 3:
            n = a[0];
            m = a[1];
            u = a[2];
            break;
        case 2:
            n = a[0];
            u = a[1];
            break;
        default:
            continue;
        }

        if (typeof n == "string")
            context.on("click", n, [n, m, u], sendHand);
        else if (n)
            n.on("click", [n, m, u], sendHand);
    }

    // 当选中时, 在指定区域加载指定页面, 并附带树节点ID
    if (linkUrls) {
        treeBox.on("select", function(evt, id) {
            for (var i = 0; i < linkUrls.length; i ++) {
                jQuery(linkUrls[i][0]).load(linkUrls[i][1]
                  .replace('$ID', encodeURIComponent(id)));
            }
        });
    }

    if (findBox.length) {
        findBox.on("submit", function() {
            that.find( loadUrl , this );
            return true;
        });
    }

    treeBox.on("click", ".tree-node td.tree-hand", function() {
        that.toggle(jQuery(this).closest(".tree-node"));
    });
    treeBox.on("click", ".tree-node td:not(.tree-hand)", function() {
        that.select(jQuery(this).closest(".tree-node"));
    });

    var  rootBox = jQuery('<div class="tree-node tree-root" id="tree-node-'
                  +rootInfo["id"]+'"></div>')
                  .appendTo( treeBox );
    this.fillInfo( rootInfo, rootBox );
    this.select  (     rootInfo["id"]);
    this.load(loadUrl, rootInfo["id"]);
}
HsTree.prototype = {
    load     : function(url, pid) {
        if (url ) this._url = url;
        if (pid ) this._pid = pid;
        jQuery.ajax({
            "url"       : this._url ,
            "data"      :{"pid":this._pid},
            "type"      : "POST",
            "dataType"  : "json",
            "context"   : this,
            "complete"  : function(rst) {
                this.loadBack(rst, pid);
            }
        });
    },
    loadBack : function(rst, pid) {
        rst = hsResponObj(rst );
        var sid = this.getSid();
        if (rst.__success__ == false) return;
        if (rst.list) this.fillList( rst.list, pid );
        this.treeBox.trigger("loadBack", [rst, pid]);
        if (this.treeBox.find("#tree-node-"+sid).length == 0) {
            this.select ( pid );
        }
    },
    fillList : function(list, pid) {
        var lst, nod, i, id;
        nod = this.treeBox.find("#tree-node-"+pid);
        lst = nod .children(".tree-list:first");
        if (list.length == 0) {
            nod.removeClass("tree-fold")
               .removeClass("tree-open");
            lst.remove();
            return;
        }

        if (lst.length == 0 ) {
            lst = jQuery('<div class="tree-list"></div>');
            lst.appendTo(nod);
        }

        var pid2, lst2, lsts = {}; lsts[pid] = lst;
        for(i = list.length -1; i > -1; i --) {
            id  = hsGetValue(list[i], this.idKey);
            nod = this.treeBox.find("#tree-node-"+id);
            if (nod.length == 0) {
                nod = jQuery('<div class="tree-node"></div>');
                nod.attr("id", "tree-node-"+id );
            }
            else {
                nod.find("table:first").empty( );
                pid2 = this.getPid(nod);
                lst2 = nod.closest(".tree-list");
                if (pid2 != pid) lsts[pid2] = lst2;
            }
            nod.prependTo(lst);
            this.fillInfo(list[i], nod);
        }
        for(i in lsts) {
            this.fillCnum(list.length, lsts[i]);
        }
    },
    fillInfo : function(info, nod) {
        var tab = jQuery('<table><tbody><tr>'
            + '<td class="tree-hand"></td>'
            + '<td class="tree-name"></td>'
            + '<td class="tree-cnum"></td>'
            + '</tr></tbody></table>');
        var n, t;

        n = hsGetValue(info, this.nameKey);
        tab.find(".tree-name").text(n);
        if (typeof this.noteKey != "undefined") {
            n = hsGetValue(info , this.noteKey);
            nod.find(".tree-name").attr("title", n);
        }
        if (typeof this.typeKey != "undefined") {
            t = hsGetValue(info , this.typeKey);
            nod.addClass("tree-type-" + t );
        }
        if (typeof this.cnumKey != "undefined") {
            n = hsGetValue(info , this.cnumKey);
            tab.find(".tree-cnum").text(n );
            if (n)
                nod.addClass("tree-fold");
        }
        else {
                nod.addClass("tree-fold");
        }

        if (! t) t = "info";
        if (typeof this["fill_"+t] != "undefined") {
            this["fill_"+t].call(this, tab, info );
        }

        tab.prependTo(nod);
    },
    fillCnum : function(cnum, lst) {
        var nod = lst.closest (".tree-node");
        var arr = lst.children(".tree-node");

        if (typeof cnum == "undefined")
            cnum  = arr.length;
        if (cnum != arr.length)
            for (var i = arr.length-1; i > cnum-1; i --) {
                jQuery(arr[i]).remove();
            }

        if (cnum != 0)
            nod.find(".tree-cnum").text(cnum.toString());
        else {
            nod.find(".tree-cnum").hide();
            nod.removeClass("tree-fold")
               .removeClass("tree-open");
        }
    },

    find     : function(url, data) {
    },
    findBack : function(rst) {
    },

    send     : function(btn, msg, url, data) {
        if ( msg && !confirm(msg) ) return ;
        var dat2 = jQuery.extend({}, hsSerialObj(url), hsSerialObj(data||{}));
        jQuery.ajax({
            "url"       : url,
            "data"      : data,
            "type"      : "POST",
            "dataType"  : "json",
            "context"   : this,
            "complete"  : function(rst) {
                this.sendBack(btn, rst, dat2);
            }
        });
    },
    sendBack : function(btn, rst, data) {
        rst = hsResponObj(rst);
        if (rst.__success__ == false) return;
        var evt = new jQuery.Event("sendBack");
        btn.trigger(evt, [rst, data]);
        if (evt.isDefaultPrevented()) return;

        if (data["fid"] !== undefined)
            this.load(null, data["fid"]);
        if (data["pid"] !== undefined)
            this.load(null, data["pid"]);
        else if (data["id"] !== undefined)
            this.load(null, this.getPid(data["id"]));
    },

    open     : function(btn, box, url, data) {
        var that = this;
        var dat2 = jQuery.extend({}, hsSerialObj(url), hsSerialObj(data||{}));
        if (box == "@") box = jQuery(btn).parent(".load-box");
        if (box)
            HsOpen.call(box, url, data, function() {
               that.openBack(btn, this, dat2);
            });
        else
            hsOpen     (     url, data, function() {
               that.openBack(btn, this, dat2);
            });
    },
    openBack : function(btn, box, data) {
        var that = this;
        btn.trigger("openBack",[box, data]);
        box.on("saveBack", function(evt) {
            if(evt.isDefaultPrevented()) return;
            btn.trigger ( evt ,[box, data]);
            if(evt.isDefaultPrevented()) return;

            if (data["fid"] !== undefined)
                that.load(null, data["fid"]);
            if (data["pid"] !== undefined)
                that.load(null, data["pid"]);
            else if (data["id"] !== undefined)
                that.load(null, that.getPid(data["id"]));
            else
                that.load(null, that.getSid());
        });
    },

    select   : function(id) {
        var nod = this.getNode(id);
            id  = this.getId (nod);
        this.treeBox.find(".tree-node")
            .removeClass ( "tree-curr");
        nod.addClass ( "tree-curr").trigger("select", [id]);
    },
    toggle   : function(id) {
        var nod = this.getNode(id);
            id  = this.getId (nod);
        var lst = nod.find(".tree-list");
        if (lst.length == 0) this.load(null, id );
        lst.toggle(); nod.trigger("toggle", [id]);
    },

    getId    : function(id) {
        if (typeof id === "object")
            return this.getId(id.attr("id"));
        else
            return id.toString( ).substr(10);
    },
    getPid   : function(id) {
        return this.getId(this.getPnode(id));
    },
    getNode  : function(id) {
        if (typeof id === "object")
            return id.closest(".tree-node" );
        else
            return this.treeBox.find( "#tree-node-" + id );
    },
    getPnode : function(id) {
        return this.getNode(id).parent().closest(".tree-node");
    },
    getSid   : function() {
        return this.getId(this.treeBox.find(".tree-curr"));
    },
    getRid   : function() {
        return this.getId(this.treeBox.find(".tree-root"));
    }
};

/** 功能辅助函数 **/

function _hsWalkInto(data, func) {
    var path = [];
    if (arguments.length>2) {
        path = arguments[2];
    }
    if (jQuery.isPlainObject(data)) {
        for (var k in data) {
            _hsWalkInto(data[k], func, path.concat([k]));
        }
    }
    else if (jQuery.isArray (data)) {
        for (var i = 0; i < data.length; i ++) {
            _hsWalkInto(data[i], func, path.concat([i]));
        }
    }
    else {
        func(data, path);
    }
}

function _HsInitOpts(opts, name) {
        var func = self[name];
    if (!(this instanceof func)) {
        var inst = this.data(name);
        if (!inst) {
            inst = new func(opts, this);
            this.data(name, inst).addClass(name);
        }
        return inst;
    }
    else {
        if (opts) for (var k in opts) {
            // 允许扩展已有方法, 添加或重写"fill"方法
            if (this[k] != undefined || /^fill/.test(k)) {
                this[k]  = opts[k];
            }
        }
        return null;
    }
}

function _HsReadOpts() {
    var obj = {};
    var arr = jQuery(this).find("param");
    for(var i = 0; i < arr.length; i ++) {
        var n = jQuery(arr[i]).attr("name" );
        var v = jQuery(arr[i]).attr("value");
        switch (v.substring(0, 2)) {
        case "S:": // String
            v = v.substring(2);
            break;
        case "N:": // Number
            v = v.substring(2);
            v = parseFloat (v);
            break;
        case "B:": // Boolean
            v = v.substring(2);
            v =/true/i.test(v);
            break;
        case "D:": // Data
            v = v.substring(2);
            v = jQuery(arr[i]).parent().data(v);
            break;
        case "E:": // Eval
            v = v.substring(2);
            v = eval(v);
            break;
        default:
            if (/^\s*(\(.*\)|\[.*\]|\{.*\})\s*$/.test(v))
                v = eval('('+v+')');
        }
        if (n == "") continue;
        hsSetValue(obj, n, v);
    }
    return obj;
}

function _jt2hsDF(format) {
  return format.replace(/dddd/g, "EE")
               .replace(/ddd/g , "E" )
               .replace(/m/g   , "M" );
}
function _hs2jtDF(format) {
  return format.replace(/EE/g, "dddd")
               .replace(/E/g , "ddd" )
               .replace(/M/g , "m"   );
}

// 常用jQuery扩展
jQuery.extend({
    hsOpen  : hsOpen
});
jQuery.fn.extend({
    hsOpen  : HsOpen,
    hsClose : HsClose,
    hsReady : HsReady,
    hsForm  : HsForm,
    hsList  : HsList,
    hsTree  : HsTree
});

/** 应用支持部分 **/

/*
编码原则:
1. 尽可能的少写程序, 用描标记述化代替
2. 使用事件驱动应用, 而不是初始化程序
*/
( function($) {
    /** 重写jQuery函数 **/

    var _ajax = $.ajax;
    var _load = $.fn.load;

    $.ajax = function(url, settings) {
        if (typeof url === "object") {
            settings = url;
            if (typeof url["url"] != "undefined")
                url  = url["url"];
        }
        return _ajax( hsFixUri( url ), settings );
    };
    $.fn.load = function(url, data, complate) {
        if ($.isFunction(data)) {
            complate = data ;
            data = undefined;
        }
        if (!$.isFunction(complate)) {
            complate = function() {};
        }
        this.data( "url" , url ).data( "data" , data )
            .addClass("load-box").addClass("load-ing");
        return _load.call(this, url, data, function( ) {
            var that  = $(this);
            that.removeClass ( "load-ing" );
            complate.apply(that, arguments);
            HsReady .call (that);
        });
    };

    /** 设置jQueryTools参数 **/

    // 设置jquery tools浮动层
    $.tools.overlay.conf.fixed = true;
    $.tools.overlay.conf.oneInstance = false;
    $.tools.overlay.conf.closeOnClick = false;

    // 设置jquery tools国际化
    $.tools.validator.localize("en", {
        "*"               : "",
        ":number"         : hsGetLang("form.is.not.number"),
        ":url"            : hsGetLang("form.is.not.url"),
        ":email"          : hsGetLang("form.is.not.email"),
        ":radio"          : hsGetLang("form.required"),
        "[required]"      : hsGetLang("form.required"),
        "[max]"           : hsGetLang("form.gt.max"),
        "[min]"           : hsGetLang("form.lt.min"),
        "[minlength]"     : hsGetLang("form.lt.minlength"),
        "[type=date]"     : hsGetLang("form.is.not.date"),
        "[type=time]"     : hsGetLang("form.is.not.time"),
        "[type=datetime]" : hsGetLang("form.is.not.datetime"),
        "[data-equals]"   : hsGetLang("form.is.not.equals"),
        "[data-unique]"   : hsGetLang("form.is.not.unique")
    });
    $.tools.dateinput.localize("en", {
        "days"            : hsGetLang("date.LE").join(","),
        "shortDays"       : hsGetLang("date.SE").join(","),
        "months"          : hsGetLang("date.LM").join(","),
        "shortMonths"     : hsGetLang("date.SM").join(",")
    });
    $.tools.dateinput.conf.format = _hs2jtDF(hsGetLang("date.format"));
    $.tools.dateinput.conf.firstDay =  hsGetLang("date.first.day");

    // 设置jquery tools表单校验方法
    $.tools.validator.fn("[minlength]", function(input, value) {
        return input.attr("minlength") >= value.length;
    });
    $.tools.validator.fn("[type=date]", function(input, value) {
        return /^\d{4}\/\d{1,2}\/\d{1,2}$/.test(value);
    });
    $.tools.validator.fn("[type=time]", function(input, value) {
        return /^\d{1,2}:\d{1,2}:\d{1,2}$/.test(value);
    });
    $.tools.validator.fn("[type=datetime]", function(input, value) {
        return /^\d{4}\/\d{1,2}\/\d{1,2} \d{1,2}:\d{1,2}:\d{1,2}$/.test(value);
    });
    $.tools.validator.fn("[data-equals]", function(input, value) {
        return this.getInputs().filter("[name="+input.attr("equals")+"]").val() == value;
    });
    $.tools.validator.fn("[data-unique]", function(input, value) {
        var ret = true;
        var act = this.getForm().attr("action");
        var data = hsGetParamsMap(act);
        data["n"] = input.attr("name");
        data["v"] = value;
        $.ajax({
            "url": input.attr("data-unique"),
            "data": data,
            "type": "POST",
            "dataType": "json",
            "async": false,
            "cache": false,
            "success": function(rst) {
               ret = rst["success"];
            }
        });
        return ret;
    });

    /** 自定义标签化语义 **/

    $.fn.hsInit = function( ) {
        $(this).hsInitLoad( );
        $(this).find("div[data-eval]").each(function() {
            eval($(this).attr("data-eval"));
        });
        $(this).find("div[data-load]").each(function() {
            $(this).load($(this).attr("data-load"));
        });
        $(this).find("div[data-open]").each(function() {
            $(this).hsOpen($(this).attr("data-load"));
        });
        $(this).find("object.config" ).each(function() {
            var prt = $(this).parent();
            var fun = $(this).attr("name");
            var cnf = _HsReadOpts.call( this );
            if (typeof prt[fun] == "function") {
                prt[fun](cnf);
            }
        }).remove();

        // 为所有的input加上type class, 方便设置样式, 兼容老浏览器
        $(this).find('input').each(function() {
            $(this).addClass($(this).attr("type"));
        });

        // 清除文本节点
        $(this).find('.ct').each(function() {
        $(this).contents( ).each(function() {
            if (this.nodeType == 3)
                this.remove();
        });});

        // 工具按钮状态初始化
        $(this).find(".bind-to-select,.bind-to-single").prop("disabled", true);

        return this;
    };
    $.fn.hsInitLoad = function() {
        $(this).find("[type=date]").dateinput();
        $(this).find(".tabs").each(function() {
            $(this).tabs($(this).next(".panes").children("div"));
        });
        $(this).find(".overlay-trigger").each(function() {
            var o = {}, n = $(this).next(".overlay");
            if ($(this).attr("rel")) {
                o.target = $(this).attr("rel");
            }
            else if (n.length) {
                o.target = n;
            }
            $(this).overlay(o);
        });
        $(this).find(".tooltip-trigger").each(function() {
            var o = {}, n = $(this).next(".tooltip");
            if ($(this).attr("rel")) {
                o.tip = $(this).attr("rel");
            }
            else if (n.length) {
                o.tip = n;
            }
            $(this).tooltip(o);
        });
        return this;
    };
    $.fn.hsInitOpen = function(cnf) {
        var v, o, c;
        var box = $(this).closest(".load-box");
        if (box.data( "overlay" )) {
            o = box.data( "overlay" ); c = o.getConf();
            v = hsGetValue(cnf, "top"); if (v) c.top = v;
            v = hsGetValue(cnf, "left"); if (v) c.left = v;
            v = hsGetValue(cnf, "width"); if (v) box.css("width",v);
            o.getOverlay( ).overlay( c );
        }
        else if (box.data("tabs")) {
            v = hsGetValue(cnf, "title"); if (v) box.data("curTab").text(v);
        }
        return this;
    };

    $(function() {
        $(this).hsInit()
    });
    $(document )
    .on("hsReady", ".load-box", function() {
        $(this).hsInit();
    })
    .on("hsClose", ".load-box", function() {
        // 解决表单"窗口"关闭后validator的错误消息仍然存在的问题
        $(this).find(".HsForm").data("HsForm").validator.destroy();
    })
    .on("click", "[data-load-in]", function() {
        $($(this).attr("data-load-in")).load($(this).attr("href"));
        return false;
    })
    .on("click", ".load", function() {
        $(this).closest(".load-box").load($(this).attr("href"));
        return false;
    })
    .on("click", "[data-open-in]", function() {
        $($(this).attr("data-open-in")).hsOpen($(this).attr("href"));
        return false;
    })
    .on("click", ".open", function() {
        $(this).closest(".load-box").hsOpen($(this).attr("href"));
        return false;
    })
    .on("click", ".open-new", function() {
        $.hsOpen( $(this).attr("href") );
        return false;
    })
    .on("click", ".list-box tbody td", function(evt) {
        // 当点击表格列时单选
        // 工具按钮有三类, 打开|打开选中|发送选中
        // 打开选中只能选一行, 发送选中可以选多行
        // 但是复选框太小不方便操作, 故让点击单元格即单选该行, 方便操作
        if (this != evt.target) return;
        var tr = $(this).closest("tr");
        var ck = tr.find(".check-one");
        if (this !=  ck .closest("td")) {
            tr.closest("tbody").find(".check-one").not(ck)
                               .prop( "checked"   , false);
        }
        ck.prop("checked", ! ck.prop( "checked")).change();
    })
    .on("change", ".HsList .check-one", function() {
        // 当选中行时, 开启工具按钮, 否则禁用相关按钮
        var box = $(this).closest(".HsList");
        var len = box.find(".check-one:checked").length;
        box.find(".bind-to-select").prop("disabled", len == 0);
        box.find(".bind-to-single").prop("disabled", len != 1);
    })
    .on("select", ".HsTree .tree-node", function() {
        // 当选中非根节点时, 开启工具按钮, 否则禁用相关按钮
        var box = $(this).closest(".HsTree");
        var obj = box.data("HsTree");
        box.find(".bind-to-select").prop("disabled", obj.getSid()==obj.getRid());
    });
})(jQuery);
