package app.hongs.action;

import app.hongs.CoreLocale;
import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.util.Dict;
import app.hongs.util.Synt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * error.Ex1105=不正确的规则调用方法
 * error.Ex1106=执行规则方法时发生异常
 * </pre>
 */
public class VerifyHelper {

    private Map<String, List<Rule>> rules;

    public VerifyHelper() {
        rules = new LinkedHashMap();
    }

    public VerifyHelper addRule(String name, Rule... rule) {
        List rulez = rules.get(name);
        if (rulez == null   ) {
            rulez =  new ArrayList();
            rules.put( name, rulez );
        }
        for(Rule rula : rule) {
            if (rula.params == null) {
                rula.params =  new HashMap();
            }
            rulez.add(rula);
        }
        return this;
    }

    public VerifyHelper addRulesByForm(String conf, String form) throws HongsException {
        FormSet cnf = FormSet.getInstance(conf);
        Map map  = cnf.getForm(form);
        if (map == null) return this;

        FormSet dfs = FormSet.getInstance("default");
        Map tps  = dfs.getEnum("__types__");
        Map pts  = dfs.getEnum("__patts__");

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry et = (Map.Entry)it.next();
            String  code = (String) et.getKey();
            Map     optz = (Map)  et.getValue();
            Map     opts =  new HashMap( optz );
            Object  o;

            o = opts.remove("__required__");
            if (! "".equals(o)) {
                if (Synt.declare(o, false)) {
                    Rule rule = new Required();
                    Map  prms = new HashMap( );
                         rule.setParams(prms );
                    this.addRule( code, rule );
                } else {
                    Rule rule = new Optional();
                    this.addRule( code, rule );
                }
            }

            o = opts.remove("__repeated__");
            if (! "".equals(o)) {
                if (Synt.declare(o, false)) {
                    Rule rule = new Repeated();
                    Map  prms = new HashMap( );
                         rule.setParams(prms );
                    this.addRule( code, rule );
                    if (opts.containsKey("distrinct"))
                        prms.put("distrinct", opts.remove("distrinct"));
                    if (opts.containsKey("minrepeat"))
                        prms.put("minrepeat", opts.remove("minrepeat"));
                    if (opts.containsKey("maxrepeat"))
                        prms.put("maxrepeat", opts.remove("maxrepeat"));
                } else {
                    Rule rule = new Norepeat();
                    this.addRule( code, rule );
                }
            }

            o = opts.remove("default");
            if (o != null) {
                Rule rule = new Default();
                Map  prms = new HashMap();
                     rule.setParams(prms);
                this.addRule( code, rule);
                prms.put("default" , o  );
                prms.put("alwayset", opts.remove("default-alwayset"));
                prms.put("increate", opts.remove("default-increate"));
            }

            String rule = (String) opts.get("__rule__");
            if (null == rule || "".equals(rule)) {
                String type = (String) opts.get("__type__");

                // 类型映射
                if (tps.containsKey(type)) {
                    rule =  tps.get(type ).toString( );
                } else {
                    rule =   "string";
                }

                // 预定正则
                if (pts.containsKey(type)) {
                    opts.put("pattern", pts.get(type));
                }

                // 将 type 转换为 isType 规则名
                String c = rule.substring(0, 1);
                String n = rule.substring(   1);
                rule = "Is"+c.toUpperCase()+ n ;
            }
            if (! rule.contains(".") ) {
                rule = this.getClass().getName()+"$"+rule;
            }

            Rule inst;
            try {
                inst = (Rule) (Class.forName(rule).newInstance());
            }
            catch (ClassNotFoundException ex) {
                throw new HongsException(0x1101, "Failed to get rule: "+rule+" in "+conf+":"+form, ex);
            }
            catch (InstantiationException ex) {
                throw new HongsException(0x1101, "Failed to get rule: "+rule+" in "+conf+":"+form, ex);
            }
            catch (IllegalAccessException ex) {
                throw new HongsException(0x1101, "Failed to get rule: "+rule+" in "+conf+":"+form, ex);
            }
            catch (ClassCastException ex) {
                throw new HongsException(0x1101, "Failed to get rule: "+rule+" in "+conf+":"+form, ex);
            }

            opts.put("__conf__", conf);
            opts.put("__form__", form);
            opts.put("__name__", code);
            inst.setParams(opts);
            addRule(code , inst);
        }

