
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
                var c =  jQuery(arguments[1]).closest(".loadbox");
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
 * @param {Object|String} rst JSON对象/JSON文本或错误消息
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
        } else
        if (rst.charAt(0) === '<') {
            // 某些时候服务器可能出错, 返回错误消息的页面
            // 需要清理其中的html代码, 以供输出简洁的消息
            rst = {
                "ok" : false,
                "err":  "" ,
                "msg":  rst
                    .replace(/<script.*?>.*?<\/script>/img, "")
                    .replace(/<style.*?>.*?<\/style>/img, "")
                    .replace(/<[^>]*?>/g, "")
                    .replace(/&[^&;]*;/g, "")
                    .replace(/^\s*(\r\n|\r|\n)/mg, "")
            };
        } else {
            rst = {
                "ok" : false,
                "err":  "" ,
                "msg":  rst
            };
        }
    }
    if (typeof(rst) === "object") {
        if (typeof(rst.ok ) === "undefined") {
            rst.ok  = true;
        }
        if (typeof(rst.err) === "undefined") {
            rst.err =  "" ;
        }
        if (typeof(rst.msg) === "undefined") {
            rst.msg =  "" ;
        }
        if (! qut) {
            if (rst.ok) {
                if (rst.msg) {
                    jQuery.hsNote(rst.msg, 'alert-success');
                }
            }
            else {
                if (rst.msg) {
                    alert(rst.msg);
                } else {
                    alert(hsGetLang("error.unkwn"));
                }
            }
        }
        if (typeof(rst.to) !== "undefined") {
            if (rst.to) {
                location.assign(rst.to);
            } else {
                location.reload();
            }
            delete rst.to;
        }
    }
    return rst;
}

/**
 * 序列化为数组, 供发往服务器
 * @param {Object|Array|String|Elements} obj
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
        hsEach(obj, function(val,key) {
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
 * @param {Object|Array|String|Elements} obj
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
 * @param {Object|Array} obj
 * @param {Array|String} path ['a','b'] 或 a.b
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
 * @param {Object|Array} obj
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
 * @param {Object|Array} obj
 * @param {Array|String} path ['a','b'] 或 a.b
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
               .split  ( "." );
    hsSetPoint(obj, path, val);
}
/**
 * 向树对象设置值(hsSetValue的底层方法)
 * @param {Object|Array} obj
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
    if ( /^\d+$/.test(date)) {
      date = parseInt(date);
    }
    else {
      date = Date.parse(date.replace(/-/g, "/").replace(/\.\d+$/, ""));
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
          return hsGetLang("date.format.LM")[M];
        }
        else if (len == 3) {
          return hsGetLang("date.format.SM")[M];
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
          return hsGetLang("date.format.LE")[E];
        }
        else {
          return hsGetLang("date.format.SE")[E];
        }
      case 'a':
        if (len >= 4) {
          return hsGetLang("date.format.La")[a];
        }
        else {
          return hsGetLang("date.format.Sa")[a];
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
          for (j = 0; j < hsGetLang("date.format.LM").length; j ++) {
            if (wrd == hsGetLang("date.format.LM")[j]) {
              M = j;
              break;
            }
          }
        }
        else if (len == 3) {
          for (j = 0; j < hsGetLang("date.format.SM").length; j ++) {
            if (wrd == hsGetLang("date.format.SM")[j]) {
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
          for (j = 0; j < hsGetLang("date.format.La").length; j ++) {
            if (wrd == hsGetLang("date.format.La")[j]) {
              A = j;
              break;
            }
          }
        }
        else {
          for (j = 0; j < hsGetLang("date.format.Sa").length; j ++) {
            if (wrd == hsGetLang("date.format.Sa")[j]) {
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
function hsEach(data, func) {
    var path = [];
    if (arguments.length>2) {
        path = arguments[2];
    }
    if (jQuery.isPlainObject(data)) {
        for (var k in data) {
            hsEach(data[k], func, path.concat([k]));
        }
    }
    else if (jQuery.isArray (data)) {
        for (var i = 0; i < data.length; i ++) {
            hsEach(data[i], func, path.concat([i]));
        }
    }
    else {
        func(data, path);
    }
}

/**
 * HongsCORE日期格式转Bootstrap日期格式
 * @param {String} format
 * @return {String)
 */
function _hs2bsDF(format) {
  return format.replace(/a/g , 'P')
               .replace(/m/g , 'i')
               .replace(/M/g , 'm')
               // 交换 H h
               .replace(/H/g , 'x')
               .replace(/h/g , 'H')
               .replace(/x/g , 'h');
}
/**
 * Bootstrap日期格式转HongsCORE日期格式
 * @param {String} format
 * @return {String)
 */
