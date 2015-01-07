package app.hongs.action;

import app.hongs.Core;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.Tree;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 数据校验助手
 * @author Hongs
 *
 * <h3>异常代码</h3>
 * <pre>
 * 代码区间 0x1100~0x110f
 * error.Ex1100=规则格式错误
 * error.Ex1101=找不到表单规则
 * error.Ex1102=找不到规则的类
 * error.Ex1103=找不到规则的方法
 * error.Ex1104=无法获取规则方法
 * error.Ex1105=参数与要求的格式不匹配
 * error.Ex1106=执行规则方法时发生异常
 * </pre>
 */
public class VerifyHelper {

    private Map<String , Map<String, Map > > rules;

    private static final Map<String, String> alias;
    static {
        alias = new HashMap();
        alias.put("textarea", "text");
        alias.put("hidden",   "text");
        alias.put("slider", "number");
        alias.put("switch", "number");
    }
    
    public VerifyHelper() {
        rules = new LinkedHashMap();
    }

    public VerifyHelper addRule(String name, String rule, Map<String, Object> opts) {
        Map rulez  = rules.get(  name  );
        if (rulez == null) {
            rulez  = new LinkedHashMap();
        }   rulez.put  (  rule , opts  );
        return this;
    }

    public VerifyHelper addRule(String name, String rule, String... args) {
        Map<String, Object> opts = new HashMap();
        int i = 0;
        for(String   arg : args) {
            String[] arr = arg.split( "::" , 2 );
            if (arr.length > 1 ) {
                opts.put(arr[0], arr[1]);
            } else {
                opts.put(String.valueOf(++i), arr[0]);
            }
        }
        return addRule(name, rule, opts);
    }

    public VerifyHelper addRulesByForm(String conf, String form) throws HongsException {
        StructConfig cnf = StructConfig.getInstance(conf);
        Map map  = cnf.getForm(form);
        if (map == null) return this;
        map = (Map) map.get("items");

        int i = 0;
        try {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry et = (Map.Entry)it.next();
                String  code = (String) et.getKey();
                Map     opts = (Map)  et.getValue();

                byte required = (Byte) opts.remove("_required");
                if (required > 0) {
                    this.addRule(code, "_required");
                }

                byte repeated = (Byte) opts.remove("_repeated");
                if (repeated > 0) {
                    this.addRule(code, "_repeated");
                }

                String  rule = (String) opts.get("_rule");
                if (null == rule || "".equals(rule)) {
                        rule = (String) opts.get("_type");
                if (null == rule || "".equals(rule)) {
                    continue;
                }
                if (alias.containsKey(rule)) {
                    rule = alias.get (rule);
                }
                    // 将 type 转换为 isType 规则名
                    String c = rule.substring(0, 1);
                    String n = rule.substring(   1);
                    rule = "is"+c.toUpperCase( )+n ;
                }
                opts.put("__conf__", conf);
                opts.put("__form__", form);
                this.addRule(code, rule, opts);
            }
        }
        catch (ClassCastException ex) {
            throw new HongsException(0x1101, "Failed to get rule: "+conf+":"+form);
        }
        catch (IndexOutOfBoundsException ex) {
            throw new HongsException(0x1101, "Failed to get rule: "+conf+":"+form+"#"+i, ex);
        }

