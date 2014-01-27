/*
HongsCORE(Javascript)
作者: 黄弘 <kevin.hongs@gmail.com>
创建: 2013/01/01
修改: 2014/01/20 23:52:30
依赖:
    jquery.js,
    jquery.tools.js (tabs, overlay, tooltip, validator, dateinput, expose)
    bootstrap.css   (.btn, .alert, .tooltip, .popover, .pagination,
                     .dropdown, .arrow, .caret, .close, .active,
                     .fade, .[in|out], .[position], .has-error)
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
        if (arguments.length == 1) {
            arguments[1] = location.href;
        }
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
        } else {
            if (b == '@')
                return hsGetParams(arguments[1], arguments[0]);
            else
                return hsGetParam (arguments[1], arguments[0]);
        }
    case '$':
    case '%':
        var c = b == '$' ? window.sessionStorage : window.localStorage;
        if (typeof c == "undefined") {
            throw("H$: Does not support '"+(b == '$' ? 'session' : 'local')+"Storage'");
        }
        if (arguments.length == 1) {
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
    if (typeof rst == "object") {
        if (typeof rst["__success__"] == "undefined") {
            rst["__success__"] = true;
        }
        if (typeof rst["__message__"] == "undefined") {
            rst["__message__"] =  "" ;
        }
        if (rst["__message__"] && !qut) {
            if (  rst["__success__"]  ) {
                hsNote(rst["__message__"], 'alert-success');
            } else {
                alert (rst["__message__"]);
            }
        }
        if (typeof rst["__refresh__"] != "undefined") {
            if (  rst["__refresh__"]  ) {
                location.assign( rst["__refresh__"] );
            }
            else {
                location.reload( );
            }
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
        _hsEachLeaf(obj, function(val,key) {
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
/**
 * 序列化为对象, 供进一步处理
 * @param {Object,Array,String,Elements} obj
 * @return {Object}
 */
