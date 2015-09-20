
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
                var lb = jQuery(arguments[1]).closest(".loadbox");
                arguments[1] = hsSerialArr(lb);
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
            throw("H$: Does not support '"+(b=='$' ? 'session':'local')+"Storage'");
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
            rst.ok = true ;
        } else
        if (rst.ok === "0") {
            rst.ok = false;
        }
        if (typeof(rst.err) === "undefined") {
            rst.err =  "" ;
        }
        if (typeof(rst.msg) === "undefined") {
            rst.msg =  "" ;
        }
        // 成功失败消息处理 (失败则直接弹对话框)
        if (! qut && ! self.HsGONE) {
            if (! rst.ok) {
                if (rst.msg) {
                    alert(rst.msg);
                } else {
                    alert(hsGetLang("error.unkwn"));
                }
            } else {
                if (rst.msg) {
                    jQuery.hsNote(rst.msg, 'alert-success');
                }
            }
        }
        // 服务器端要求跳转 (通常为未登录无权限)
        if (typeof(rst["goto"]) !== "undefined") {
            if/**/(rst["goto"]) {
                location.assign(rst["goto"]);
            } else {
                location.reload();
            }
            self.HsGONE = true;
            delete rst["goto"];
        }
        // 针对特定数据结构
        if (typeof(rst['data']) !== "undefined") {
            jQuery.extend(rst , rst['data']);
            delete rst['data'];
        }
    }
    return rst;
}

/**
 * 序列化为数组, 供发往服务器(类似 jQuery.fn.serializeArray)
 * @param {String|Object|Array|Elements} obj
 * @param {Array}
 */
function hsSerialArr(obj) {
    var arr = [];
    var typ = !jQuery.isPlainObject() ? jQuery.type(obj) : "objact";
    switch (typ) {
        case "string":
            var ar1, ar2, key, val, i = 0;
            ar1 = obj.split('#' , 2);
            if (ar1.length > 1) obj = ar1[0];
            ar1 = obj.split('?' , 2);
            if (ar1.length > 1) obj = ar1[1];
            ar1 = obj.split('&');
            for ( ; i < ar1.length ; i ++ ) {
                ar2 = ar1[i].split('=' , 2);
                if (ar2.length > 1) {
                    key = decodeURIComponent (ar2[0]);
                    val = decodeURIComponent (ar2[1]);
                    arr.push({name: key, value: val});
                }
            }
            break;
        case "objact":
            hsForEach(obj,function(val,key) {
                if (key.length > 0) {
                    key = key.join('.'/**/);
                    arr.push({name: key, value: val});
                }
            });
            break;
        case "object":
            obj = jQuery( obj );
            if (obj.data("href")) {
                arr = [];
                var pos ;
                var url = obj.data("href");
                var dat = obj.data("data");
                pos = url.indexOf("?");
                if (pos != -1) {
                    hsSerialMix(arr, hsSerialArr(url.substring(pos+1)));
                }
                pos = url.indexOf("#");
                if (pos != -1) {
                    hsSerialMix(arr, hsSerialArr(url.substring(pos+1)));
                }
                if (dat) {
                    hsSerialMix(arr, hsSerialArr(dat));
                }
            } else {
                arr = jQuery(obj).serializeArray();
            }
            break;
        case "array" :
            arr = obj;
            break;
    }
    return  arr;
}
/**
 * 序列化为字典, 供快速地查找(直接使用object-key获取数据)
 * @param {String|Object|Array|Elements} obj
 * @return {Object}
 */
function hsSerialDic(obj) {
    var arr = hsSerialArr(obj);
    var reg = /(\.\.|\.$)/;
    obj = {};
    for(var i = 0 ; i < arr.length ; i ++) {
        var k = arr[i].name ;
        var v = arr[i].value;
        if (k.length == 0) continue;
        k = k.replace(/\]\[/g, ".")
             .replace(/\[/   , ".")
             .replace(/\]/   , "" );
        if (reg.test( k )) { // a.b. 或 a..b 都是数组
            if (obj[k]===undefined) {
                obj[k]=[ ];
            }
            obj[k].push(v);
        } else {
            obj[k]    = v ;
        }
    }
    return  obj;
}
/**
 * 序列化为对象, 供进一步操作(可以使用hsGetValue获取数据)
 * @param {String|Object|Array|Elements} obj
 * @return {Object}
 */