function _bs2hsDF(format) {
  return format.replace(/m/g , 'M')
               .replace(/i/g , 'm')
               .replace(/P/gi, 'a')
               // 交换 H h
               .replace(/H/g , 'x')
               .replace(/h/g , 'H')
               .replace(/x/g , 'h');
}

(function($) {

$.jqAjax = $.ajax;
$.hsAjax = function(url, settings) {
    if (typeof(url) === "object") {
        settings = url;
        if (typeof(url["url"]) !== "undefined")
            url  = url["url"];
    }
    return $.jqAjax( hsFixUri(url) , settings );
};
$.hsNote = function(msg, cls, sec) {
    if (! cls) cls = "alert-info";
    if (! sec) sec = 5;

    var div = $('<div class="alert alert-dismissable">'
              + '<button type="button" class="close">&times;</button>'
              + '<div class="notebox"></div></div>')
             .addClass(cls);
    var btn = div.find( "button" );
    var box = div.find(".notebox").append(msg);
    var ctr = $("#notebox").append(div).show();

    div.slideDown(200);
    btn.click(function() {
        div.slideUp(200, function() {
            div.remove();
            if (ctr.children().size() == 0) {
                ctr.hide();
            }
        });
    });
    setTimeout(function() {
        btn.click();
    } , sec * 1000);

    return box;
};
$.hsOpen = function(url, data, complete) {
    var div = $('<div class="modal"><div class="modal-dialog"><div class="modal-content"><div class="modal-header">'
              + '<button type="button" class="close" data-dismiss="modal">&times;</button>'
              + '<h4 class="modal-title">' +hsGetLang("opening")+ '</h4>'
              + '</div><div class="modal-body openbox">'
              + '</div></div></div></div>')
              .css('z-index', 99999);
    var box = div.find( '.openbox' );
    div.modal();

    box.hsLoad(url, data, complete );
    return box;
};

$.fn.jqLoad = $.fn.load;
$.fn.hsLoad = function(url, data, complete) {
    if ( $.isFunction(  data  )) {
        complete = data ;
        data = undefined;
    }
    if (!$.isFunction(complete)) {
        complete = function() {};
    }

    this.data( "url", url ).data( "data", data );
    this.addClass("loadbox").addClass("loading");
    return $.fn.jqLoad.call(this, hsFixUri(url ), data, function() {
        var that = $(this);
        that.removeClass(  "loading"  );
        complete.apply(this, arguments);
        that.hsReady(    );
    });
};
$.fn.hsOpen = function(url, data, complete) {
    var prt = $(this);
    var box;
    var pre;
    var tab;

    if (prt.is(".panes")) {
        prt = prt.data("rel");
        prt = prt.hsTaba(url);
        tab = prt[0]; prt = prt[1];
    } else
    if (prt.is( ".nav" )) {
        prt = prt.hsTaba(url);
        tab = prt[0]; prt = prt[1];
    } else
    if (prt.parent().is( ".nav" )) {
        tab = prt;
        prt = tab.parent().data("rel").children().eq(tab.index());
    } else
    if (prt.parent().is(".panes")) {
        tab = prt.parent().data("rel").children().eq(prt.index());
    }

    if (tab) {
        pre = tab.parent( ).children( ).filter(".active");
        tab.show().find( "a" ).click();
        if (tab.find("a span").size()) {
            tab.find("a span").not(".close").text(hsGetLang("loading"));
        } else {
            tab.find("a").text(hsGetLang("loading"));
        }
        // 关闭关联的 tab
        if (prt.children( ).size( ) ) {
            prt.children( ).hsCloze();
            prt.empty();
        }
    } else {
        pre = prt.contents().detach();
    }

    box = $('<div class="openbox"></div>')
          .appendTo(prt).data("pre", pre );
    box.hsLoad( url, data, complete );
    return box;
};
$.fn.hsClose = function() {
    var prt = $(this).parent();
    var box = $(this);
    var tab;

    if (prt.parent().is(".panes")) {
        tab = prt.parent().data("rel").children().eq(prt.index());
    } else
    if (prt.parent().is( ".nav" )) {
        tab = prt;
        prt = tab.parent().data("rel").children().eq(tab.index());
        box = prt.children(".openbox");
    }

    // 触发事件
    box.trigger("hsClose");

    // 联动关闭
    box.hsCloze();

    // 恢复标签
    if (tab) {
        var idx = box.data("pre") ? box.data("pre").index() : 0;
        tab.parent().children().eq(idx).find( "a" ).click() ;
        if (tab.has(".close").size()) {
            tab.remove();
            prt.remove();
        }
    } else
    // 恢复内容
    if (box.data( "pre" )) {
        prt.append(box.data( "pre" )) ; box.remove();
    } else
    // 关闭浮窗
    if (box.closest(".modal").size()) {
        box.closest(".modal").modal("hide").remove();
    }

    return box;
};
$.fn.hsCloze = function() {
    var box = $(this);
    $( document.body).find(".openbox").each(function() {
        if (box.is($(this).data("rel"))) {
            $(this).hsClose();
        }
    });
    return box;
};
$.fn.hsReady = function() {
    var box = $(this);

    // 为标签页进行包裹
    if (box.is( ".tabsbox" ) ) {
        box.hsTabw().hsReady();
        return box;
    }

    // 为避免在 chrome 等浏览器中显示空白间隔, 清除全部空白文本节点
    box.find('*').contents().filter(function() {
        return this.nodeType == 3 && /^\s+$/.test(this.nodeValue);
    }).remove();

    // 为所有的 input 加上 input-type , 方便设置样式, 兼容老浏览器
    box.find('input').each(function() {
        $(this).addClass("input-" + $(this).attr("type"));
    });

    // 至少要执行 hsInit
    if (box.children("object.config[name=hsInit]").size( ) === 0) {
        box.hsInit({});
    }

    //** 语义标签解析 **/

    box.find("object.config" ).each(function() {
        var prnt = $(this).parent();
        var func = $(this).attr("name");
        var opts = $(this)._hsConfig( );
        if (typeof(prnt[func]) === "function")
            prnt[func]( opts );
    }).remove();

    box.find("[data-eval]").each(function() {
        eval($(this).attr("data-eval"));
    });
    box.find("[data-load]").each(function() {
        $(this).hsLoad($(this).attr("data-load"));
    });
    box.find("[data-open]").each(function() {
        $(this).hsOpen($(this).attr("data-open"));
    });

    box.find(".nav").each(function() {
        $(this).hsTabs(  );
    });

    box.find(".dropdown-body").each(function() {
        var x = $(this).parent().is(".dropup");
        $(this).toggleClass("invisible" , ! x);
    });

    box.trigger("hsReady");
    return box;
};
$.fn.hsInit = function(cnf) {
    var box = $(this);

    // 自动提取标题, 替换编辑文字
    // 如主键不叫id, 打开编辑页面, 则需加上id=1
    var h = box.children("h1,h2,h3");
    if (h.length ) {
        cnf.title = h.text( );
    }
    if (cnf.title) {
        cnf.title = H$("&id", box)?
            hsGetLang(cnf.title, {'opr':cnf.update||hsGetLang("form.update")}):
            hsGetLang(cnf.title, {'opr':cnf.create||hsGetLang("form.create")});
    }
    if (h.length ) {
        h.text(  cnf.title  );
    }

    if (box.parent().parent().is(".panes")) {
        var a = box.parent();
        var b = box.parent().parent();
        a = b.data("rel").children().eq(a.index());
        for(var k in cnf) {
            var v =  cnf[k];
            switch (k) {
                case "title":
                    var x = a.find( "a"  );
                    var y = x.find("span").not(".close");
                    if (y.size()) {
                        y.text(v);
                    } else {
                        x.text(v);
                    }
                    break;
            }
        }
    } else
    if (box.is(".modal-body")) {
        var a = box.closest(".modal");
        for(var k in cnf) {
            var v =  cnf[k];
            switch (k) {
                case "title":
                    a.find(".modal-title" ).text(v);
                    break;
                case "modal":
                    a.find(".modal-dialog").addClass("modal-"+v);
                    break;
            }
        }
        a.modal();
    }

    return box;
};

// 包裹标签
$.fn.hsTabw = function() {
    var box = $(this);
    var tbs = $('<ul class="nav nav-tabs"><li><a href="javascript:;"></a></li></ul>');
    var pns = $('<div class="panes"></div>');
    var pan = $('<div></div>').appendTo(pns);
    var div = $('<div></div>').appendTo(pan);
    div.append(box.contents()).addClass("openbox");
    box.append(tbs);
    box.append(pns);
    tbs.hsTabs();
    return div;
};
// 关联窗格
$.fn.hsTabs = function(rel) {
    var box = $(this);
    if (! rel) {
        if (box.attr("data-target")) {
            rel = _hsTarget(this, box.attr("data-target"));
                  rel.addClass( "panes");
        } else {
            rel = box.siblings(".panes");
        }
    }
    box.data("rel", rel);
    rel.data("rel", box);
    if (! box.has(".active").size()) {
        box.find("li>a").first().click();
    }
    return box;
};
// 添加标签
$.fn.hsTaba = function(url) {
    var box = $(this);
    var tab;
    if (! url) {
        url  =  '';
        tab  =  [];
    } else {
        tab  =  box.find("[data-for='" + url + "']").closest("li");
    }
    if (! tab.length) {
        tab = $('<li></li>').attr( 'data-for', url )
            .appendTo(box)
            .append($('<a href="javascript:;"></a>')
                .append( '<span></span>' )
                .append( '<span class="close">&times;</span>' )
            );
        return [tab, $('<div></div>').appendTo(box.data("rel"))];
    } else {
        return [tab, box.data("rel").children().eq(tab.index())];
    }
};

$.fn._hsConfig = function() {
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
            (function () {
                if (/^\s*(\[.*\]|\{.*\})\s*$/.test(v))
                    v = eval('('+v+')');
                else  if  (/^\s*(\(.*\))\s*$/.test(v))
                    v = eval(v);
            }).call(this);
        }
        if (n) {
            // IE 对相同 name 的 param 只取一个, 故需要加编号来表示数组
            n = n.replace(/:.*$/ , ".");
            hsSetValue(obj, n, v);
        };
    }
    return obj;
};
$.fn._hsTarget = function(selr) {
    var elem = this;
    var flag = selr.charAt(0);
    var salr = selr.substr(1);
    switch (flag) {
        case '@':
            do {
                var x;

                x = elem.closest(".panes");
                if (x.size()) {
                    elem = x;
                    break;
                }

                x = elem.closest(".openbox");
                if (x.size()) {
                    elem = x;
                    break;
                }

                x = elem.closest(".loadbox");
                if (x.size()) {
                    elem = x;
                    break;
                }
            } while (false);
            return salr ? jQuery(salr, elem) : elem;
        case '>':
            return salr ? jQuery(selr, elem) : elem;
        default :
            return jQuery(selr);
    }
};
$.fn._hsConstr = function(opts, func) {
    var elem = this;
    var name = func.name || /^function (\w+)/.exec(func.toString())[1];
    var inst = elem.data(name);
    if (! inst) {
        inst = new func(opts, elem);
        elem.data(name, inst);
        elem.addClass ( name);
    }
    if (opts) for ( var k in opts ) {
        if ('_'===k.substring(0, 1)
        ||  inst[k] !== undefined ) {
            inst[k]  =  opts[k];
        }
    }
    return inst;
};

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
.on("click", "[data-toggle=hsLoad]",
function(evt) {
    var box = $(this).attr("data-target");
    var url = $(this).attr("data-href");
    if (box) {
        box = $(this)._hsTarget(box);
        box.hsLoad( url );
    }
    evt.stopPropagation();
})
.on("click", "[data-toggle=hsOpen]",
function(evt) {
    var box = $(this).attr("data-target");
    var url = $(this).attr("data-href");
    if (box) {
        box = $(this)._hsTarget(box);
        box.hsOpen( url );
    } else {
          $.hsOpen( url );
    }
    evt.stopPropagation();
})
.on("click", ".nav>li>a",
function(evt) {
    var tab = $( this ).parent( );
    var idx = tab.index  (      );
    var nav = tab.closest(".nav");
    var pns = nav.data   ( "rel");
    nav.children().removeClass("active")
            .eq (idx).addClass("active").show();
    if (pns) {
        pns.children( ).hide( ).eq(idx ).show();
    }
    evt.stopPropagation();
})
.on("click", ".nav>li>a>.close",
function(evt) {
    $(this).closest("a").hsClose();
    evt.stopPropagation();
})
.on("click", ".close,.cancel,.repeal",
function(evt) {
    $(this).closest(".modal,.openbox").hsClose();
    evt.stopPropagation();
})
.on("click", ".dropdown-toggle",
function(evt) {
    var body = $(this).siblings(  ".dropdown-body"  );
    if (body.size() == 0) return;
    var cont = $(this).parent( );
    cont.toggleClass( "dropup" );
    body.toggleClass("invisible",!cont.is(".dropup"));
    evt.stopPropagation();
})
.on("click", "select[multiple]",
function(evt) {
    if (evt.shiftKey || evt.ctrlKey || evt.altKey) {
        return;
    }
    var vals = $(this).data("vals") || [];
    var valz = $(this).val();
    if (valz.length == 0) {
        vals = [];
    } else {
        $.each(valz, function(x,  v  ) {
            var i = $.inArray(v, vals);
            if (i >= 0) {
                vals.splice(i, 1);
            } else {
                vals.push( v );
            }
        });
    }
    $(this).data("vals", vals);
    $(this).val ( vals);
});

$(function () {
    $(document).hsReady();
});

})(jQuery);