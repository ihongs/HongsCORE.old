package app.hongs.util;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.FetchMore;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证器
 * @author Hongs
 * 
 * <h3>异常代码</h3>
 * <pre>
 * 代码区间 0x1110~0x111f
 * 0x1111 规则格式错误
 * 0x1113 找不到规则的类
 * 0x1115 找不到规则的方法
 * 0x1117 参数与要求的格式不匹配
 * </pre>
 */
public class Verifier {
    private static class Rule {
        String name;
        String rule;
        List params;
        public Rule(String name, String rule, List params) {
            this.name = name;
            this.rule = rule;
            this.params = params;
        }
    }
    
    private static class Item {
        String name;
        String value;
        Map<String, List<String>> values;
        public Item(String name, String value, Map<String, List<String>> values) {
            this.name = name;
            this.value = value;
            this.values = values;
        }
    }
    
    private CoreLanguage lang;
    private List<Rule> rules1;
    private List<Rule> rules2;
    
    public void setLang(CoreLanguage lang) {
        this.lang = lang;
    }
    
    public void setRules(String... rules) throws HongsException {
        Pattern pat = Pattern.compile("^([^:]+):([^\\(\\)]+)(\\((.*)\\))?$");
        Matcher mat;
        String name;
        String args;
        List params;
        this.rules1 = new ArrayList();
        this.rules2 = new ArrayList();
        for (String rule : rules) {
            mat = pat.matcher(rule);
            if (! mat.matches()) {
                throw new HongsException(0x1111, "Rule format error, rule: "+rule);
            }
            
            name = mat.group(1);
            rule = mat.group(2);
            args = mat.group(4);
            params = args==null?new ArrayList() : (List)JSON.parse("["+params+"]");
            
            if (name.indexOf('*') == -1) {
                this.rules1.add(new Rule(name, rule, params));
            }
            else {
                this.rules2.add(new Rule(name, rule, params));
            }
        }
    }
    
    protected void addValue(String name, Object value, Map<String, List> values) {
        List valuez = values.get(name);
        if (valuez == null) {
            valuez = new ArrayList();
            values.put(name, valuez);
        }
        valuez.add(value);
    }
    
    protected List<String> getNames(String name, Map<String, List> values) {
        name = "^"+Str.escapeRegular(name).replace("\\u002a", "[^\\.]+")+"$";
        Pattern pa = Pattern.compile(name);
        List<String> names = new ArrayList();
        for (String  name  : values.keySet()) {
            if (pa.matcher( name ).matches()) {
                names.add ( name );
            }
        }
        return names;
    }
    