function hsSerialObj(obj) {
    var arr = hsSerialArr(obj);
    obj = {};
    for(var i = 0; i < arr.length; i ++) {
        hsSetValue(obj, arr[i].name, arr[i].value);
    }
    return obj;
}
/**
 * 将 ar2 并入 arr 中, arr 和 ar2 必须都是 serializeArray 结构
 * @param {Array} arr
 * @param {Array} ar2
 */
function hsSerialMix(arr, ar2) {
    var map = {};
    for(var i =  0, j = ar2.length  ; i < j; i ++) {
        map[ar2[i].name] = 1 ;
    }
    for(var i = -1, j = arr.length-1; i < j; j --) {
        if (map[arr[j].name]) {
            arr.splice(j , 1);
        }
    }
    jQuery.merge(arr, ar2);
}
/**
 * 获取多个序列值
 * @param {Array} arr 使用 hsSerialArr 获得
 * @param {String} name
 * @return {Array}
 */
function hsGetSerias(arr, name) {
    var val = [];
    for(var i = 0; i < arr.length; i ++) {
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
    for(var i = 0 ; i < value.length; i ++) {
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
    hsSetSerias(arr, name, value != undefined && value != null ? [value] : []);
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
        url += "&"+name+"="+encodeURIComponent(value[i]);
    }
    if (url.indexOf("?") < 0 ) {
        url  = url.replace("&", "?");
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
    return hsSetParams(url, name, value != undefined && value != null ? [value] : []);
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
        return _hsGetPoint(obj, path, def);
    }
    if (typeof(path) === "number") {
        return _hsGetPoint(obj,[path],def);
    }
    if (typeof(path) !== "string") {
        throw("hsGetValue: 'path' must be a string");
    }
    var keys = _hsGetDakey(path);
    return _hsGetPoint(obj, keys, def);
}
/**
 * 从树对象获取值(hsGetValue的底层方法)
 * @param {Object|Array} obj
 * @param {Array} keys ['a','b']
 * @param def 默认值
 * @return 获取到的值, 如果没有则取默认值
 */
function _hsGetPoint(obj, keys, def) {
    if (!obj) {
        return def;
    }
    if (!jQuery.isArray(obj ) && !jQuery.isPlainObject(obj )) {
        throw("_hsGetPoint: 'obj' must be an array or object");
    }
    if (!jQuery.isArray(keys)) {
        throw("_hsGetPoint: 'keys' must be an array");
    }
    if (!keys.length) {
        throw("_hsGetPoint: 'keys' can not be empty");
    }
    return _hsGetDepth(obj, keys, def, 0);
}
function _hsGetDepth(obj, keys, def, pos) {
    var key = keys[pos];
    if (obj == null) {
        return def;
    }

    // 按键类型来决定容器类型
    if (key == null) {
        if (keys.length == pos + 1) {
            return obj;
        } else {
            return _hsGetDapth(obj, keys, def, pos + 1);
        }
    } else
    if (typeof(key) == "number") {
        // 如果列表长度不够, 则直接返回默认值
        if (obj.length  <= key ) {
            return def;
        }

        if (keys.length == pos + 1) {
            return obj[key] || def;
        } else {
            return _hsGetDepth(obj[key], keys, def, pos + 1);
        }
    } else {
        if (keys.length == pos + 1) {
            return obj[key] || def;
        } else {
            return _hsGetDepth(obj[key], keys, def, pos + 1);
        }
    }
}
function _hsGetDapth(lst, keys, def, pos) {
    var col = [];
    for(var i = 0; i < lst.length; i ++) {
        var obj  = _hsGetDepth(lst[i], keys, def, pos);
        if (obj !=  null) {
            col.push(obj);
        }
    }
    if (!jQuery.isEmptyObject(col)) {
        return col;
    } else {
        return def;
    }
}
function _hsGetDakey(path) {
    path = path.replace(/\]\[/g, ".")
               .replace(/\[/   , ".")
               .replace(/\]/   , "" )
               .split  (/\./ );
    var i , keys = [];
    for(i = 0; i < path.length; i ++) {
        var keyn = path[i];
        if (keyn.substr(0, 1) == '~') {
            keys.push(parseInt(keyn.substr(1)));
        } else
        if (keyn.length == 0 && i!=0) {
            keys.push(null);
        } else
        {
            keys.push(keyn);
        }
    }
    return keys;
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
        _hsSetPoint(obj, path, val); return;
    }
    if (typeof(path) === "number") {
        _hsSetPoint(obj,[path],val); return;
    }
    if (typeof(path) !== "string") {
        throw("hsSetValue: 'path' must be a string");
    }
    var keys = _hsGetDakey(path);
    _hsSetPoint(obj, keys, val );
}
/**
 * 向树对象设置值(hsSetValue的底层方法)
 * @param {Object|Array} obj
 * @param {Array} keys ['a','b']
 * @param val
 */
