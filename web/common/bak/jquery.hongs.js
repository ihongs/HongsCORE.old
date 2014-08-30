/*
HongsCORE(Javascript)
作者: 黄弘 <kevin.hongs@gmail.com>
创建: 2013/01/01
修改: 2014/01/27 23:51:30
依赖:
    jquery.js,
    jquery.tools.js (tabs, overlay, tooltip, validator, dateinput, expose)
    bootstrap.css   (.btn, .alert, .tooltip, .popover, .pagination,
                     .dropdown, .arrow, .caret, .close, .active,
                     .fade, .[in|out], .[position], .has-error)

[ID]

note-box        全局消息
node-[ID]       节点编号, 在.tree-node上

[Class]

open            在浮窗中打开href对应的页
close           关闭浮窗的按钮
cancel          取消表单并关闭浮窗的按钮
ensure          提交表单的按钮
load-ing
load-box
open-box
note-box
list-box        用于HsList中, 下同
page-box
check-all
check-one
for-select
for-checks
tree-box        用于HsTree中, 下同
tree-list
tree-node
tree-root
tree-curr
tree-opened
tree-folded
tree-hand       节点开关图标
tree-name       节点名称
tree-cnum       节点子级数量
node-TYPE       节点类型, TYPE为类型标识, 在.tree-node上
pick            打开href对应的选择窗口, 用于选择组件, 下同
pick-box        选项窗口
pick-mul        多选窗口

[Attr]

data-eval       执行JS, this指向当前节点
data-load       在当前节点中加载, 属性值为url
data-open       在当前节点中打开, 属性值为url
data-toggle     关联触发, 属性可选: overlay,alert,modal,tooltip,dropdown,hsOpen,hsPick
data-target     关联区域, 属性值为selector
data-placement  tooltip的相对位置, 请参阅bootstrip的tooltip
data-fn         HsForm和HsList中的field-name
data-ft         HsForm和HsList中的field-type
data-pn         HsForm中的param-name, HsList中的page-num
data-vk         HsForm中的value-key
data-tk         HsForm中的 text-key
data-repeat     HsForm中校验重复, 属性值为input-name
data-unique     HsForm中限制唯一, 属性值为url, url中可用{input-name}指定其他表单项的值
data-validate   HsForm中校验函数, 属性值为函数名

[Data]

url             用于HsLoad中, 下同
data
baks            用于HsOpen中, 下同
tabs
oldTab          之前选中的Tab对象
curTab          现在选中的Tab对象
overlay
trigger         用于从tip上获取trigger对象
pickData        用于Pick组件中绑定选择数据

[Event]

HsReady:
    hsReady
HsClose:
    hsClose
HsForm:
    loadBack    绑定在formBox上
    loadError   绑定在formBox上, 加载错误触发, 参数同ajaxError
    saveBack    绑定在formBox上, 默认关闭表单
    saveFail    绑定在formBox上, 提交失败触发
HsList:
    loadBack    绑定在listBox上
    loadError   绑定在listBox上, 加载错误触发, 参数同ajaxError
    sendBack    绑定在btn上, 默认刷新列表
    sendError   绑定在btn上, 发送错误触发, 参数同ajaxError
    openBack    绑定在btn上
    saveBack    绑定在btn上, 默认刷新列表
HsTree:
    loadBack    绑定在treeBox上
    loadError   绑定在treeBox上, 加载错误触发, 参数同ajaxError
    sendBack    绑定在btn上, 默认刷新节点
    sendError   绑定在btn上, 发送错误触发, 参数同ajaxError
    openBack    绑定在btn上
    saveBack    绑定在btn上, 默认刷新节点
    select      绑定在节点上(选中)
    taggle      绑定在节点上(开关)
hsPick:
    pickItem
    pickBack

*/

if (typeof(HsAUTH) === "undefined") HsAUTH = {};
if (typeof(HsCONF) === "undefined") HsCONF = {};
if (typeof(HsLANG) === "undefined") HsLANG = {};

/**
 * 快捷方式
 * 说明(首参数以下列字符开头的意义):
 * .    获取配置
 * :    获取语言
 * ?    检查权限
 * /    补全路径
 * &    获取单个参数值, 第二个参数指定参数容器
 * @    获取多个参数值, 第二个参数指定参数容器
 * $    获取/设置会话存储, 第二个参数存在为设置, 第二个参数为null则删除
 * %    获取/设置本地存储, 第二个参数存在为设置, 第二个参数为null则删除
 * @return {Mixed} 根据开头标识返回不同类型的数据
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
    case '&':
    case '@':
        if (arguments.length === 1) {
            arguments[1] = location.href;
        }
        if (typeof(arguments[1]) !== "string") {
            if (!jQuery.isArray(arguments[1])) {
                var c =  jQuery(arguments[1]).closest(".load-box");
                var d = hsSerialArr(c.data("url" ));
                var e = hsSerialArr(c.data("data"));
                for (var i = 0; i < e.length; i ++)
                    d.push(e[i]);
                arguments[1] = d;
            }
            if (b === '@')
                return hsGetSerias(arguments[1], arguments[0]);
            else
                return hsGetSeria (arguments[1], arguments[0]);
        } else {
            if (b === '@')
                return hsGetParams(arguments[1], arguments[0]);
            else
                return hsGetParam (arguments[1], arguments[0]);
        }
    case '$':
    case '%':
        var c = b === '$' ? window.sessionStorage : window.localStorage;
        if (typeof(c) === "undefined") {
            throw("H$: Does not support '"+(b == '$' ? 'session' : 'local')+"Storage'");
        }
        if (arguments.length === 1) {
            return c.getItem(arguments[0]);
        } else if (arguments[1]) {
                   c.setItem(arguments[0] , arguments[1]);
        } else {
                c.removeItem(arguments[0]);
        }
    default: throw("H$: Unrecognized identified '"+b+"'");
    }
}

/**
 * 标准化返回对象
 * @param {Object,String} rst JSON对象/JSON文本或错误消息
 * @param {Boolean} qut 静默, 不显示消息
 * @return {Object}
 */
function hsResponObj(rst, qut) {
    if (typeof(rst.responseText) !== "undefined") {
        rst  = rst.responseText;
    }
    if (typeof (rst) === "string") {
        if (rst.charAt(0) === '{') {
            if (typeof(JSON) !== "undefined") {
                rst  = JSON.parse( rst );
            }
            else {
                rst  = eval('('+rst+')');
            }
        }
        else
        if (rst.charAt(0) === '<') {
            // 某些时候服务器可能出错, 返回错误消息的页面
            // 需要清理其中的html代码, 以供输出简洁的消息
            rst = {
                "__success__" : false,
                "__message__" :  rst
                    .replace(/<script.*?>.*?<\/script>/img, "")
                    .replace(/<style.*?>.*?<\/style>/img, "")
                    .replace(/<[^>]*?>/g, "")
                    .replace(/&[^&;]*;/g, "")
                    .replace(/^\s*(\r\n|\r|\n)/mg, "")
            };
        }
        else {
            rst = {
                "__success__" : false,
                "__message__" :  rst
            };
        }
    }
    if (typeof(rst) === "object") {
        if (typeof(rst.__success__) === "undefined") {
            rst.__success__ = true;
        }
        if (typeof(rst.__message__) === "undefined") {
            rst.__message__ =  "" ;
        }
        if (! qut) {
            if (rst.__success__) {
                if (rst.__message__) {
                    hsNote(rst.__message__, 'alert-success');
                }
            }
            else {
                if (rst.__message__) {
                    alert (rst.__message__);
                }
                else {
                    alert (hsGetLang("error.unkwn"));
                }
            }
        }
        if (typeof(rst.__refresh__) !== "undefined") {
            if (rst.__refresh__) {
                location.assign(  rst.__refresh__  );
            }
            else {
                location.reload(  );
            }
            delete rst.__refersh__ ;
        }
    }
    return rst;
}