function hsSerialObj(obj) {
    var arr = hsSerialArr(obj);
    obj = {};
    for (var i = 0; i < arr.length; i ++) {
        hsSetValue(arr[i].name, arr[i].value);
        obj[arr[i].name] = arr[i].value;
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
        if (arr[i]["name"] == name) {
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
        if (arr[j]["name"] == name) {
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
        if (arr == null) break;
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
        return hsGetArray(obj, path, def);
    }
    if (typeof path == "number") {
        return hsGetArray(obj,[path],def);
    }
    if (typeof path != "string") {
        throw("hsGetValue: 'path' must be a string");
    }

    path = path.replace(/\]\[/g, ".")
               .replace(/\[/   , ".")
               .replace(/\]/   , "" )
               .replace(/\.+$/ , "" ) // a[b][c][] 与 a.b.c 一样, 应用场景: 表单中多选项按 name[] 提取数据
               .split  (/\./ );
    return hsGetArray(obj, path, def);
}
/**
 * 从树对象获取值(hsGetValue的底层方法)
 * @param {Object,Array} obj
 * @param {Array} keys ['a','b']
 * @param def 默认值
 * @return 获取到的值, 如果没有则取默认值
 */
function hsGetArray (obj, keys, def) {
    if (!obj) {
        return null;
    }
    if (!jQuery.isArray(obj ) && !jQuery.isPlainObject(obj )) {
        throw("hsGetArray: 'obj' must be an array or object");
    }
    if (!jQuery.isArray(keys)) {
        throw("hsGetArray: 'keys' must be an array");
    }
    if (!keys.length) {
        throw("hsGetArray: 'keys' can not be empty");
    }

    var i; var k;
    for(i = 0; i < keys.length; i ++) {
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
        hsSetArray(obj, path, val); return;
    }
    if (typeof path == "number") {
        obj[path] = val; return;
    }
    if (typeof path != "string") {
        throw("hsSetValue: 'path' must be a string");
    }
    path = path.replace(/\]\[/g, ".")
               .replace(/\[/   , ".")
               .replace(/\]/   , "" )
               .split  (/\./ );
    hsSetArray(obj, path, val);
}
/**
 * 向树对象设置值(hsSetValue的底层方法)
 * @param {Object,Array} obj
 * @param {Array} keys ['a','b']
 * @param val
 */
function hsSetArray (obj, keys, val) {
    if (!obj) {
        return;
    }
    if (!jQuery.isPlainObject(obj)) {
        throw("hsSetArray: 'obj' must be an object");
    }
    if (!jQuery.isArray(keys)) {
        throw("hsSetArray: 'keys' must be an array");
    }
    if (!keys.length) {
        throw("hsSetArray: 'keys' can not be empty");
    }

    var i, k, t = keys[0];
    for(i = 0; i < keys.length -1; i ++) {
        k = keys[ i ];
        t = keys[i+1];
        if (!t)t = -1;
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
    if (typeof HsCONF[key] != "undefined") {
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
 * 格式化日期
 * @param {Date} date
 * @param {String} format
 * @return {String}
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
    if (!(this instanceof func)) {
        var inst = this.data(name);
        if (!inst) {
            this.data(name , inst).addClass(name);
            inst = new func(opts, this);
        }
        return inst;
    }
    else {
        if (opts) for (var k in opts) {
            // 允许扩展已有方法, 添加或重写方法/属性
            if (this[k] != undefined || k.substring(0, 1) == '_') {
                this[k]  = opts[k];
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
            v = /(true|yes|ok)/i.test(v);
            break;
        default:
            if (/^\s*(\[.*\]|\{.*\})\s*$/.test(v))
                v = eval('('+v+')');
            else  if  (/^\s*(\(.*\))\s*$/.test(v))
                v = eval( v );
        }
        if (n == "") continue;
        hsSetValue(obj, n, v);
    }
    return obj;
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
    var tabs = box.parent().data("tabs");
    if (tabs) {
        tabs.click(box.index());
        var tab = tabs.getCurrentTab(  );
        tab.show().find( "a" ).text( hsGetLang("tab.loading"));
        box = tabs.getCurrentPane();
        if (! box.data("tabs")) {
            box.data("tab" , tab  );
            box.data("tabs", tabs );
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
    }
    else if (prt.data("tabs")) {
        prt.data("tabs").click( 0 );
        prt.data("tab").hide();
        prt.removeData("tabs");
    }
    else if (prt.data("baks")) {
        prt.data("baks").appendTo(prt);
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
    var box = jQuery(this);
    box.trigger("hsReady");
    if (box.data("overlay")
    &&  box.children("object.config[name=hsInit]").size() == 0) {
        box.data("overlay").load(); // 有hsIint则交给HsInit去处理
    }
    
    var $ = jQuery;
    
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
        if (typeof prnt[func] === "function" ) prnt[func](opts);
    }).remove();

    /** 样式相关处理 **/

    box.find('*').contents().filter(function() {
        // 清除全部空白文本节点, 避免在chrome等浏览器中显示空白间隔
        return 3 == this.nodeType && /^\s+$/.test(this.nodeValue);
    }).remove();
    box.find('input').each(function() {
        // 为所有的input加上type-class, 方便设置样式, 兼容老浏览器
        $(this).addClass("input-" + $(this).attr("type"));
    });
    box.find("fieldset legend.dropdown-toggle").click(function() {
        // 如果fieldset legend为dropdown toggle, 则点击显示或隐藏
        $(this).closest("fieldset").toggleClass("dropup")
            .find(".dropdown-body").toggleClass("vh");
    }).closest("fieldset").not(".dropup")
            .find(".dropdown-body").toggleClass("vh");

    /** jquery tools, bootstrap 语义标签解析 **/

    box.find("[data-toggle=tabs],[data-toggle=pills]").each(function() {
        var m = $(this), n;
        do {
            n = m.attr("data-target");
            if (n) break;
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
            if (n) break;
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
            if (n) break;
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

    /** jquery tools, bootstrap 初始配置处理 **/

    // 自动提取标题, 替换编辑文字
    var h = box.children("h1,h2,h3,h4,h5,h6");
    if (h.length) opts.title = h.text();
    if (opts.title) {
        opts.title = hsGetParam( "&id", box )?
            hsGetLang(opts.title, {'opt': opts.update || hsGetLang("form.update")}):
            hsGetLang(opts.title, {'opt': opts.create || hsGetLang("form.create")});
    }
    if (h.length) h.text ( opts.title );

    var v, o, c;
    if (box.data( "overlay" )) {
        o = box.data( "overlay" );  c = o.getConf();
        v = hsGetValue(opts, "top"); if (v) c.top = v;
        v = hsGetValue(opts, "left"); if (v) c.left = v;
        v = hsGetValue(opts, "width"); if (v) box.css("width" , v);  o.load();
    }
    else if (box.parent().data("tabs")) {
        v = hsGetValue(opts, "title"); if (v) box.parent().data("tab").find("a").text(v);
    }
}

/**
 * 表单组件
 * @param {Object} opts 选项
 * @param {Element} context 容器
 */
function HsForm(opts, context) {
    var data = _HsInitOpts.call(this, opts, "HsForm");
    if (data) return data;  context = jQuery(context);

    var loadBox  = context.closest(".load-box");
    var formBox  = context.find   ( "form"    );
    var saveUrl  = hsGetValue(opts, "saveUrl" );
    var loadUrl  = hsGetValue(opts, "loadUrl" );
    var loadNoId = hsGetValue(opts, "loadNoId"); // 加载即使没有id
    var dontRdLd = hsGetValue(opts, "dontRdLd"); // 不读取加载数据
    var idVar    = hsGetValue(opts, "idVar" , hsGetConf("model.id.var" , "id" ));

    if (formBox.length === 0) formBox = context;

    this.context = context;
    this.loadBox = loadBox;
    this.formBox = formBox;

    var ld, id, a, i;

    ld = hsSerialArr( loadUrl );
    if (!dontRdLd) {
        a = hsSerialArr(loadBox.data("url" ));
        for(i = 0; i < a.length; i ++) {
            ld.push(a[i]);
        }
        a = hsSerialArr(loadBox.data("data"));
        for(i = 0; i < a.length; i ++) {
            ld.push(a[i]);
        }
    }
    id = hsGetSeria (ld, idVar);
    if ( loadNoId || id.length) {
        this.load(loadUrl , ld);
    }
    else {
        this.fillData({});
    }

    /**
     * 使用初始化数据填充表单
     * 在打开表单窗口时, 可能指定一些参数(如父ID, 初始选中项等)
     * 这时有必要将这些参数值填写入对应的表达项, 方便初始化过程
     */
    var n, v;
    for(i = 0; i < ld.length; i ++) {
        n = ld[i].name ;
        v = ld[i].value;
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
        if (rst.__success__ == false) return;
        if (rst.data) this.fillData( rst.data );
        if (rst.info) this.fillInfo( rst.info );
        this.formBox.trigger("loadBack", [rst]);
    },
    fillData : function(data) {
        var nodes, datas, i, n, t, v, inp, opt, lab, vk, tk, tp;
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

            if (typeof this["_fill_"+n] != "undefined") {
                v = this["_fill_"+n].call(this, inp, v, n, "data");
            }
            // 按类型填充
            else if (inp.attr("data-ft")) {
                t =  inp.attr("data-ft");
            if (typeof this["_fill_"+t] != "undefined") {
                v = this["_fill_"+t].call(this, inp, v, n, "data");
            }}
            if (! v) continue;

            if (i == 0) {
                this._fill__review(inp, v, n, t);
            }
            else if (inp.prop("tagName") == "SELECT") {
                this._fill__select(inp, v, n, t);
            }
        }
        delete this._data;
    },
    fillInfo : function(info) {
        var nodes, infos, i, n, t, v, inp, vk, tk;
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

            if (typeof this["_fill_"+n] != "undefined") {
                v = this["_fill_"+n].call(this, inp, v, n, "info");
            }
            // 按类型填充
            else if (inp.attr("data-ft")) {
                t =  inp.attr("data-ft");
            if (typeof this["_fill_"+t] != "undefined") {
                v = this["_fill_"+t].call(this, inp, v, n, "info");
            }}
            if (! v && (v !== 0 || v !== "")) continue;

            if (i == 0) {
                v = this._fill__review( inp, v, n, t );
                inp.text(v);
            }
            else {
                inp.val (v).change();
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
                    "error"     : function() { return false;