        return this;
    }

    public Map verify(Map values, boolean update) throws Wrongs, HongsException {
        Map<String, Object> valuez = new LinkedHashMap();
        Map<String, Wrong > wrongz = new LinkedHashMap();

        for(Map.Entry<String, Map<String, Map>> et : rules.entrySet()) {
            Map<String, Map> rulez  =  et.getValue( );
            String name = et.getKey();
            Object data = Tree.getValue2(values, name);

            Map<String, String> rq = rulez.remove("_required");
            Map<String, String> rp = rulez.remove("_repeated");

            if (rq == null || update) {
                if ( null == data) {
                    continue;
                }
            } else {
                try {
                    data = required(data);
                } catch (Wrong  w) {
                    failed(wrongz, w, name);
                    continue;
                }
            }

            if (rp == null) {
                try {
                    data = verify(name, data, values, rulez, update);
                } catch (Wrong  w) {
                    failed(wrongz, w, name);
                    continue;
                } catch (Wrongs w) {
                    failed(wrongz, w, name);
                    continue;
                }
            } else {
                try {
                    data = repeated(data);
                } catch (Wrong  w) {
                    failed(wrongz, w, name);
                    continue;
                }

                List data2 = new ArrayList();
                if (data instanceof List) {
                    int i3 = 0;
                    for(Object data3 : ((List) data ) ) {
                        String name3 = name+"."+(i3 ++);

                        try {
                            data3 = verify(name3, data3, values, rulez, update);
                        } catch (Wrong  w) {
                            failed(wrongz, w, name3);
                            continue;
                        } catch (Wrongs w) {
                            failed(wrongz, w, name3);
                            continue;
                        }
                        data2.add(data3);
                    }
                } else
                if (data instanceof Map ) {
                    for(Object i3 : ((Map ) data).entrySet()) {
                        Map.Entry e3 = (Map.Entry) i3;
                        Object data3 = e3.getValue( );
                        String name3 = name+"."+((String) e3.getKey());

                        try {
                            data3 = verify(name3, data3, values, rulez, update);
                        } catch (Wrong  w) {
                            failed(wrongz, w, name3);
                            continue;
                        } catch (Wrongs w) {
                            failed(wrongz, w, name3);
                            continue;
                        }
                        data2.add(data3);
                    }
                }

                if (null != data2) {
                    data  = data2;
                }
            }

            Tree.setValue(valuez, data, name);
        }

        if (!wrongz.isEmpty()) {
            throw new Wrongs(wrongz);
        }

        return valuez;
    }

    protected Object verify(String name, Object value, Map values, Map<String, Map> rules2, boolean update) throws Wrong, HongsException {
        for(Map.Entry<String, Map> rule2 : rules2.entrySet()) {
            Map  params = rule2.getValue();
            String rule = rule2.getKey(  );
            value = verify(name, value, values, rule, params, update);
        }
        return value;
    }

    protected Object verify(String name, Object value, Map values, String rule, Map params, boolean update) throws Wrong, HongsException {
        // 放入环境参数
        params.put("__name__"  , name  );
        params.put("__rule__"  , rule  );
        params.put("__update__", update);

        // 调用 rule 指定的静态方法进行校验
        String cls;
        String mtd;
        int pos = rule.lastIndexOf(".");
        if (pos != -1) {
            cls = rule.substring(0 , pos);
            mtd = rule.substring(1 + pos);
        } else {
            cls = this.getClass().getName();
            mtd = rule;
        }

        try {
            Class  kls = Class.forName(cls);
            Method wtd = kls.getMethod(mtd ,
                   new Class[]{ Object.class, Map.class, Map.class });
            return wtd.invoke ( value, values, params );
        }
        catch (   ClassNotFoundException ex) {
            throw new HongsException(0x1102, "Class '" +cls +"' for '"+rule+"' is not exists");
        }
        catch (    NoSuchMethodException ex) {
            throw new HongsException(0x1103, "Method '"+rule+"' for '"+name+"' is not exists");
        }
        catch (        SecurityException ex) {
            throw new HongsException(0x1104, ex);
        }
        catch (   IllegalAccessException ex) {
            throw new HongsException(0x1104, ex);
        }
        catch ( IllegalArgumentException ex) {
            throw new HongsException(0x1105, ex);
        }
        catch (InvocationTargetException ex) {
            Throwable e = ex.getCause();
            if (e instanceof HongsException) {
                throw (HongsException) e;
            } else
            if (e instanceof HongsError    ) {
                throw (HongsError    ) e;
            } else {
                throw new HongsException(0x1106, e);
            }
        }
    }

    protected static void failed(Map<String, Wrong> wrongz, Wrong  wrong , String name) {
        wrongz.put(name, wrong);
    }

    protected static void failed(Map<String, Wrong> wrongz, Wrongs wrongs, String name) {
        for (Map.Entry<String, Wrong> et : wrongs.getWrongs().entrySet()) {
            String n = et.getKey(   );
            Wrong  e = et.getValue( );
            wrongz.put(name+"."+n, e);
        }
    }

    //** 顶级校验器 **/

    public static Object required(Object value) throws Wrong {
        if (value  ==  null ) {
            throw new Wrong("fore.form.required");
        }
        if ("".equals(value)) {
            throw new Wrong("fore.form.required");
        }
        if ((value instanceof List) && ((List) value).isEmpty()) {
            throw new Wrong("fore.form.requreid");
        }
        if ((value instanceof Map ) && ((Map ) value).isEmpty()) {
            throw new Wrong("fore.form.requreid");
        }
        return value;
    }

    public static Object repeated(Object value) throws Wrong {
        if (value instanceof List) {
            return value;
        }
        if (value instanceof Map ) {
            return value;
        }
        throw new Wrong("fore.form.repeated");
    }

    public static Object norepeat(Object value) throws Wrong {
        try {
            repeated(value);
        } catch (Wrong w) {
            return value;
        }
        throw new Wrong("fore.form.norepeat");
    }

    public static Object ignore(Object value, Map values, Map params) {
        return null;
    }

    //** 类型校验器 **/

    public static Object isForm(Object value, Map values, Map params) throws Wrongs, HongsException {
        String conf = Tree.getValue(params, "", "conf");
        String name = Tree.getValue(params, "", "name");
        if (conf == null || !"".equals(conf)) {
            conf = Tree.getValue(params, "","__conf__");
        }
        if (name == null || !"".equals(name)) {
            name = Tree.getValue(params, "","__name__");
        }

        boolean upd = Tree.getValue(params,false, "__update__");
        VerifyHelper veri = new VerifyHelper();
        veri.addRulesByForm(conf, name);
        return  veri.verify(values,upd);
    }

    public static Object isEnum(Object value, Map values, Map params) throws Wrong , HongsException {
        String conf = Tree.getValue(params, "", "conf");
        String code = Tree.getValue(params, "", "code");
        if (conf == null || !"".equals(conf)) {
            conf = Tree.getValue(params, "","__conf__");
        }
        if (code == null || !"".equals(code)) {
            code = Tree.getValue(params, "","__code__");
        }

        Map data = StructConfig.getInstance(conf).getEnum(code);
        if (! data.containsValue(value.toString()) ) {
            throw new Wrong("fore.form.not.in.enum");
        }
        return value;
    }

    public static String isFile(Object value, Map values, Map params) throws Wrong {
        UploadHelper u = new UploadHelper();
        String x = Tree.getValue(params, "", "__name__");
        u.setUploadName(x);
        x = (String) params.get("path");
        if (x != null) u.setUploadPath(x);
        x = (String) params.get("href");
        if (x != null) u.setUploadHref(x);
        x = (String) params.get("name");
        if (x != null) u.setUploadDate(x);
        x = (String) params.get("type");
        if (x != null) u.setAllowTypes(x.split(","));
        x = (String) params.get("extn");
        if (x != null) u.setAllowExtns(x.split(","));

        ActionHelper hlp = Core.getInstance(ActionHelper.class);
        HttpServletRequest req = hlp.getRequest();
        UploadHelper.upload(req, u);
        return u.getResultHref(   );
    }

    public static Object isText(Object value, Map values, Map params) {
        return value;
    }

    public static Object isDate(Object value, Map values, Map params) {
        return value;
    }

    public static Object isTime(Object value, Map values, Map params) {
        return value;
    }

    public static Object isDatetime(Object value, Map values, Map params) {
        return value;
    }

    public static Object isNumber(Object value, Map values, Map params) {
        return value;
    }

    public static Object isTel(Object value, Map values, Map params) {
        return value;
    }

    public static Object isUrl(Object value, Map values, Map params) {
        return value;
    }

    public static Object isEmail(Object value, Map values, Map params) {
        return value;
    }

    /** 内部错误类 **/

    public static class Wrong  extends HongsException {
        public Wrong(String desc, String... prms) {
            super(HongsException.NOTICE, desc);
            this.setLocalizedOptions(prms);
        }

        public Wrong(Throwable cause, String desc, String... prms) {
            super(HongsException.NOTICE, desc, cause);
            this.setLocalizedOptions(prms);
        }
    }

    public static class Wrongs extends HongsException {
        private final Map<String, Wrong> wrongs;

        public Wrongs(Map<String, Wrong> wrongs) {
            super(HongsException.NOTICE, "fore.form.invalid");
            this.wrongs = wrongs;
        }

        public Map<String, Wrong > getWrongs() {
            return wrongs;
        }

        public Map<String, String> getErrors() throws HongsException {
            Map<String, String> errors = new LinkedHashMap();
            for (Map.Entry et : wrongs.entrySet()) {
                Wrong  w = (Wrong )  et.getValue();
                String n = (String)  et.getKey ( );
                String e = w.getLocalizedMessage();
                errors.put(n, e);
            }
            return errors;
        }

        public Map<String, Object> getErtree() throws HongsException {
            Map<String, Object> errors = new LinkedHashMap();
            for (Map.Entry et : wrongs.entrySet()) {
                Wrong  w = (Wrong )  et.getValue();
                String n = (String)  et.getKey ( );
                String e = w.getLocalizedMessage();
                Tree.setValue(errors, e, n);
            }
            return errors;
        }
    }

}