/**
 * 序列化为数组, 供发往服务器
 * @param {Object,Array,String,Elements} obj
 * @param {Array}
 */
function hsSerialArr(obj) {
    var arr = [];
    if (typeof(obj) === "string") {
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
        _hsEachLeaf(obj, function(val,key) {
            if (key.length < 1 || val.length < 1) return;
            arr.push({name : key.join("."),
                      value: val});
        });
    }
    else if (!jQuery.isArray(obj) && typeof(obj) === "object") {
        arr = jQuery(obj).serializeArray();
    }
    else if ( obj != null ) {
        arr = obj;
    }
    return arr;
}
/**
 * 序列化为对象, 供进一步操作(可以使用hsGetValue获取数据)
 * @param {Object,Array,String,Elements} obj
 * @return {Object}
 */
function hsSerialObj(obj) {
    var arr = hsSerialArr(obj);
    obj = {};
    for (var i = 0; i < arr.length; i ++) {
        hsSetValue(obj, arr[i].name, arr[i].value);
    }
    return obj;
}
/**
 * 获取多个序列值
 * @param {Array} arr 使用 hsSerialArr 获得
 * @param {String} name
 * @return {Array}
 */
function hsGetSerias(arr, name) {
    var val = [];
    for(var i = 0; i < arr.length ; i ++) {
        if (arr[i]["name"] === name ) {
            val.push(arr[i]["value"]);
        }
    }
    return val;
}
/**
 * 设置多个序列值
 * @param {Array} arr 使用 hsSerialArr 获得
 * @param {String} name
 * @param {Array} value
 */
function hsSetSerias(arr, name, value) {
    for(var j = arr.length-1; j > -1; j --) {
        if (arr[j]["name"] === name) {
            arr.splice(j, 1);
        }
    }
    for(var i = 0; i < value.length ; i ++) {
        arr.push({name: name, value: value[i]});
    }
}
/**
 * 获取单个序列值
 * @param {Array} arr 使用 hsSerialArr 获得
 * @param {String} name
 * @return {String}
 */
function hsGetSeria (arr, name) {
    var val = hsGetSerias(arr, name);
    if (val.length) return val.pop();
    else            return "";
}
/**
 * 设置单个序列值
 * @param {Array} arr 使用 hsSerialArr 获得
 * @param {String} name
 * @param {Array} value
 */
function hsSetSeria (arr, name, value) {
    hsSetSerias(arr, name, [value]);
}

/**
 * 获取多个参数值
 * @param {String} url
 * @param {String} name
 * @return {Array}
 */
function hsGetParams(url, name) {
    name = encodeURIComponent(name).replace('.', '\\.');
    var reg = new RegExp("[\\?&]"+name+"=([^&]*)", "g");
    var arr = null;
    var val = [];
    while (true) {
        arr = reg.exec(url);
        if ( arr === null ) break;
        val.push(decodeURIComponent(arr[1]));
    }
    return val;
}
/**
 * 设置多个参数值
 * @param {String} url
 * @param {String} name
 * @param {Array} value
 */
function hsSetParams(url, name, value) {
    name = encodeURIComponent(name).replace('.', '\\.');
    var reg = new RegExp("[\\?&]"+name+"=([^&]*)", "g");
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
/**
 * 获取单个参数值
 * @param {String} url
 * @param {String} name
 * @return {String}
 */
function hsGetParam (url, name) {
    var val = hsGetParams(url, name);
    if (val.length) return val.pop();
    else            return "";
}
/**
 * 设置单个参数值
 * @param {String} url
 * @param {String} name
 * @param {String} value
 */
function hsSetParam (url, name, value) {
    return hsSetParams(url, name, [value]);
}

/**
 * 从树对象获取值
 * @param {Object,Array} obj
 * @param {Array,String} path ['a','b'] 或 a.b
 * @param def 默认值
 * @return 获取到的值, 如果没有则取默认值
 */
function hsGetValue (obj, path, def) {
    if (jQuery.isArray(path)) {
        return hsGetPoint(obj, path, def);
    }
    if (typeof(path) === "number") {
        return hsGetPoint(obj,[path],def);
    }
    if (typeof(path) !== "string") {
        throw("hsGetValue: 'path' must be a string");
    }

    path = path.replace(/\]\[/g, ".")
               .replace(/\[/   , ".")
               .replace(/\]/   , "" )
               .replace(/\.+$/ , "" ) // a[b][c][] 与 a.b.c 一样, 应用场景: 表单中多选项按 id[] 提取数据
               .split  (/\./ );
    return hsGetPoint(obj, path, def);
}
/**
 * 从树对象获取值(hsGetValue的底层方法)
 * @param {Object,Array} obj
 * @param {Array} keys ['a','b']
 * @param def 默认值
 * @return 获取到的值, 如果没有则取默认值
 */
function hsGetPoint (obj, keys, def) {
    if (!obj) {
        return null;
    }
    if (!jQuery.isArray(obj ) && !jQuery.isPlainObject(obj )) {
        throw("hsGetPoint: 'obj' must be an array or object");
    }
    if (!jQuery.isArray(keys)) {
        throw("hsGetPoint: 'keys' must be an array");
    }
    if (!keys.length) {
        throw("hsGetPoint: 'keys' can not be empty");
    }

    var i , k;
    for(i = 0; i < keys.length; i ++) {
        k = keys[i];
        if(typeof(obj[k]) !== "undefined") {
           obj  = obj[k];
           continue;
        }
        return  def;
    }
        return  obj;
}
/**
 * 向树对象设置值
 * @param {Object,Array} obj
 * @param {Array,String} path ['a','b'] 或 a.b
 * @param val
 */
function hsSetValue (obj, path, val) {
    /**
     需要注意的键:
     a[1]   数字将作为字符串对待, 但可用hsSetArray完成
     a[][k] 空键将作为字符串对待, 但放在末尾可表示push
     */
    if (jQuery.isArray(path)) {
        hsSetPoint(obj, path, val); return;
    }
    if (typeof(path) === "number") {
        obj[path] = val; return;
    }
    if (typeof(path) !== "string") {
        throw("hsSetValue: 'path' must be a string");
    }
    path = path.replace(/\]\[/g, ".")
               .replace(/\[/   , ".")
               .replace(/\]/   , "" )
               .split  (/\./ );
    hsSetPoint(obj, path, val);
}
/**
 * 向树对象设置值(hsSetValue的底层方法)
 * @param {Object,Array} obj
 * @param {Array} keys ['a','b']
 * @param val
 */
function hsSetPoint (obj, keys, val) {
    if (!obj) {
        return;
    }
    if (!jQuery.isPlainObject(obj)) {
        throw("hsSetPoint: 'obj' must be an object");
    }
    if (!jQuery.isArray(keys)) {
        throw("hsSetPoint: 'keys' must be an array");
    }
    if (!keys.length) {
        throw("hsSetPoint: 'keys' can not be empty");
    }

    var i, k, t = keys[0];
    for(i = 0; i < keys.length -1; i ++) {
        k = keys[ i ];
        t = keys[i+1];
        if (!t)t = -1;
        if (typeof(t) === "number")
            if (!jQuery.isArray(obj[k])) {
                obj[k] = [];
            }
        else
        if (typeof(t) === "string")
            if (!jQuery.isPlainObject(obj[k])) {
                obj[k] = {};
            }
        else
            throw("hsSetPoint: key must be a string or number");
        obj = obj[k];
    }
    if (t !== -1)
        obj[t] = val;
    else
        obj.push(val);
}

/**
 * 获取配置
 * @param {String} key
 * @param {String} def 默认值
 * @return {String} 获取到的配置, 如果没有则取默认值
 */
function hsGetConf  (key, def) {
    if (typeof(HsCONF[key]) !== "undefined") {
        return HsCONF[key];
    }
    else {
        return def;
    }
}
/**
 * 获取语言
 * @param {String} key
 * @param {Object,Array} rep 替换参数, {a:1,b:2} 或 [1,2]
 * @return {String} 获取到的语言, 其中的 $a或$0 可被 rep 替换
 */
function hsGetLang  (key, rep) {
    if (typeof(HsLANG[key]) !== "undefined") {
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
            if (typeof(rep[w]) !== "undefined") {
                return rep[w];
            }
            else {
                return "";
            }
        });
    }

    return key;
}
/**
 * 检查URI是否有权访问
 * @param {String} uri
 * @return {Boolean} 是(true)否(false)有权访问
 */