function _hsSetPoint(obj, keys, val) {
    if (!obj) {
        return;
    }
    if (!jQuery.isPlainObject(obj)) {
        throw("_hsSetPoint: 'obj' must be an object");
    }
    if (!jQuery.isArray(keys)) {
        throw("_hsSetPoint: 'keys' must be an array");
    }
    if (!keys.length) {
        throw("_hsSetPoint: 'keys' can not be empty");
    }
    _hsSetDepth(obj, keys, val, 0);
}
function _hsSetDepth(obj, keys, val, pos) {
    var key = keys[pos];

    // 按键类型来决定容器类型
    if (key == null) {
        if (obj == null) {
            obj =  [];
        }

        if (keys.length == pos + 1) {
            obj.push(val);
        } else {
            obj.push(_hsSetDepth(null, keys, val, pos + 1));
        }

        return obj;
    } else
    if (typeof(key) == "number") {
        if (obj == null) {
            obj =  [];
        }

        // 如果列表长度不够, 填充到索引的长度
        if (obj.length <= key) {
            for(var i = 0; i <= key; i++) {
                obj.push(null);
            }
        }

        if (keys.length == pos + 1) {
            obj[key] = val;
        } else {
            obj[key] = _hsSetDepth(obj[key], keys, val, pos + 1);
        }

        return obj;
    } else {
        if (obj == null) {
            obj =  {};
        }

        if (keys.length == pos + 1) {
            obj[key] = val;
        } else {
            obj[key] = _hsSetDepth(obj[key], keys, val, pos + 1);
        }

        return obj;
    }
}

/**
 * 遍历对象或数组的全部叶子节点
 * @param {Object,Array} data
 * @param {Function} func
 */