    public static String getValue(Item item, String name) {
        List<String> values = item.values.get(name);
        if (values != null && !values.isEmpty() {
            return values.get(0);
        }
        else {
            return null;
        }
    }
    
    public static T getParam(Rule rule, int idx, T def) {
        Object val = rule.params.get(idx);
        if (val == null) {
            return def;
        }
        if (val instanceof T) {
            return (T) val;
        }
        throw new HongsException(0x1117,
            "Arg type for "+rule.name+":"+rule.rule
            +"["+idx+"] is not '"+T.class.getName()+"'");
    }
    
    public Map<String, List<String>> verify(Map data) throws HongsException {
        Map<String, List<String>> values = new LinkedHashMap();
        Tree.walk4req(new Each4Req() {
            public void eachItem(String name, String value) {
                addValue(name, value, values);
            }
        }, data);
        
        return verify(values);
    }
    
    public Map<String, List<String>> verify(Map<String, List<String>> values) throws HongsException {
        Map <String, List<String>> errors = new LinkedHashMap();
        Map <String, List< Rule >> rules3 = new LinkedHashMap();
        String error;
        
        for (Rule rule : rules1) {
            addValue(rule.name, rule, rules3);
        }
        
        for (Rule rule : rules2) {
            List<String> names = getNames(rule.name, values);
            for (String  name  : names) {
                addValue(name , rule, rules3);
            }
        }
        
        for (Map.Entry et : rules3.entrySet()) {
            String name = et.getKey();
            List  rules = et.getValue();
            List<String> valuez = values.get(name);
            for (Rule rule : rules) {
                if (valuez==null || valuez.isEmtpy()) {
                    if (rule.rule.equals("required")) {
                        error = required(null);
                        addValue(rule.name, error, errors);
                    }
                    else
                    if (rule.rule.equals("requires")) {
                        error = requires(null);
                        addValue(rule.name, error, errors);
                    }
                }
                else
                for (String value : valuez) {
                    item  = new Item(name, value, values);
                    error = verify  (item, rule);
                    if (error != null) {
                        addValue(rule.name, error, errors);
                    }
                }
            }
        }
        
        return errors;
    }
    
    protected String verify(Item item, Rule rule) throws HongsException {
        switch (rule.rule) {
            case "required":
                return required(item.value);
            case "isNumber":
                return isNumber(item.value);
            case "isEmail":
                return isEmail(item.value);
            case "isURL":
                return isURL(item.value);
            case "isDate":
                return isDate(item.value);
            case "isTime":
                return isTime(item.value);
            case "isDatetime":
                return isDatetime(item.value);
            case "min":
                return min(item.value, getParam(rule, 0, 1.0));
            case "max":
                return max(item.value, getParam(rule, 0, 1.0));
            case "minLength":
                return minLength(item.value, getParam(rule, 0, 1));
            case "maxLength":
                return maxLength(item.value, getParam(rule, 0, 1));
            case "isMatch":
                return isMatch(item.value, getParam(rule, 0, ""), getParam(rule, 1, ""));
            case "isRepeat":
                return isRepeat(item.value, item.values, getParam(rule, 0, ""));
            case "isUnique":
                List<String> fields = new ArrayList();
                fields.addAll(rule.params);
                fields.remove(0);
                return isUnique(item.value, item.values, getParam(rule, 0, ""),
                        item.name, fields.toArray(new String[]{}));
            default:
                // 调用 rule 指定的静态方法进行校验
                int pos = rule.rule.lastIndexOf(".");
                String cls = rule.rule.substring(0,pos);
                String mtd = rule.rule.substring(1+pos);
                try {
                    Class  klass  = Class.forName  (cls);
                    Method method = klass.getMethod(mtd, new Class[]{ Item.class, Rule.class });
                    return (String) method.invoke(mtd, item, rule);
                }
                catch (ClassNotFoundException ex) {
                    throw new HongsException(0x1113, "Class '"+cls+"' for '"+rule.name+"' is not exists");
                }
                catch (NoSuchMethodException ex) {
                    throw new HongsException(0x1115, "Method '"+rule.rule+"' for '"+rule.name+"' is not exists");
                }
                catch (SecurityException ex) {
                    throw new HongsException(0x1115, ex);
                }
                catch (IllegalAccessException ex) {
                    throw new HongsException(0x1115, ex);
                }
                catch (IllegalArgumentException ex) {
                    throw new HongsException(0x1115, ex);
                }
                catch (InvocationTargetException ex) {
                    throw new HongsException(0x1115, ex);
                }
        }
    }
    
    protected String requires(Object value) {
        if (value == null) {
            return lang.translate("js.form.requires");
        }
        return null;
    }
    
    protected String required(String value) {
        if (value == null||"".equals(value)) {
            return lang.translate("js.form.required");
        }
        return null;
    }
    
    protected String isNumber(String value) {
        return value.matches("^\d+$");
    }
    
    protected String isEmail(String value) {
        return null;
    }
    
    protected String isURL(String value) {
        return null;
    }
    
    protected String isDate(String value)  {
        return null;
    }
    
    protected String isTime(String value) {
        return null;
    }
    
    protected String isDatetime(String value) {
        return null;
    }
    
    protected String minLength(String value, int num) {
        return null;
    }
    
    protected String maxLength(String value, int num) {
        return null;
    }
    
    protected String min(String value, double num) {
        if (num > Double.parseDouble(value)) {
            return lang.translate("js.form.lt.min");
        }
        return null;
    }
    
    protected String max(String value, double num) {
        if (num < Double.parseDouble(value)) {
            return lang.translate("js.form.gt.max");
        }
        return null;
    }
    
    protected String isMatch(String value, String regex, String error) {
        if (value != null && ! value.matches(regex)) {
            return lang.translate(error);
        }
        return null;
    }
    
    protected String isRepeat(String value, Map<String, List<String>> values, String name2) {
        List<String> valuez = values.get(name2);
        String value2 = valuez != null && !valuez.isEmpty() ? valuez.get(0) : "";
        if ( ! value2.equals(value) ) {
            return lang.translate("js.form.is.not.repeat" );
        }
        return null;
    }
    
    protected String isUnique(String value, Map<String, List<String>> values, String model,
            String field, String... fields) throws HongsException {
        AbstractBaseModel mode = (AbstractBaseModel) Core.getInstance(model);
        FetchMore more = new FetchMore();
        more.where(".`"+field+"` = ?", value);

        List v = values.get(mode.table.primaryKey);
        if (v != null) {
            if ( v.size() > 1 ) {
                more.where(".`"+mode.table.primaryKey+"` NOT IN (?)", v);
            }
            else {
                more.where(".`"+mode.table.primaryKey+"` != ?", v);
            }
        }
        
        for (String f : fields) {
            v = values.get(f);
            if (v == null) {
                continue;
            }
            if ( v.size() > 1 ) {
                more.where(".`"+f+"` IN (?)", v);
            }
            else {
                more.where(".`"+f+"` = ?", v);
            }
        }

        Map row = mode.table.fetchLess(more);
        if (! row.isEmpty()) {
            return lang.translate("js.form.is.not.unique");
        }
        
        return null;
    }
}
