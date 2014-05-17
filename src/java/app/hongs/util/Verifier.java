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
 * </pre>
 */
public class Verifier {
    private static class Rule {
        String name;
        String rule;
        List   args;
        public Rule(String name, String rule, List args) {
            this.name = name;
            this.rule = rule;
            this.args = args;
        }
    }
    
    private static class Item {
        String name;
        List<String> values;
        public Item(String name, List<String> values) {
            this.name = name;
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
        String argz;
        List   args;
        this.rules1 = new ArrayList();
        this.rules2 = new ArrayList();
        for (String rule : rules) {
            mat = pat.matcher(rule);
            if (! mat.matches()) {
                throw new HongsException(0x1201, "Rule format error, rule: "+rule);
            }
            
            name = mat.group(1);
            rule = mat.group(2);
            argz = mat.group(4);
            args = argz == null ? new ArrayList() : (List)JSON.parse("["+argz+"]");
            
            if (name.indexOf('*') == -1) {
                this.rules1.add(new Rule(name, rule, args));
            }
            else {
                this.rules2.add(new Rule(name, rule, args));
            }
        }
    }
    
    protected void addError(String name, String error, Map<String, List<String>> errors) {
        List<String> errorz = errors.get(name);
        if (errorz == null ) {
            errorz = new ArrayList();
            errors.put(name, errorz);
        }
        errorz.add(error);
    }
    
    protected Map<String, List<String>> getValues(Map<String, Object> data) {
        Map<String, List<String>> values = new LinkedHashMap();
        Tree.walk4req(new Each4Req() {
            public void eachItem(String name, String value) {
                addError(name, value, values);
            }
        }, data);
        return values;
    }
    
    protected List<Item> getValues(String name, Map<String, List<String>> values) {
        name = "^"+Str.escapeRegular(name).replace("\\u002a", "[^\\.]+")+"$";
        Pattern pa = Pattern.compile(name);
        List<Item> items = new ArrayList();
        List<String> valuez;
        for (Map.Entry et : values.emptySet()) {
            name   = (String) et.getKey();
            valuez = (List<String>) et.getValue();
            if (pa.matcher(mn).matches()) {
                items.add(new Item(name, valuez));
            }
        }
        return items;
    }
    
    public Map<String, List<String>> verify(Map<String, Object> data) throws HongsException {
        Map<String, List<String>> errors = new LinkedHashMap();
        Mpa<Stirng, List<String>> values = getValues(data);
        List<String> valuez;
        List<Item> items;
        String error;
        
        for (Rule rule : rules1) {
            valuez = values.get(rule.name);
            
            if (valuez == null) {
                if (rule.rule.equals("required")) {
                    error = required(null);
                    addError(rule.name, error, errors);
                }
                else
                if (rule.rule.equals("requires")) {
                    error = requires(null);
                    addError(rule.name, error, errors);
                }
            }
            else
            for (String value : valuez) {
                error = verify(rule, value, values);
                if (error != null) {
                    addError(rule.name, error, errors);
                }
            }
        }
        
        // 带通配符的规则
        for (Rule rule : rules2) {
            items = getValues(rule.name, values);
            
            if (items.isEmtpy()) {
                if (rule.rule.equals("required")) {
                    error = required(null);
                    addError(rule.name, error, errors);
                }
                else
                if (rule.rule.equals("requires")) {
                    error = requires(null);
                    addError(rule.name, error, errors);
                }
            }
            else
            for (Item item : items) {
                error = verify(rule,item.value,values);
                if (error != null) {
                    addError(item.name, error, errors);
                }
            }
        }
        
        return errors;
    }
    
    protected String verify(Rule rule, String value, Map<String, List<String>> values) throws HongsException {
        switch (rule.rule) {
            case "required":
                return required(value);
            case "isNumber":
                return isNumber(value);
            case "isEmail":
                return isEmail(value);
            case "isURL":
                return isURL(value);
            case "isDate":
                return isDate(value);
            case "isTime":
                return isTime(value);
            case "isDatetime":
                return isDatetime(value);
            case "min":
                return min(value, (Integer)rule.args.get(0));
            case "max":
                return max(value, (Integer)rule.args.get(0));
            case "minLength":
                return minLength(value, (Integer)rule.args.get(0));
            case "maxLength":
                return maxLength(value, (Integer)rule.args.get(0));
            case "isMatch":
                return isMatch (value, (String)rule.args.get(0), (String)rule.args.get(1));
            case "isRepeat":
                return isRepeat(values, value, (String)rule.args.get(0));
            case "isUnique":
                String model = (String)rule.args.get(0);
                String field = rule.name;
                List<String> fields = new ArrayList ( );
                fields.addAll(rule.args);
                fields.remove(0);
                return isUnique(values, value, model,field,fields.toArray(new String[]{}));
            default:
                // 调用 rule 指定的静态方法进行校验
                int pos = rule.rule.lastIndexOf(".");
                String cls = rule.rule.substring(0,pos);
                String mtd = rule.rule.substring(1+pos);
                try {
                    Class  klass  = Class.forName  (cls);
                    Method method = klass.getMethod(mtd, new Class[]
                        {Map.class, String.class, String.class, Object[].class});
                    return (String)method.invoke(mtd, values, value, rule.name, rule.args.toArray());
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
        if (value == null) {
            return lang.translate("js.form.required");
        }
        return null;
    }
    
    protected String isNumber(String value) {
        return null;
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
    
    protected String min(String value, int num) {
        return null;
    }
    
    protected String max(String value, int num) {
        return null;
    }
    
    protected String isMatch(String value, String regex, String error) {
        if (value != null && ! value.matches(regex)) {
            return lang.translate(error);
        }
        return null;
    }
    
    protected String isRepeat(Map<String, List<String>> values, String value, String name2) {
        List<String> valuez = values.get(name2);
        String value2 = valuez != null && !valuez.isEmpty() ? valuez.get(0) : '';
        if ( ! value2.equals(value) ) {
            return lang.translate("js.form.is.not.repeat" );
        }
        return null;
    }
    
    protected String isUnique(Map<String, List<String>> values, String value, String field,
    String model, String... fields) throws HongsException {
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