        return this;
    }

    public Map verify(Map values, boolean update) throws Wrongs, HongsException {
        Map<String, Object> valuez = new LinkedHashMap();
        Map<String, Wrong > wrongz = new LinkedHashMap();

        if (values == null) {
            values =  new HashMap();
        }

        for(Map.Entry<String, List<Rule>> et : rules.entrySet()) {
            List<Rule> rulez = et.getValue();
            String name = et.getKey();
            Object data = Dict.getParam(values, name);

            data = verify(values, update, data, name, rulez, wrongz);

            if (data == SKIP) {
                continue;
            }

            Dict.setParam(valuez, data, name);
        }

        if (!wrongz.isEmpty()) {
            throw new Wrongs(wrongz);
        }

        return valuez;
    }

    private Object verify(Map values, boolean update, Object data, String name, List<Rule> rulez, Map wrongz) throws HongsException {
        int i =0;
        for (Rule  rule  :  rulez) {
            i ++;

            rule.setValues(values);
            rule.setUpdate(update);

            try {
                data = rule.verify(data);
            } catch (Wrong  w) {
                failed(wrongz, w , name);
                data =  SKIP;
                break;
            } catch (Wrongs w) {
                failed(wrongz, w , name);
                data =  SKIP;
                break;
            }
            if (data == SKIP ) {
                break;
            }

            if (rule instanceof Repeated) {
                List<Rule> rulex = rulez.subList(i, rulez.size());
                data = repeat(values, update, data, name, rulex, wrongz, rule.params);
                break;
            }
        }
        return  data ;
    }

    private Object repeat(Map values, boolean update, Object data, String name, List<Rule> rulez, Map wrongz, Map params)
    throws HongsException {
        Collection data2 = new ArrayList();

        // 将后面的规则应用于每一个值
        if (data instanceof Collection) {
            int i3 = 0;
            for(Object data3 : ( Collection ) data) {
                String name3 = name + "." + (i3 ++);
                data3 = verify(values, update, data3, name3, rulez, wrongz);
                if (data3 !=  SKIP ) {
                    data2.add(data3);
                }
            }
        } else if (data instanceof Map) {
            for(Object i3 : ((Map ) data).entrySet()) {
                Map.Entry e3 = (Map.Entry) i3;
                Object data3 = e3.getValue( );
                String name3 = name +"."+ ( (String) e3.getKey( ) );
                data3 = verify(values, update, data3, name3, rulez, wrongz);
                if (data3 !=  SKIP ) {
                    data2.add(data3);
                }
            }
        }

        // 值是否需是不同的
        if (Synt.declare(params.get("distrinct"), false)) {
            data2 = new LinkedHashSet( data2 );
        }

        // 多个值的数量限制
        int n, c = data2.size();
        n = Synt.declare(params.get("minrepeat"), 0);
        if (n != 0 && c < n) {
            failed(wrongz, new Wrong("fore.form.lt.minrepeat", String.valueOf(n), String.valueOf(c)), name);
        }
        n = Synt.declare(params.get("maxrepeat"), 0);
        if (n != 0 && c > n) {
            failed(wrongz, new Wrong("fore.form.gt.maxrepeat", String.valueOf(n), String.valueOf(c)), name);
        }

        return data2;
    }

    public static void failed(Map<String, Wrong> wrongz, Wrong  wrong , String name) {
        wrongz.put(name, wrong);
    }

    public static void failed(Map<String, Wrong> wrongz, Wrongs wrongs, String name) {
        for (Map.Entry<String, Wrong> et : wrongs.getWrongs().entrySet()) {
            String n = et.getKey(   );
            Wrong  e = et.getValue( );
            wrongz.put(name+"."+n, e);
        }
    }

    //** 顶级校验器 **/

    public static Object SKIP = new Object();

    public static abstract class Rule {
        protected Map params = null;
        protected Map values = null;
        protected boolean update;

        public void setParams(Map params) {
            this.params = params;
        }
        public void setValues(Map values) {
            this.values = values;
        }
        public void setUpdate(boolean update) {
            this.update = update;
        }

        public abstract Object verify(Object value) throws Wrong, Wrongs, HongsException;
    }

    public static class Pass extends Rule {
        @Override
        public Object verify(Object value) {
            return value;
        }
    }

    public static class Skip extends Rule {
        @Override
        public Object verify(Object value) {
            return SKIP ;
        }
    }

    public static class Required extends Rule {
        @Override
        public Object verify(Object value) throws Wrong {
            if (value == null) {
                if (update) return SKIP;
                throw new Wrong("fore.form.required");
            }
            if ("".equals(value)) {
                throw new Wrong("fore.form.required");
            }
            if ((value instanceof List) && ((List) value).isEmpty()) {
                throw new Wrong("fore.form.requreid");
            }
            if ((value instanceof Set ) && ((Set ) value).isEmpty()) {
                throw new Wrong("fore.form.requreid");
            }
            if ((value instanceof Map ) && ((Map ) value).isEmpty()) {
                throw new Wrong("fore.form.requreid");
            }
            return value;
        }
    }

    public static class Optional extends Rule {
        @Override
        public Object verify(Object value) throws Wrong {
            try {
                Required rule = new Required();
                rule.setParams(params);
                rule.setValues(values);
                rule.verify(value);
            }   catch (Wrong w) {
                return SKIP ;
            }
            return value;
        }
    }

    public static class Repeated extends Rule {
        @Override
        public Object verify(Object value) throws Wrong {
            if (value instanceof Object[ ] ) {
                return Arrays.asList((Object[]) value);
            }
            if (value instanceof Collection) {
                return value;
            }
            if (value instanceof Map) {
                return value;
            }
            throw new Wrong("fore.form.repeated");
        }
    }

    public static class Norepeat extends Rule {
        @Override
        public Object verify(Object value) throws Wrong {
            try {
                Repeated rule = new Repeated();
                rule.setParams(params);
                rule.setValues(values);
                rule.verify(value);
            }   catch (Wrong w) {
                return value;
            }
            throw new Wrong("fore.form.norepeat");
        }
    }

    public static class Default extends Rule {
        @Override
        public Object verify(Object value) {
            if (value==null|| Synt.declare(params.get("alwayset"), false)) { // 无论有没有都设置
                if (update && Synt.declare(params.get("increate"), false)) { // 仅创建的时候设置
                    return SKIP;
                }
                value = params.get("default");
                if ( "$now".equals( value ) ) {
                    value = new java.util.Date();
                }
            }
            return value;
        }
    }

    //** 类型校验器 **/

    public static class IsString extends Rule {
        @Override
        public Object verify(Object value) {
            return /*value == null ? "" :*/ value.toString();
        }
    }

    public static class IsNumber extends Rule {
        @Override
        public Object verify(Object value) throws Wrong {
            // 类型转换
            String type = Synt.declare(params.get("type"), "");
            Number  num;
            try {
                if ( "byte".equals(type)) {
                    num = Synt.declare(value, ( byte) 0);
                } else
                if ("short".equals(type)) {
                    num = Synt.declare(value, (short) 0);
                } else
                if (  "int".equals(type)) {
                    num = Synt.declare(value, 0 );
                } else
                if ( "long".equals(type)) {
                    num = Synt.declare(value, 0L);
                } else
                if ("float".equals(type)) {
                    num = Synt.declare(value, 0.0 );
                } else {
                    type= "double";
                    num = Synt.declare(value, 0.0D);
                }
            } catch (HongsError er) {
                throw new Wrong("fore.form.conv.to."+type+".failed");
            }

            // 最大最小值
            Double m;
            m = Synt.declare(params.get("min"), Double.class);
            if (m != null && m > num.doubleValue()) {
                throw new Wrong("fore.form.lt.min", Double.toString(m));
            }
            m = Synt.declare(params.get("max"), Double.class);
            if (m != null && m < num.doubleValue()) {
                throw new Wrong("fore.form.lt.max", Double.toString(m));
            }

            return num;
        }
    }

    public static class IsDate extends Rule {
        @Override
        public Object verify(Object value) {
            return value;
        }
    }

    public static class IsFile extends Rule {
        @Override
        public Object verify(Object value) throws Wrong {
            if (value == null || "".equals(value)) {
                return   null; // 允许为空
            }

            String name = Synt.declare(params.get("name"), String.class);
            if (name == null || "".equals(name)) {
                name = Synt.declare(params.get("__name__"), "");
            }

            UploadHelper u = new UploadHelper();
            u.setUploadName(name);
            String x;
            x = (String) params.get( "href" );
            if (x != null) u.setUploadHref(x);
            x = (String) params.get( "path" );
            if (x != null) u.setUploadPath(x);
            x = (String) params.get( "name" );
            if (x != null) u.setUploadDate(x);
            x = (String) params.get( "type" );
            if (x != null) u.setAllowTypes(x.split(","));
            x = (String) params.get( "extn" );
            if (x != null) u.setAllowExtns(x.split(","));

            u.upload(value.toString());
            return  u.getResultHref( );
        }
    }

    public static class IsEnum extends Rule {
        @Override
        public Object verify(Object value) throws Wrong, HongsException {
            if (value == null || "".equals(value)) {
                return   null; // 允许为空
            }

            String conf = Synt.declare(params.get("conf"), String.class);
            String name = Synt.declare(params.get("enum"), String.class);
            if (conf == null || "".equals(conf)) {
                conf = Synt.declare(params.get("__conf__"), "");
            }
            if (name == null || "".equals(name)) {
                name = Synt.declare(params.get("__name__"), "");
            }

            Map data = FormSet.getInstance(conf).getEnum(name);
            if (! data.containsKey( value.toString() ) ) {
                throw new Wrong("fore.form.not.in.enum");
            }
            return  value;
        }
    }

    public static class IsForm extends Rule {
        @Override
        public Object verify(Object value) throws Wrongs, HongsException {
            if (value == null) {
                return   null; // 允许为空
            }

            boolean  ud = Synt.declare(params.get("__update__") , false);
            String conf = Synt.declare(params.get("conf"), String.class);
            String name = Synt.declare(params.get("form"), String.class);
            if (conf == null || "".equals(conf)) {
                conf = Synt.declare(params.get("__conf__"), "");
            }
            if (name == null || "".equals(name)) {
                name = Synt.declare(params.get("__name__"), "");
            }

            VerifyHelper hlpr = new VerifyHelper();
            hlpr.addRulesByForm(conf  , name  );
            return  hlpr.verify(values, update);
        }
    }

    /** 内部错误类 **/

    public static class Wrong extends HongsException {
        public Wrong(Throwable cause, String desc, String... prms) {
            super(HongsException.NOTICE, desc, cause);
            this.setLocalizedSection("default");
            this.setLocalizedOptions(prms);
        }

        public Wrong(String desc, String... prms) {
            super(HongsException.NOTICE, desc );
            this.setLocalizedSection("default");
            this.setLocalizedOptions(prms);
        }

        @Override
        public String getLocalizedMessage() {
            CoreLocale trns = CoreLocale.getInstance(  getLocalizedSection());
            String/**/ desx = trns.translate(getDesc(),getLocalizedOptions());
            return desx;
        }
    }

    public static class Wrongs extends HongsException {
        private final Map<String, Wrong> wrongs;

        public Wrongs(Map<String, Wrong> wrongs) {
            super(HongsException.NOTICE, "fore.form.invalid");
            this.setLocalizedSection("default");
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

        public Map<String, Object> getErrmap() throws HongsException {
            Map<String, Object> errors = new LinkedHashMap();
            for (Map.Entry et : wrongs.entrySet()) {
                Wrong  w = (Wrong )  et.getValue();
                String n = (String)  et.getKey ( );
                String e = w.getLocalizedMessage();
                Dict.setParam(errors, e, n);
            }
            return errors;
        }

        @Override
        public String getLocalizedMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.getLocalizedMessage()).append("\r\n");
            try {
                for (Map.Entry<String, String> et : getErrors().entrySet()) {
                    sb.append( "\t" )
                      .append(et.getKey(  ))
                      .append( ": " )
                      .append(et.getValue())
                      .append("\r\n");
                }
            } catch (HongsException ex) {
                throw new HongsError(HongsError.COMMON, ex);
            }
            return sb.toString().trim();
        }
    }

}