function hsChkUri   (uri) {
    return HsAUTH? HsAUTH[hsFixUri(uri)]: false;
}
/**
 * 补全URI为其增加前缀
 * @param {String} uri
 * @return {String} 完整的URI
 */
function hsFixUri   (uri) {
    if (/^(\w+:\/\/|\/|\.)/.test(uri) === false)
        return hsGetConf("BASE_HREF") +"/"+ uri;
    else
        return uri;
}

/**
 * 格式化数字
 * @param {Number} num
 * @param {Number} len 总长度(不含小数点)
 * @param {Number} dec 小数位
 * @param {String} sep 千分符
 * @param {String} dot 小数点
 * @return {String}
 */
function hsFmtNum(num, len, dec, sep, dot) {
  if (typeof(len) === "undefined") {
    len = 0;
  }
  if (typeof(dec) === "undefined") {
    dec = 2;
  }
  if (typeof(sep) === "undefined") {
    sep = ",";
  }
  if (typeof(dot) === "undefined") {
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
 * 格式化日期
 * @param {Date} date
 * @param {String} format
 * @return {String}
 */
function hsFmtDate(date, format) {
  if (typeof(date) === "string") {
    if ( ! /^\d*$/.test(date)) {
      date = Date.parse(date);
    }
    else {
      date = parseInt(date);
    }
  }
  if (typeof(date) === "number") {
    if (date <= 2147483647) {
      date = date * 1000 ;
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

/**
 * 解析日期
 * @param {String} text
 * @param {String} format
 * @return {Date}
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
  if (typeof(M) !== "undefined"
  &&  typeof(d) !== "undefined"
  &&  typeof(y) !== "undefined") {
    text2 = M+"/"+d+"/"+y+" "+H+":"+m+":"+s;
  }
  else {
    text2 = H+":"+m+":"+s;
  }

  return new Date(Date.parse(text2));
}

/**
 * 遍历对象或数组的全部叶子节点
 * @param {Object,Array} data
 * @param {Function} func
 */
function _hsEachLeaf(data, func) {
    var path = [];
    if (arguments.length>2) {
        path = arguments[2];
    }
    if (jQuery.isPlainObject(data)) {
        for (var k in data) {
            _hsEachLeaf(data[k], func, path.concat([k]));
        }
    }
    else if (jQuery.isArray (data)) {
        for (var i = 0; i < data.length; i ++) {
            _hsEachLeaf(data[i], func, path.concat([i]));
        }
    }
    else {
        func(data, path);
    }
}

/**
 * 标准文档对象操作类初始化方法
 * @param {Object} opts
 * @param {String} name
 * @returns {Object,null}
 */
function _HsInitOpts(opts, name) {
    var func = self[name];
    if (! ( this instanceof func)) {
        var inst = this.data(name);
        if (! inst) {
            inst = new  func(opts , this);
            this.data(name , inst);
        }
        return inst;
    }
    else {
        if (opts) for ( var k in opts) {
            // 允许扩展及重写方法和属性
            if ('_'===k.substring(0,1)
            ||  this[k] !== undefined) {
                this[k]  =  opts[k];
            }
        }
        return null;
    }
}

/**
 * 获取配置选项
 * @returns {Object}
 */
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
            v =  parseFloat(v);
            break;
        case "B:": // Boolean
            v = v.substring(2);
            v = /(true|yes|ok|t|y|1)/i.test(v);
            break;
        default:
            if (/^\s*(\[.*\]|\{.*\})\s*$/.test(v))
                v = eval('('+v+')');
            else  if  (/^\s*(\(.*\))\s*$/.test(v))
                v = eval(v);
        }
        if (n) hsSetValue(obj, n,v);
    }
    return obj;
}

/**
 * 获取相对目标区域
 * @param {string} sel
 * @param {jQuery} that
 * @returns {jQuery}
 */
function _hsTarget(sel, that) {
    that = jQuery(that);
    var flg = sel.charAt(0);
    var sal = sel.substr(1);
    switch (flg) {
        case '@':
            that = that.closest(".load-box");
            return sal ? jQuery(sal, that) : that;
        case '>':
            return sal ? jQuery(sel, that) : that;
        default :
            return jQuery(sel);
    }
}

/**
 * bootstrap方位转jquery.tools方位
 * @param {String} pos
 * @returns {String}
 */
function _bs2jtPos(pos) {
    return {
        top     : "top center",
        left    : "center left",
        right   : "center right",
        bottom  : "bottom center"
    }[pos];
}
/**
 * HongsCORE日期格式转jquery.tools日期格式
 * @param {String} format
 * @return {String)
 */
function _hs2jtDF(format) {
  return format.replace(/EE/g, "dddd")
               .replace(/E/g , "ddd" )
               .replace(/M/g , "m"   );
}
/**
 * jquery.tools日期格式转HongsCORE日期格式
 * @param {String} format
 * @return {String)
 */
function _jt2hsDF(format) {
  return format.replace(/dddd/g, "EE")
               .replace(/ddd/g , "E" )
               .replace(/m/g   , "M" );
}

/** UI 组件 **/

/**
 * 通知
 * @param {String} msg 消息
 * @param {String} cls 样式类, 默认为alert-info
 * @param {Integer} sec 展示秒数, 默认为5秒
 * @return {jQuery} 消息框对象
 */
function hsNote(msg, cls, sec) {
    if (! cls) cls = "alert-info";
    if (! sec) sec = 5;

    var div = jQuery('<div class="alert alert-dismissable">'
                    +'<button type="button" class="close">&times;</button>'
                    +'<div class="note-box"></div></div>')
            .addClass(cls);
    var btn = div.find("button");
    var box = div.find(".note-box").append(msg);
    var ctr =  jQuery ("#note-box").append(div).show();

    div.slideDown(200);
    btn.click(function() {
      div.slideUp(200, function() {
          div. remove ();
          if (ctr.children().size() == 0) {
              ctr.hide();
          }
      });
    });

    setTimeout(function() {
      btn.click( );
    }, sec * 1000);
    return box;
}

/**
 * 打开浮窗
 * @param {String} url 浮窗内容URL
 * @param {hsSerialArr_obj} data 附加参数
 * @param {Function} callback
 * @return {jQuery} 浮窗对象
 */
function hsOpen(url, data, callback) {
    var callfore;
    if (jQuery.isArray(callback)) {
        callfore = callback[0];
        callback = callback[1];
    }

    var div = jQuery('<div class="overlay alert alert-dismissable">'
                    +'<button type="button" class="close">&times;</button>'
                    +'<div class="open-box"></div></div>');
    var box = div.find( '.open-box' );
    div.appendTo(document.body)
       .overlay({
          close : div.find('.close' ),
        onClose : function() {
            HsClose.call  (   box   );
            box.removeData("overlay");
            div.remove();
        }
    });
    box.data("overlay", div.data("overlay"));
    if (callfore) callfore.call(box);
    box.load( url , data , callback);
    return box;
}

/**
 * 在tab或指定区域中打开
 * @param {String} url 内容URL
 * @param {hsSerialArr_obj} data 附加参数
 * @param {Function} callback
 * @return {jQuery} 区域对象
 */
function HsOpen(url, data, callback) {
    var callfore;
    if (jQuery.isArray(callback)) {
        callfore = callback[0];
        callback = callback[1];
    }

    var box  = jQuery(this);
    if (box.is( ".nav" )) {
        var lnk = box.find("[data-href='"+url+"']" );
        if (! lnk.size()) {
            lnk = jQuery('<a href="javascript:;"><span></span><span class="close">&times;</span></a>');
            var tab = jQuery('<li class="dn"></li>');
            var div = jQuery('<div></div>');
            var pns = box.siblings(  );
            lnk.attr("data-href", url);
            tab.appendTo(box);
            lnk.appendTo(tab);
            div.appendTo(pns);
            // 重建 tabs
            var tabs = box.data("tabs");
            var cnf = tabs.getConf ( );
            var idx = tabs.getIndex( );console.log(idx)
            tabs = box.tabs(pns.children(), cnf);
            tabs.click(idx);
        }
        box  = lnk.parent();
    }
    var tabs = box.parent().data("tabs");
    if (tabs) {
        var pre = tabs.getCurrentTab ( );console.log(pre.index());
        tabs.click(box.index());
        var tab = tabs.getCurrentTab ( );
            box = tabs.getCurrentPane( );
        var lab = tab.find("a");
        if (lab.find("span:first").size()) {
            lab = lab.find( "span:first" );
        }
        lab.text(hsGetLang("tab.loading"));
        tab.show();
        if (! box.data("tabs")) {
            box.data("tabs", tabs );
            box.data("tab" , tab  );
            box.data("pre" , pre  );
        }
    } else {
        if (! box.data("baks")) {
            var baks = box.contents( ).detach( );
            box.data("baks", baks );
        }
    }
    box.contents().remove();
    box = jQuery('<div class="open-box"></div>').appendTo(box);
    if (callfore) callfore.call(box);
    box.load( url , data , callback);
    return box;
}

/**
 * 关闭浮窗或区域
 * @return 浮窗或区域对象
 */
function HsClose() {
    var box = jQuery(this);
    var prt = box.parent();
    box.trigger("hsClose");
    if (box.data("overlay")) {
        box.data("overlay").close();
    } else
    if (prt.data("tabs")) {
        var pre = prt.data( "pre" );
        var idx = pre.is(":hidden")
                ?  0 : pre.index( );
        prt.data("tabs").click(idx);
        prt.data("tab").hide();
        prt.removeData("tabs");
    } else
    if (prt.data("baks")) {
        prt.data("baks").appendTo( prt );
        prt.removeData("baks");
    }
    box.remove();
    return box;
}

/**
 * 预置浮窗或区域
 * @return 浮窗或区域对象
 */
function HsReady() {
    var $   =  jQuery;
    var box = $(this);
    box.trigger ("hsReady");

    if (box.is(".load-box")
    &&  box.children("object.config[name=hsInit]").size() == 0) {
        HsInit.call(box,{});
    }

    /**
     * 为所有的 input 加上 type-CLASS, 方便设置样式, 兼容老浏览器
     */
    box.find('input').each(function() {
        $(this).addClass("input-" + $(this).attr("type"));
    });

    /**
     * 为避免在 chrome 等浏览器中显示空白间隔, 清除全部空白文本节点
     */
    box.find('*').contents().filter(function() {
        return this.nodeType == 3 && /^\s+$/.test(this.nodeValue);
    }).remove();

    /** 语义标签解析 **/

    box.find("div[data-eval]").each(function() {
        eval($(this).attr("data-eval"));
    });
    box.find("div[data-load]").each(function() {
        $(this).load($(this).attr("data-load"));
    });
    box.find("div[data-open]").each(function() {
        HsOpen.call(this, $(this).attr("data-open"));
    });
    box.find("object.config" ).each(function() {
        var prnt = $(this).parent();
        var func = $(this).attr("name");
        var opts = _HsReadOpts.call( this );
        if (typeof(prnt[func]) === "function") prnt[func](opts);
    }).remove();

    /** jquery tools, bootstrap 语义标签解析 **/

    box.find("[data-toggle=tabs],[data-toggle=pills]").each(function() {
        var m = $(this), n;
        do {
            n = m.attr("data-target");
            if (n) { n = _hsTarget(n, m); break; }
            n = m.siblings(".panes,.nav-panes");
            if (n.size()) break;
            return;
        } while (false);
        n.data("tabs", m.tabs(n.children()).data("tabs"));
    });
    box.find("[data-toggle=overlay],[data-toggle=alert],[data-toggle=modal]").each(function() {
        var m = $(this), n;
        do {
            n = m.attr("data-target");
            if (n) { n = _hsTarget(n, m); break; }
            n = m.siblings(".overlay,.alert,.modal");
            if (n.size()) break;
            return;
        } while (false);
        var o = {
          target      : n,
          mask        : {
            color     : "#000",
            opacity   : 0.8,
            loadSpeed : 0
          }
        };
        n.data("overlay", m.overlay(o).data("overlay"));
    });
    $(this).find("[data-toggle=tooltip],[data-toggle=popover],[data-toggle=dropdown]").each(function() {
        var m = $(this), n, r;
        do {
            n = m.attr("data-target");
            r = false;
            if (n) { n = _hsTarget(n, m); break; }
            n = m.siblings(".tooltip,.popover,.dropdown-menu");
            r = true ;
            if (n.size()) break;
            return;
        } while ( false );
        var o = {
            tip       : n,
            relative  : r
        };

        // 与 bootstrap 配合使用
        var p = m.attr("data-placement");
        switch (m.attr("data-toggle")) {
            case "tooltip":
                o.position = p ? _bs2jtPos(p) : "top center";
                o.tipClass = "tooltip";
                o.events = {def: "click mouseover,mouseout"};
                break;
            case "popover":
                o.position = p ? _bs2jtPos(p) : "top center";
                o.tipClass = "popover";
                o.events = {def: "click,mouseout"};
                break;
            case "dropdown":
                if (! p && m.parent().hasClass("dropup"))
                    p = "top"; // 如果父级为 dropup 则向上弹出
                o.position = p ? _bs2jtPos(p) : "bottom center";
                o.tipClass = "dropdown-menu";
                o.events = {def: "click,mouseout"};
                break;
        }

        n.data("tooltip", m.tooltip(o).data("tooltip"));
    });

    box.find("[type=date]" ).dateinput( );
    box.find("[type=range]").rangeinput();

    return box;
}

/**
 * 配置浮窗或区域
 * @param {Object} opts 选项
 */
function HsInit(opts) {
    var box = jQuery(this);

    // 自动提取标题, 替换编辑文字
    // 如主键不叫id, 打开编辑页面, 则需加上id=1
    var h = box.children("h1,h2,h3,h4,h5,h6");
    if (h.length) opts.title = h.text();
    if (opts.title) {
        opts.title  =  H$( "&id", box )?
            hsGetLang(opts.title,{'opr':opts.update||hsGetLang("form.update")}):
            hsGetLang(opts.title,{'opr':opts.create||hsGetLang("form.create")});
    }
    if (h.length)h.text(opts.title);

    if (box.data("overlay")) {
        var o = box.data("overlay");
        for(var k in opts  ) {
            var v  = opts[k];
            switch (k) {
                case "class":
                    box.addClass(v);
                    break;
                default:
                    box.css ( k, v);
                    break;
            }
        }
        o.load();
    }
    else if (box.parent( ).data("tabs")) {
        var v = hsGetValue(opts,"title");
        if (v) {
            var o = box.parent().data("tab").find("a");
            if (o.find("span").size( ) ) {
                o = o.find("span:first");
            }
            o.text(v);
        }
    }
}

/**
 * 表单组件
 * @param {Object} opts 选项
 * @param {Element} context 容器
 */
function HsForm(opts, context) {
    var that = _HsInitOpts.call(this, opts, "HsForm");
    if (that)  return that;

    that = this;
    context = jQuery ( context );
    context.data("HsForm", this);
    context.addClass ( "HsForm");

    var loadBox  = context.closest(".load-box");
    var formBox  = context.find   ( "form"    );
    var saveUrl  = hsGetValue(opts, "saveUrl" );
    var loadUrl  = hsGetValue(opts, "loadUrl" );
    var idKey    = hsGetValue(opts, "idKey", "id"); // id参数名, 用于判断编辑还是创建
    var probeMode= hsGetValue(opts, "probeMode"  ); // 刺探模式, 无论有无id都请求信息
    var aloneMode= hsGetValue(opts, "aloneMode"  ); // 独立模式, 请求时不携带上层数据

    if (formBox.length === 0) formBox = context;

    this.context = context;
    this.loadBox = loadBox;
    this.formBox = formBox;

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

    this.validate();
    this.saveInit(saveUrl);
}
HsForm.prototype = {
    load     : function(url, data) {
        jQuery.ajax({
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

    validate : function() {
        this.formBox.validator();
    },
    saveInit : function(act) {
        var url  = this.formBox.attr("action" ) || act;
        var type = this.formBox.attr("method" );
        var enc  = this.formBox.attr("enctype");
        var data = this.formBox;
        var that = this;

        this.formBox.attr(  "action", url  );
        this.formBox.on("submit", function() {
            if (that.formBox.data("validator").checkValidity()) {
                that.formBox.trigger("beforeSave");
                return true;
            }
            return false;
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
            if (typeof(rst.errors) !== "undefined") {
                this.formBox.data("validator").invalidate(rst.errors);
            }
            this.formBox.trigger("saveFail", [rst]);
            return;
        }
        var evt = new jQuery.Event("saveBack");
        this.formBox.trigger(evt, [rst]);
        if (evt.isDefaultPrevented() == false)
            HsClose.call( this.loadBox );
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
    }
};

/**
 * 列表组件
 * @param {Object} opts 选项
 * @param {Element} context 容器
 */
function HsList(opts, context) {
    var that = _HsInitOpts.call(this, opts, "HsList");
    if (that)  return that;

    that = this;
    context = jQuery ( context );
    context.data("HsList", this);
    context.addClass ( "HsList");

    var loadBox  = context.closest(".load-box");
    var listBox  = context.find   (".list-box");
    var pageBox  = context.find   (".page-box");
    var findBox  = context.find   (".find-box");
    var loadUrl  = hsGetValue(opts, "loadUrl" );
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

    var i, a, n, m, u;

    function openHand(evt) {
        //var n = evt.data[0];
        var n = jQuery(this);
        var m = evt.data[1];
        var u = evt.data[2];

        switch (m) {
            case "{CONTEXT}": m = context; break;
            case "{LOADBOX}": m = loadBox; break;
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

    this.listBox.on("change", ".check-all", function() {
        var ck  =  jQuery(this);
        that.listBox.find(".check-one")
                    .prop("checked", ck.prop("checked")).trigger( "change" );
    });
    this.listBox.on("change", ".check-one", function() {
        var cks = that.listBox.find(".check-one");
        var ckd =        cks.filter(":checked"  );
        that.listBox.find(".check-all")
                    .prop("checked", cks.length && cks.length == ckd.length);
    });

    if (loadUrl) this.load(loadUrl, hsSerialArr(loadUrl));
}
HsList.prototype = {
    load     : function(url, data) {
        if (url ) this._url  = url;
        if (data) this._data = hsSerialArr(data);
        jQuery.ajax({
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
        jQuery.ajax({
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
        if (box == "@") box = jQuery(btn).closest(".load-box");
        if (box)
            HsOpen.call(box, url, data, function() {
               that.openBack(btn, this, dat2);
            }).data("related", btn.closest(".load-box")[0]);
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

/**
 * 树型组件
 * @param {Object} opts 选项
 * @param {Element} context 容器
 */
function HsTree(opts, context) {
    var that = _HsInitOpts.call(this, opts, "HsTree");
    if (that)  return that;

    that = this;
    context = jQuery ( context );
    context.data("HsTree", this);
    context.addClass ( "HsTree");

    var loadBox  = context.closest(".load-box");
    var treeBox  = context.find   (".tree-box");
    var findBox  = context.find   (".find-box");
    var loadUrl  = hsGetValue(opts, "loadUrl" );
    var openUrls = hsGetValue(opts, "openUrls");
    var sendUrls = hsGetValue(opts, "sendUrls");
    var linkUrls = hsGetValue(opts, "linkUrls");
    // 数据的节点属性的键
    this.idKey   = hsGetValue(opts, "idKey"  , "id"  );
    this.pidKey  = hsGetValue(opts, "pidKey" , "pid" );
    this.nameKey = hsGetValue(opts, "nameKey", "name");
    this.noteKey = hsGetValue(opts, "noteKey");
    this.typeKey = hsGetValue(opts, "typeKey");
    this.cnumKey = hsGetValue(opts, "cnumKey");
    this.bidKey  = hsGetValue(opts, "bidKey", hsGetConf("tree.bid.key", "bid")); // 移动定位字段(虚拟字段)
    // 根节点信息
    var rootInfo = {
            id   : hsGetValue(opts, "rootId", hsGetConf("tree.root.id", "0")),
            name : hsGetValue(opts, "rootName", hsGetLang("tree.root.name")),
            note : hsGetValue(opts, "rootNote", hsGetLang("tree.root.note"))
        };

    this.context = context;
    this.loadBox = loadBox;
    this.treeBox = treeBox;

    var i, a, n, m, u;

    function openHand(evt) {
        //var n = evt.data[0];
        var n = jQuery(this);
        var m = evt.data[1];
        var u = evt.data[2];

        switch (m) {
            case "{CONTEXT}": m = context; break;
            case "{LOADBOX}": m = loadBox; break;
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

        var tip = n.closest(".tooltip");
        if (tip.length)
            n   = tip.data ( "trigger");

        if (typeof(u) === "function") {
            u.call( that, n, m );
            return;
        }

        if (0 <= u.indexOf("{ID}")) {
            var sid;
            if (0 <= jQuery.inArray(treeBox[0], n.parents())) {
                sid = that.getId (n);
            }
            else {
                sid = that.getSid( );
            }
            if (sid == null) return ;

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

        var tip = n.closest(".tooltip");
        if (tip.length)
            n   = tip.data ( "trigger");

        if (typeof(u) === "function") {
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

    // 当选中时, 在指定区域加载指定页面, 并附带树节点ID
    if (linkUrls) {
        treeBox.on("select", function(evt, id) {
            for (var i = 0; i < linkUrls.length; i ++) {
                jQuery(linkUrls[i][0])
                 .load(linkUrls[i][1].replace('{ID}', encodeURIComponent(id)));
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
        var data = {};
        data[this.pidKey] = this._pid;
        jQuery.ajax({
            "url"       : this._url ,
            "data"      : data,
            "type"      : "POST",
            "dataType"  : "json",
            "action"    : "load",
            "async"     : false,
            "cache"     : false,
            "context"   : this,
            "success"   : function(rst) {
                this.loadBack(rst, pid);
            }
        });
    },
    loadBack : function(rst, pid) {
        rst = hsResponObj(rst );
        var sid = this.getSid();
        if (rst.__success__ === false) return;
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
        nod.addClass("tree-open");

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
            + '<td class="tree-hand"><span class="caret"></span></td>'
            + '<td class="tree-name"></td>'
            + '<td class="tree-cnum"></td>'
            + '</tr></tbody></table>');
        var n, t;

        n = hsGetValue(info, this.nameKey);
        tab.find(".tree-name").text(n);
        if (typeof(this.noteKey) !== "undefined") {
            n = hsGetValue(info , this.noteKey);
            nod.find(".tree-name").attr("title", n);
        }
        if (typeof(this.typeKey) !== "undefined") {
            t = hsGetValue(info , this.typeKey);
            nod.addClass("tree-type-" + t);
        }
        if (typeof(this.cnumKey) !== "undefined") {
            n = hsGetValue(info , this.cnumKey);
            tab.find(".tree-cnum").text(n);
            if (n)
                nod.addClass("tree-fold");
        }
        else {
                nod.addClass("tree-fold");
        }

        if (! t) t = "info";
        if (typeof(this["_fill_"+t]) !== "undefined") {
            this["_fill_"+t].call(this, tab, info);
        }

        tab.prependTo(nod);
    },
    fillCnum : function(cnum, lst) {
        var nod = lst.closest (".tree-node");
        var arr = lst.children(".tree-node");

        if (typeof(cnum) === "undefined")
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

        if (data[this.idKey ] !== undefined)
            this.load(null, this.getPid(data[this.idKey])); // 移动/删除
        if (data[this.pidKey] !== undefined)
            this.load(null, data[this.pidKey]); // 移动
    },

    open     : function(btn, box, url, data) {
        var that = this;
        var dat2 = jQuery.extend({}, hsSerialObj(url), hsSerialObj(data||{}));
        if (box == "@") box = jQuery(btn).parent(".load-box");
        if (box)
            HsOpen.call(box, url, data, function() {
               that.openBack(btn, this, dat2);
            }).data("related", btn.closest(".load-box")[0]);
        else
            hsOpen     (     url, data, function() {
               that.openBack(btn, this, dat2);
            });
    },
    openBack : function(btn, box, data) {
        var that = this;
        btn.trigger("openBack",[box, data]);
        box.on("saveBack", function(evt) {
            if (evt.isDefaultPrevented()) return;
            btn.trigger(evt, [box, data]);
            if (evt.isDefaultPrevented()) return;

            if (data[that.idKey] !== undefined)
                that.load(null, that.getPid(data[that.idKey])); // 修改
            else
                that.load(null, that.getSid( )); // 添加
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
        var lst = nod.children(".tree-list");
        lst.toggle(); nod.trigger("toggle", [id]);
        if (lst.length == 0) this.load(null, id );
        else {
            nod.removeClass("tree-open tree-fold");
            lst.is(":visible") ? nod.addClass("tree-open")
                               : nod.addClass("tree-fold");
        }
    },

    getNode  : function(id) {
        if (typeof(id) === "object")
            return id.closest(".tree-node" );
        else
            return this.treeBox.find( "#tree-node-" + id );
    },
    getPnode : function(id) {
        return this.getNode(id).parent().closest(".tree-node");
    },
    getId    : function(id) {
        if (typeof(id) === "object")
            return this.getId(id.attr("id"));
        else
            return id.toString( ).substr(10);
    },
    getPid   : function(id) {
        return this.getId(this.getPnode(id));
    },
    getRid   : function() {
        return this.getId(this.treeBox.find(".tree-root"));
    },
    getSid   : function() {
        return this.getId(this.treeBox.find(".tree-curr"));
    }
};

/**
 * 组合选择工具之用法:
 * 在表单配置区域添加:
 * <param name="_fill__pick" value="(hsFormFillPick)"/>
 * 在表单选项区域添加:
 * 多选: <ul data-fn="xxxxxx_id[]" data-ft="_pick"></ul>
 * 单选: <input type="hidden" name="x" data-ft="_pick"/>
 * <button type="button" data-toggle="hsPick" data-pick="x/pick.html">Pick</button>
 * 在选择列表配置添加:
 * <param name="_fill__pick" value="(hsListFillPick)"/>
 * 在选择列表头部添加:
 * <td data-ft="_pick"><input type="checkbox" class="check-all"/></td>
 */

/**
 * 选择控件
 * @param {String} url 要打开的选择页地址
 * @param {jQuery} tip 在哪打开
 * @param {jQuery} box 在哪填充
 * @param {Function} fil 填充函数
 * @returns {jQuery}
 */
function HsPick(url, tip, box, fil) {
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
            var val = opt.find(".pick-val").val ();
            var txt = opt.find(".pick-txt").text();
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
        box.trigger( evt, [ v ] );
        if (evt.isDefaultPrevented()) {
            return false;
        }

        fil.call(form , box, v, n, "data");
        return true;
    }

    function pickOpen() {
        var tip = jQuery( this );
        tip.data("pickData", v )
           .addClass("pick-box")
        .toggleClass("pick-mul", mul)
        .on("saveBack", function(evt, rst) {
            if (! rst || ! rst.back
            ||  ! pickItem.apply( self, rst.back )
            ||  ! pickBack())
                return;

            tip.hsClose();
        })
        .on("click", ".ensure", function() {
            var btn = jQuery(this);
            if (! btn.closest(".open-box").is(tip)
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
        win = HsOpen.call(tip, url, undefined, pickOpen);
        win.data("related", btn.closest(".load-box")[0]);
    } else {
        win = hsOpen     (     url, undefined, pickOpen);
    }
    return win;
}

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

    var btn = box.siblings("[data-toggle=hsPick],.pick");
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
                return false;
            });
        }

        if (jQuery.isEmptyObject(v)) {
            reset(box, btn);
        }

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
            box.append(jQuery('<li class="btn btn-default form-control"></li>')
               .append(jQuery('<input type="hidden" class="pick-val"/>').attr("name", n).val(val))
               .append(jQuery('<span class="close pull-right">&times;</span>'))
               .append(jQuery('<span class="pick-txt"></span>').text( txt )).attr( "title" , txt )
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
    var box = cel.closest(".pick-box");
    var mul = box.hasClass("pick-mul");
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

/** jQuery插件整合 **/

// 常用jQuery扩展
jQuery.extend({
    hsNote  : hsNote,
    hsOpen  : hsOpen
});
jQuery.fn.extend({
    hsOpen  : HsOpen,
    hsClose : HsClose,
    hsReady : HsReady,
    hsInit  : HsInit,
    hsForm  : HsForm,
    hsList  : HsList,
    hsTree  : HsTree,
    hsPick  : HsPick
});

// 重写jQuery函数
var _jqAjax = jQuery.ajax;
var _jqLoad = jQuery.fn.load;
jQuery.ajax = function(url, settings) {
    if (typeof(url) === "object") {
        settings = url;
        if (typeof(url["url"]) !== "undefined")
            url  = url["url"];
    }
    return _jqAjax( hsFixUri( url ), settings );
};
jQuery.fn.load = function(url, data, complete ) {
    if ( jQuery.isFunction(  data  )) {
        complete = data ;
        data = undefined;
    }
    if (!jQuery.isFunction(complete)) {
        complete = function() {};
    }

    // 解决重载区域后内部区域未关闭的问题
    if (jQuery.fn.load.caller != hsOpen
    &&  jQuery.fn.load.caller != HsOpen) {
        this.find  (".open-box").hsClose();
        this.filter(".open-box").hsClose();
    }

    this.data( "url" , url ).data( "data" , data );
    this.addClass("load-box").addClass("load-ing");
    return _jqLoad.call(this, hsFixUri(url),data, function() {
        var that = jQuery(this);
        that.removeClass ( "load-ing" );
        complete.apply(that, arguments);
        HsReady .call (that);
    });
};

/**
 * 应用支持
 * 编码原则:
 * 1. 尽可能的少写程序, 用描述化标记代替
 * 2. 使用事件驱动应用, 避免使用配置函数
 * @param {jQuery} $
 */
(function($) {
    $(function() {
        $(this).hsReady();
    });

    // /** 全局事件 **/

    $(document)
    .on("ajaxError", function(evt, xhr, cnf) {
        var rst = hsResponObj(xhr);
        if (typeof(cnf.action) === "undefined" ) {
            return;
        }
        if (typeof(cnf.button) !== "undefined" ) {
            $(cnf.button).trigger(cnf.action+"Error", evt, rst);
        }
        else if (cnf.context instanceof HsForm) {
            cnf.context.formBox.trigger(cnf.action+"Error", evt, rst);
        }
        else if (cnf.context instanceof HsList) {
            cnf.context.listBox.trigger(cnf.action+"Error", evt, rst);
        }
        else if (cnf.context instanceof HsTree) {
            cnf.context.treeBox.trigger(cnf.action+"Error", evt, rst);
        }
    })
    // /** 打开 **/
    .on("click", "[data-toggle=hsOpen],.open", function() {
        var url = $(this).attr("href");
        var box = $(this).attr("data-target");
        if (box) {
            box = _hsTarget(box, this);
        } else {
            var nav = $(this).closest(".nav");
            if (nav.size()) {
                var idx = $(this).closest( "li" ).index();
                box = nav.data("tabs").getPanes().eq(idx);
            }
        }
        box = box ? box.hsOpen(url, undefined, undefined, this)
                  :   $.hsOpen(url, undefined, undefined, this);
        box.data( "related" , $(this).closest(".load-box")[0] );
        return false;
    })
    .on("click", ".close,.cancel", function() {
        var nav = $(this).closest(".nav");
        if (nav.size()) {
            var tabs = nav.data("tabs");
            var idx  = tabs.getIndex( );
            var idz  = $(this).closest("li").index();
            var pane = tabs.getPanes(  ).eq(  idz  );
            if (idx != idz) {
                pane.data("idx", idx );
                pane.children(".load-box").hsClose();
                pane.removeData("idx");
            } else {
                pane.children(".load-box").hsClose();
            }
        } else {
            $( this ).closest(".load-box").hsClose();
        }
        return false;
    })
    // /** 选择 **/
    .on("click", "[data-toggle=hsPick],.pick", function() {
        var url = $(this).attr("href");
        var box = $(this).attr("data-target");
        if (box) {
            box = _hsTarget(box, this);
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
            inp = inp.charAt(0) == ">" ? $(inp, this) : $(inp);
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
    })
    // /** 表单 **/
    .on("save", "form", function(evt) {
        if (evt.isDefaultPrevented()) {
            return;
        }
        var btn = $(this).find(":submit");
        btn.prop("disabled", true );
        btn.data("txt", btn.text());
        btn.text(hsGetLang("form.saving"));
    })
    .on("saveBack saveFail", "form", function() {
        var btn = $(this).find(":submit");
        var txt = btn.data("txt");
        if (txt)  btn.text( txt );
        btn.prop("disabled", false);
    })
    // /** 列表 **/
    .on("click", ".list-box tbody td", function(evt) {
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
    .on("change", ".HsList .check-one", function() {
        // 当选中行时, 开启工具按钮, 否则禁用相关按钮
        var box = $(this).closest(".HsList");
        var len = box.find(".check-one:checked").length;
        box.find(".for-select").prop("disabled", len != 1);
        box.find(".for-checks").prop("disabled", len == 0);
    })
    .on("loadBack", ".HsList .list-box", function() {
        var box = $(this).closest(".HsList");
        box.find(".check-all").prop("checked", false);
        box.find(".for-select,.for-checks").prop("disabled", true);
    })
    // /** 树形 **/
    .on("select loadBack", ".HsTree .tree-node", function() {
        // 当选中非根节点时, 开启工具按钮, 否则禁用相关按钮
        var box = $(this).closest(".HsTree");
        var obj =        box.data( "HsTree");
        box.find(".for-select").prop("disabled", obj.getSid()==obj.getRid());
    })
    // /** 卷帘 **/
    .on("click", "legend.dropdown-toggle,.panel-heading.dropdown-toggle", function() {
        $(this).parent().toggleClass("dropup");
    })
    .on("hsClose", ".load-box", function(evt) {
        if (evt.target !== evt.currentTarget) return;
        var that = this;
        $( this ).removeData("related");
        $(".load-box").not(this).each(function( ) {
            if ($(this).data("related") === that) {
                $(this).hsClose();
            }
        });
    });

    // /** 组件配置 **/

    // 改变tabs和class使其能使用bootstrap的样式
    $.tools.tabs.conf.tabs = "li";
    $.tools.tabs.conf.current = "active";
    $.tools.tooltip.conf.tipClass = "popover";
    $.tools.validator.conf.errorClass = "has-error";
    $.tools.validator.conf.messageClass = "help-block";
    //$.tools.validator.conf.messageClass = "tooltip right fade in";

    // 设置jquery tools遮罩层
    $.tools.overlay.conf.top = "10%",
    $.tools.overlay.conf.left = "center",
    $.tools.overlay.conf.load = false,
    $.tools.overlay.conf.fixed = false,
    $.tools.overlay.conf.oneInstance = false,
    $.tools.overlay.conf.closeOnClick = false,
    $.tools.overlay.conf.mask = {
        color       : "#000",
        opacity     : 0.5,
        loadSpeed   : 0
    };

    // 设置jquery tools国际化
    $.tools.dateinput.conf.format = _hs2jtDF(hsGetLang("date.format"));
    $.tools.dateinput.conf.firstDay = hsGetLang("date.first.day");
    $.tools.dateinput.localize("en", {
        "days"            : hsGetLang("date.LE").join(","),
        "shortDays"       : hsGetLang("date.SE").join(","),
        "months"          : hsGetLang("date.LM").join(","),
        "shortMonths"     : hsGetLang("date.SM").join(",")
    });
    $.tools.validator.localize("en", {
        "*"               : hsGetLang("form.validate"),
        ":url"            : hsGetLang("form.is.not.url"),
        ":email"          : hsGetLang("form.is.not.email"),
        ":number"         : hsGetLang("form.is.not.number"),
        "[required]"      : hsGetLang("form.required"),
        "[requires]"      : hsGetLang("form.requires"),
        "[max]"           : hsGetLang("form.gt.max"),
        "[min]"           : hsGetLang("form.lt.min"),
        "[minlength]"     : hsGetLang("form.lt.minlength"),
        "[type=date]"     : hsGetLang("form.is.not.date"),
        "[type=time]"     : hsGetLang("form.is.not.time"),
        "[type=datetime]" : hsGetLang("form.is.not.datetime"),
        "[data-repeat]"   : hsGetLang("form.is.not.repeat"),
        "[data-unique]"   : hsGetLang("form.is.not.unique")
    });

    // 设置jquery tools表单校验
    $.tools.validator.conf.formEvent = null;
    $.tools.validator.conf.inputEvent = "change";
    $.tools.validator.fn("[requires]", function(input, value) {
        if (input.prop("tagName") == "SELECT") {
            return !!value;
        }
        else if (input.hasClass("check-box") || input.hasClass("radio-box")) {
            return !!input.find( ":checked").length;
        }
        else {
            return !!input.find( ":hidden" ).length;
        }
    });
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
    $.tools.validator.fn("[data-validate]", function(input, value) {
        var fn = input.attr("data-validate");
        var fd = input.data() ? input.data(): window;
        try {
            return hsGetValue(fd, fn).call(this, input, value);
        } catch (ex) {
            if (window.console)
                window.console.log("Call "+ fn +" error: " + ex, input, value);
            return false;
        }
    });
    $.tools.validator.fn("[data-repeat]", function(input, value) {
        return this.getInputs().filter("[name="+input.attr("data-repeat")+"]").val() == value;
    });
    $.tools.validator.fn("[data-unique]", function(input, value) {
        if (!value) return true;
        var ret = true;
        var obj = input.closest(  "form"  );
        var url = input.attr("data-unique");
        var data = {
            "n" : input.attr("name"),
            "v" : value
        };
        url = url.replace(/\{(.*?)\}/, function(x, n) {
            return obj.find("[name='" +n+ "']").val();
        });
        $.ajax({
            "url": url,
            "data": data,
            "type": "POST",
            "dataType": "json",
            "async": false,
            "cache": false,
            "success": function(rst) { ret = rst["__success__"]; }
        });
        return ret;
    });
    // 自定义错误消息, 让错误消息紧贴输入框
    $.tools.validator.addEffect("default", function(errs) {
        var conf = this.getConf();
        $.each(errs, function(i, err) {
            var inp = err.input;
            var msg = inp.data("msg");

            if (msg == null) {
                msg = $(conf.message).addClass(conf.messageClass);
                inp.data("msg", msg).after(msg);
            }

            // 如果错误的项在隐藏的区域里, 则先将隐藏区域打开
            inp.closest(".vh").removeClass( "vh").prev("legend")
                         .parent("fieldset" ).addClass("dropup");
            inp.closest(".form-group").addClass(conf.errorClass);

            msg.css({visibility: 'hidden'}).empty();
            $.each(err.messages, function(i, text ) {
                msg.append($("<div></div>"). text(text));
            });
            msg.css({visibility: 'visible'}).show();

            /*
            msg.css({visibility: 'hidden'}).empty();
            $.each(err.messages, function(i, text ) {
                msg.append($('<div class="tooltip-inner"></div>').text(text))
                   .append($('<div class="tooltip-arrow"></div>'));
            });

            var p1 =  inp.position();
            var p2 = conf.position.split(/,?\s+/);
            var s1 = [inp.outerHeight(true), inp.outerWidth(true)];
            var s2 = [msg.outerHeight(true), msg.outerWidth(true)];

            // adjust Y
            if (p2[0] == 'bottom') p1.top  +=  s1[0];
            if (p2[0] == 'center') p1.top  += (s1[0] / 2) - (s2[0] / 2);

            // adjust X
            if (p2[1] == 'right' ) p1.left +=  s1[1];
            if (p2[1] == 'center') p1.left += (s1[1] / 2) - (s2[1] / 2);

            inp.parent().css({position: "relative"});
            msg.css({visibility: 'visible', position: 'absolute', top: p1.top, left: p1.left}).show();
            */
        });
    }, function(ipts) {
        var conf = this.getConf();
        ipts.each(function( ) {
            var msg = $(this).data ( "msg.el" );
            if (msg) {
                msg.css({visibility: "hidden"});
            }
        }).closest(".form-group").removeClass(conf.errorClass);
    });
})(jQuery);