function hsForEach(data, func) {
    var path = [];
    if (arguments.length>2) {
        path = arguments[2];
    }
    if (jQuery.isPlainObject(data)) {
        for (var k in data) {
            hsForEach(data[k], func, path.concat([k]));
        }
    }
    else if (jQuery.isArray (data)) {
        for (var i = 0; i < data.length; i ++) {
            hsForEach(data[i], func, path.concat([i]));
        }
    }
    else if (path.length > 0) {
        func(data, path);
    }
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
 * @param {Object|Array} rep 替换参数, {a:1,b:2} 或 [1,2]
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
        key = key.replace( /\$(\w+|\{.+?\})/gm, function(w) {
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
    uri= hsFixUri   (uri);
    if (typeof(HsAUTH[uri]) !== "undefined") {
        return HsAUTH[uri];
    }
    else {
        return false;
    }
}
/**
 * 补全URI为其增加前缀
 * @param {String} uri
 * @return {String} 完整的URI
 */
function hsFixUri   (uri) {
    if (/^(\w+:\/\/|\/|\.|\.\.)/.test(uri) === false) {
        var pre = HsCONF["BASE_HREF"];
        if (pre == undefined) {
            pre = jQuery("base").attr("href").replace(/\/$/, '');
            HsCONF["BASE_HREF"] = pre;
        }
        return pre +"/"+ uri;
    }
    else {
        return uri;
    }
}
/**
 * 补全URI为其设置参数
 * 注意: 参数必须是单个的, 对多个参数如 &a[]=$a&a[]=$a 只会设置两个一样的值
 * @param {String} uri
 * @param {Object} pms 可以是 hsSerialArr 或 .loadbox 节点
 * @returns {String} 完整的URI
 */
function hsFixPms   (uri, pms) {
    if (pms instanceof Element || pms instanceof jQuery) {
        pms = jQuery(pms).closest(".loadbox");
        pms = hsSerialArr(pms);
    }
    pms = hsSerialObj (pms);
    return uri.replace(/\$(\w+|\{.+?\})/gm , function(w) {
        if (w.substring(0 , 2) === "${") {
            w = w.substring(2, w.length -1);
        }
        else {
            w = w.substring(1);
        }
        w = hsGetValue(pms, w);
        return  w || "";
    });
};

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
  if (typeof(text) === "string") {
    if ( /^\d+$/.test(text)) {
      text = parseInt(text);
    }
  }

  if (typeof(text) === "number") {
    if (text <= 2147483647) {
      text = text * 1000 ;
    }
    return new Date(text);
  }

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
$.hsNote = function(msg, cls, sec) {
    if (! cls) cls = "alert-info";
    if (! sec) sec = 5;

    var div = $('<div class="alert alert-dismissable">'
              + '<button type="button" class="close">&times;</button>'
              + '<div class="alert-body notebox"></div></div>')
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

$.fn.jqLoad = $.fn.load;
$.fn.hsLoad = function(url, data, complete) {
    if ( $.isFunction(  data  )) {
        complete = data ;
        data = undefined;
    }
    if (!$.isFunction(complete)) {
        complete = function() {};
    }

    var dat = data ? hsSerialArr(data): [];
    this.data( "href", url )
        .data( "data", dat )
        .addClass("loadbox")
        .addClass("loading");

    /**
     * 为了给加载区域内传递参数
     * 通常将参数附加在请求之上
     * 但这样会导致静态页不缓存
     * 故, 如果是 html 且无参数"_=RANDOM"
     * 则不传递任何参数到服务端
     */
    var pos;
    pos = url.indexOf(/***/"#");
    if (pos != -1) {
        url = url.substring(0, pos);
    }
    pos = url.indexOf('.html?');
    if (pos != -1 && !hsGetParam(url, '_') && !hsGetSeria(dat, '_')) {
        url = url.substring(0, pos + 5);
        dat = undefined;
    } else
    if (dat.length == 0) {
        dat = undefined;
    }
    url = hsFixUri(url);

    return $.fn.jqLoad.call(this, url, dat, function() {
        $(this).removeClass("loading").hsReady();
        complete.apply(this,arguments);
    });
};
$.fn.hsOpen = function(url, data, complete) {
    var prt = $(this);
    var box;
    var ref;
    var tab;

    if (prt.is(".panes")) {
        prt = prt.data("tabs");
        prt = prt.hsTadd(url );
        tab = prt[0];
        prt = prt[1];
    } else
    if (prt.is( ".nav" )) {
        prt = prt.hsTadd(url );
        tab = prt[0];
        prt = prt[1];
    } else
    if (prt.parent().is(".panes")) {
        tab = prt.parent().data( "tabs").children().eq(prt.index());
    } else
    if (prt.parent().is( ".nav" )) {
        tab = prt;
        prt = prt.parent().data("panes").children().eq(tab.index());
    }

    if (tab) {
        ref = tab.parent().children( ).filter(".active");
        tab.show().find( "a" ).click();
        if (tab.find("a span").size()) {
            tab.find("a span").not(".close" /*btn*/)
                         .text(hsGetLang("loading"));
        } else {
            tab.find("a").text(hsGetLang("loading"));
        }
        // 关闭关联的 tab
        if (prt.children( ).size( ) ) {
            prt.children( ).hsCloze();
            prt.empty();
        }
    } else {
        ref = prt.contents().detach();
    }

    box = $('<div class="openbox"></div>')
          .appendTo(prt).data("ref", ref );
    box.hsLoad( url, data, complete );
    return box;
};
$.fn.hsClose = function() {
    var prt = $(this).parent();
    var box = $(this);
    var tab;

    if (prt.parent().is(".panes")) {
        tab = prt.parent().data( "tabs").children().eq(prt.index());
    } else
    if (prt.parent().is( ".nav" )) {
        tab = prt;
        prt = prt.parent().data("panes").children().eq(tab.index());
        box = prt.children(  ).first(  );
    }

    // 触发事件
    box.trigger("hsClose");

    // 联动关闭
    box.hsCloze(/*recur*/);

    // 恢复标签
    if (tab) {
        var idx = box.data("ref") ? box.data("ref").index() : 0;
//      tab.parent().children().eq(idx).find( "a" ).click() ;
        var tbs = tab.parent().children();
        var pns = prt.parent().children();
        if (tbs) {
            tbs.removeClass("active")
                      .eq(idx)
                  .addClass("active")
                      .show( );
        }
        if (pns) {
            pns.hide().eq(idx).show();
        }
        if (tab.has(".close").size()) {
            tab.remove();
            prt.remove();
        }
    } else
    // 恢复内容
    if (box.data( "ref" )) {
        prt.append(box.data( "ref" )) ; box.remove();
    } else
    // 关闭浮窗
    if (box.closest(".modal").size()) {
        box.closest(".modal").modal("hide").remove();
    } else
    // 关闭通知
    if (box.closest(".alert").size()) {
        box.closest(".alert") /* destroy */.remove();
    }

    return box;
};
$.fn.hsCloze = function() {
    var box = $(this);
    $( document.body).find( ".openbox" ).each(function( ) {
        if (!box.is(this) && box.is($(this).data("rel"))) {
            $(this).hsClose();
        }
    });
    return box;
};
$.fn.hsReady = function() {
    var box = $(this);

    // 为避免在 chrome 等浏览器中显示空白间隔, 清除全部空白文本节点
    box.find("*").contents().filter(function() {
        return this.nodeType === 3 && /^\s+$/.test(this.nodeValue);
    }).remove();

    // 输入类
    box.find("input"/*class*/).each(function() {
        $(this).addClass("input-" + $(this).attr("type"));
    });

    // 折叠栏
    box.find(".dropdown-body").each(function() {
        var u = $(this).parent().is(".dropup");
        $(this).toggleClass("invisible", ! u );
    });

    // 选项卡
    box.find(".tabs,[data-tabs]").each(function() {
        $(this).hsTabs();
    });

    // 国际化
    box.find(".i18n,[data-i18n]").each(function() {
        $(this).hsI18n();
    });

    // 初始化
    if (! box.children("object.config[name=hsInit]").size()) {
        $(this).hsInit();
    }

    box.find("object.config").each(function( ) {
        var prnt = $(this).parent();
        var func = $(this).attr("name");
        var opts = $(this)._hsConfig( );
        if (typeof(prnt[func]) === "function")
            prnt[func]( opts );
    }).remove();

    // 在加载前触发事件
    box.trigger("hsReady");

    // 加载、打开、执行
    box.find("[data-load]").each(function() {
        $(this).hsLoad($(this).attr("data-load"), $(this).attr("data-data"));
    });
    box.find("[data-open]").each(function() {
        $(this).hsOpen($(this).attr("data-open"), $(this).attr("data-data"));
    });
    box.find("[data-eval]").each(function() {
        eval($(this).attr("data-eval"));
    });

    return box;
};

// 选项卡
$.fn.hsTabs = function(rel) {
    var box = $(this);
    if (! rel) {
        if (box.attr("data-tabs")) {
            rel = _hsTarget(this, box.attr("data-tabs"));
            /***/ rel.addClass( "panes");
        } else {
            rel = box.siblings(".panes");
        }
    }
    rel.data( "tabs", box);
    box.data("panes", rel);
    if (box.has(".active").size() === 0) {
        box.find("li>a").first().click();
    }
    return box;
};
$.fn.hsTadd = function(flg) {
    var box = $(this);
    var tab ;
    if (! flg) {
        tab = []; flg = '';
    } else {
        tab = box.find("[data-for='"+flg+"']").closest("li");
    }
    if (! tab.length) {
        tab = $( '<li><a href="javascript:;"><span></span>'
            + '<span class="close">&times;</span></a></li>');
        tab.attr('data-for', flg).appendTo(box);
        return [tab, $( '<div></div>'  ).appendTo(box.data("panes"))];
    } else {
        return [tab, $(box.data("tabs")).children( ).eq(tab.index())];
    }
};

// 初始化
$.fn.hsInit = function(cnf) {
    if (cnf ===  undefined) {
        cnf =  {/**/};
    }
    var box = $(this);

    // 自动提取标题, 替换编辑文字
    // 如主键不叫id, 打开编辑页面, 则需加上id=1
    var h = box.children("h1,h2,h3");
    if (h.length ) {
        cnf.title = h.text();
    }
    if (cnf.title) {
        cnf.title = H$("&id", box)?
            hsGetLang(cnf.title, {'DO':cnf.update||hsGetLang("form.update")}):
            hsGetLang(cnf.title, {'DO':cnf.create||hsGetLang("form.create")});
    }
    if (h.length ) {
        h.text ( cnf.title );
    }

    if (box.is(".modal-body")) {
        var a = box.closest(".modal");
        for(var k in cnf) {
            var v =  cnf[k];
            switch (k) {
                case "title":
                    a.find(".modal-title" ).text( v );
                    break;
                case "modal":
                    a.find(".modal-dialog").addClass("modal-"+v);
                    break;
            }
        }
        a.modal();
    } else
    if (box.closest(".panes").length) {
        var a = box.closest(".panes>*");
        a = box.closest(".panes").data("tabs").children().eq(a.index());
        for(var k in cnf) {
            var v =  cnf[k];
            switch (k) {
                case "title":
                    var x = a.find("a");
                    var y = x.find("span").not(".close");
                    if (y.size()) {
                        y.text(v);
                    } else {
                        x.text(v);
                    }
                    break;
            }
        }
    }

    return box;
};

// 国际化
$.fn.hsI18n = function(rep) {
    var box = $(this);
    var lng;

    if (box.attr("placeholder")) {
        lng = box.attr("placeholder");
        lng = hsGetLang(lng, rep);
        box.attr("placeholder" , lng);
    }
    if (box.attr("title")) {
        lng = box.attr("title");
        lng = hsGetLang(lng, rep);
        box.attr("title" , lng);
    }
    if (box.attr("alt")) {
        lng = box.attr("alt");
        lng = hsGetLang(lng, rep);
        box.attr("alt" , lng);
    }

    if (box.attr("data-i18n")) {
        lng = box.attr("data-i18n");
        lng = hsGetLang(lng, rep);
        box.text ( lng );
    } else
    if ($(this).text()) {
        lng = box.text();
        lng = hsGetLang(lng, rep);
        box.text ( lng );
    }

    return box;
};

$.fn._hsConfig = function() {
    var obj = {};
    var arr = this.find("param");
    for(var i = 0; i < arr.length; i ++) {
        var n = jQuery(arr[i]).attr("name" );
        var v = jQuery(arr[i]).attr("value");
        n = $.trim(n);
        v = $.trim(v);
        switch (v.substring(0, 2)) {
        case "S:": // String
            v = v.substring(2);
            break;
        case "I:": // Integer
            v = v.substring(2);
            v =  parseInt  (v);
            break;
        case "F:": // Float
        case "N:": // Number
            v = v.substring(2);
            v =  parseFloat(v);
            break;
        case "B:": // Boolean
            v = v.substring(2);
            v = !/(false|not|no|f|n|0|)/i.test(v);
            break;
        default:
            (function () {
                if ( /^(\[.*\]|\{.*\})$/ .test(v))
                    v = eval('('+v+')');
                else  if  ( /^(\(.*\))$/ .test(v))
                    v = eval(v );
            }).call(this.get(0));
        }
        if (n) {
            // IE 对相同 name 的 param 只取一个
            // 故需要加编号(#)来表示数组
            n = n.replace(/#.*$/ , ".");
            hsSetValue(obj, n, v);
        };
    }

    // 由于 jQuery.fn.data 在 object 无效
    // 故从 conf 属性里提取额外配置
    this.each(function() {
        var c = this["conf"];
        if (c !== undefined) {
            for(var n in c ) {
                var v  = c[n];
                hsSetValue(obj, n, v);
            }
        }
    });

    return obj;
};
$.fn._hsTarget = function(selr) {
    var elem = this;
    var flag = selr.charAt(0);
    var salr = selr.substr(1);
    switch (flag) {
        case '>':
        case '~':
        case '+':
        case ':':
            return salr ? $(selr, elem) : elem;
        case '$':
            return salr ? $(salr, elem) : elem;
        case '%':
            do {
                var x;
                x = elem.closest(".loadbox");
                if (x.size()) {
                    elem = x;
                    break;
                }
                x = elem.closest(".openbox");
                if (x.size()) {
                    elem = x;
                    break;
                }
                elem = document;
            } while (false);
            return salr ? $(salr, elem) : elem;
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
                elem = document;
            } while (false);
            return salr ? $(salr, elem) : elem;
        default :
            return $(selr);
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
    return  inst;
};

// 三态选择
$.propHooks.choosed = {
    get : function(elem) {
        return elem.checked ? true : (elem.indeterminate ?  null : false);
    },
    set : function(elem, stat) {
        if (stat === null) {
            elem.checked = false ;
            elem.indeterminate = true ;
        } else {
            elem.checked = !!stat;
            elem.indeterminate = false;
        }
    }
};

//** Global Events **/

$(document)
.on("ajaxError", function(evt, xhr, cnf) {
    var rst = hsResponObj(xhr);
    if (typeof(cnf.funcName) === "undefined") {
        return;
    }
    if (typeof(cnf.trigger ) !== "undefined") {
        var btn = $(cnf.trigger);
        btn.trigger(cnf.funcName+"Error", evt, rst);
    }
    if (typeof(cnf.context ) !== "undefined") {
        var box;
        if ( typeof(cnf.context.context) !== "undefined") {
            box = $(cnf.context.context);
        } else {
            box = $(cnf.context);
        }
        box.trigger(cnf.funcName+"Error", evt, rst);
    }
})
.on("click", "[data-toggle=hsLoad]",
function(evt) {
    var box = $(this).attr("data-target");
    var url = $(this).attr("data-href");
    var dat = $(this).attr("data-data");
        url = hsFixPms( url , this );
    if (box) {
        box = $(this)._hsTarget(box);
        box.hsLoad(url, dat);
    }
    evt.stopPropagation();
})
.on("click", "[data-toggle=hsOpen]",
function(evt) {
    var box = $(this).attr("data-target");
    var url = $(this).attr("data-href");
    var dat = $(this).attr("data-data");
        url = hsFixPms( url , this );
    if (box) {
        box = $(this)._hsTarget(box);
        box.hsOpen(url, dat);
    } else {
          $.hsOpen(url, dat);
    }
    evt.stopPropagation();
})
.on("click", ".close,.cancel,.goback",
function(evt) {
    var box;
    do {
        box = $(this).closest(".nav>li>a");
        if (box.size()) {
            break;
        }
        if ($(this).closest(".form-group,.dont-close").size()) {
            return;
        }
        box = $(this).closest(".notebox");
        if (box.is(".alert-body")) {
            box = box.closest(".alert");
            break;
        }
        box = $(this).closest(".openbox");
        if (box.is(".modal-body")) {
            box = box.closest(".modal");
            break;
        }
        if ($(this).closest(/*ignore*/".alert,.modal").size()) {
            return;
        }
    } while (false);
    box.hsClose(  );
    evt.stopPropagation();
})
.on("click", ".nav>li>a",
function(evt) {
    var tab = $( this ).parent( );
    var nav = tab.closest(".nav");
    var pns = nav.data ( "panes");
    var idx = tab.index( );
    nav.children().removeClass("active")
            .eq (idx).addClass("active").show();
    if (pns) {
        pns.children( ).hide( ).eq(idx ).show();
    }
    evt.stopPropagation();
})
.on("click", ".dropdown-toggle",
function(evt) {
    if ($(evt.target).is(".dropdown-deny"))return;
    var body = $(this).siblings(".dropdown-body");
    if (body.size() == 0) return;
    var cont = $(this).parent( );
    cont.toggleClass( "dropup" );
    body.toggleClass("invisible", !cont.is(".dropup"));
    evt.stopPropagation();
})
.on("click", "select[multiple]",
function(evt) {
    if (evt.shiftKey || evt.ctrlKey || evt.altKey) {
        return;
    }
    var vals = $(this).data("vals") || [];
    var valz = $(this).val();
    if (!valz || valz.length === 0) {
        vals = [];
    } else {
        $.each(valz, function(x, v) {
            var i = $.inArray(v, vals);
            if (i >= 0) {
                vals.splice(i,1);
            } else {
                vals.push  ( v );
            }
        });
    }
    $(this).data("vals", vals);
    $(this).val ( vals );
});

$(function() {
    $(document).hsReady();
});

})(jQuery);