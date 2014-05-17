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
    public static class Rule {
        String name;
        String rule;
        List   args;
        public Rule(String name, String rule, List args) {
            this.name = name;
            this.rule = rule;
            this.args = args;
        }
    }
    
    private CoreLanguage        lang;
    private Map<String, Object> data;
    private List<Rule>          rules;
    
    public void setLang(CoreLanguage lang) {
        this.lang = lang;
    }
    
    public void setData(Map<String,Object> data) {
        this.data = data;
    }
    
    public void setRules(String... rules) throws HongsException {
        Pattern pat = Pattern.compile("^([^=]+)=([^\\(\\)]+)(\\((.*)\\))?$");
        Matcher mat;
        String name;
        String argz;
        List   args;
        this.rules = new ArrayList();
        for (String rule : rules) {
            mat = pat.matcher(rule);
            if (! mat.matches()) {
                throw new HongsException(0x1201, "Rule format error, rule: "+rule);
            }
            
            name = mat.group(1);
            rule = mat.group(2);
            argz = mat.group(4);
            args = argz == null ? new ArrayList() : (List)JSON.parse("["+argz+"]");
            
            this.rules.add(new Rule(name, rule, args));
        }
    }
    
    public Map<String, List<String>> verify() throws HongsException {
        Map<String, List<String>> errors = new LinkedHashMap();
        List   errorz;
        String error ;
        for (Rule rule : rules) {
                error = verify(rule);
                if (error  == null ) {
                    continue;
                }
                errorz = errors.get(rule.name);
                if (errorz == null ) {
                    errorz  = new ArrayList( );
                    errors.put(rule.name, errorz);
                }
                errorz.add(error);
        }
        return errors;
    }
    
    public String verify(Rule rule) throws HongsException {
        Object value = data.get(rule.name);
        if (!(value instanceof Collection)) {
            verify(rule, value.toString( ));
            return null;
        }
        
        Collection  valua = (Collection) value;
        
        if (value == null|| valua.isEmpty()
        &&  rule.rule.equals( "required" )) {
            requires(null);
            return null;
        }
        
        for(Object valub : valua) {
            String error = verify(rule, valub.toString());
            if ( error  !=  null) {
                return error;
            }
        }
        
        return null;
    }
    
    public String verify(Rule rule, String value) throws HongsException {
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
                return isMatch(value, (String)rule.args.get(0), (String)rule.args.get(1));
            case "isRepeat":
                return isRepeat(value, (String)rule.args.get(0));
            case "isUnique":
                String model = (String)rule.args.get(0);
                String field = rule.name;
                List<String> fields = new ArrayList ( );
                fields.addAll(rule.args);
                fields.remove(0);
                return isUnique(value, model, field, fields.toArray(new String[]{}));
            default:
                // 调用 rule 指定的静态方法进行校验
                int pos = rule.rule.lastIndexOf(".");
                String cls = rule.rule.substring(0,pos);
                String mtd = rule.rule.substring(1+pos);
                try {
                    Class  klass  = Class.forName  (cls);
                    Method method = klass.getMethod(mtd, new Class[]
                        {String.class, String.class, Map.class, Object[].class});
                    return (String)method.invoke(mtd, value, rule.name, data, rule.args.toArray());
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
    
    public String requires(Object value) {
        if (value == null) {
            return lang.translate("js.form.requires");
        }
        return null;
    }
    
    public String required(String value) {
        if (value == null) {
            return lang.translate("js.form.required");
        }
        return null;
    }
    
    public String isNumber(String value) {
        return null;
    }
    
    public String isEmail(String value) {
        return null;
    }
    
    public String isURL(String value) {
        return null;
    }
    
    public String isDate(String value)  {
        return null;
    }
    
    public String isTime(String value) {
        return null;
    }
    
    public String isDatetime(String value) {
        return null;
    }
    
    public String minLength(String value, int num) {
        return null;
    }
    
    public String maxLength(String value, int num) {
        return null;
    }
    
    public String min(String value, int num) {
        return null;
    }
    
    public String max(String value, int num) {
        return null;
    }
    
    public String isMatch(String value, String regex, String error) {
        if (value != null && ! value.matches(regex)) {
            return lang.translate(error);
        }
        return null;
    }
    
    public String isRepeat(String value, String rname) {
        if (value != null && ! value.equals(data.get(rname))) {
            return lang.translate("js.form.is.not.repeat");
        }
        return null;
    }
    
    public String isUnique(String value, String model, String field, String... fields) throws HongsException {
        AbstractBaseModel mode = (AbstractBaseModel) Core.getInstance(model);
        FetchMore more = new FetchMore();
        more.where(".`"+field+"` = ?", value);

        Object v = data.get(mode.table.primaryKey);
        if (v !=  null) {
            if (v instanceof Collection) {
                more.where(".`"+mode.table.primaryKey+"` NOT IN (?)", v);
            }
            else {
                more.where(".`"+mode.table.primaryKey+"` != ?", v);
            }
        }
        
        for (String f : fields) {
            v = data.get(f);
            if (v ==  null) {
                continue;
            }
            if (v instanceof Collection) {
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
