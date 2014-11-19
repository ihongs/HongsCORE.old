package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.FetchCase;
import app.hongs.util.Util;
import app.hongs.util.Tree;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * 校验器
 * @author Hongs
 *
 * <h3>异常代码</h3>
 * <pre>
 * 代码区间 0x1110~0x111f
 * 0x1111 规则格式错误
 * 0x1113 找不到该规则
 * 0x1115 找不到规则的类
 * 0x1117 找不到规则的方法
 * 0x1119 参数与要求的格式不匹配
 * </pre>
 */
public class VerifyHelper {

    private CoreLanguage lang; // 错误语言
    private List<Rule> rules1; // 无适配符的规则
    private List<Rule> rules2; // 带适配符的规则

    public VerifyHelper(CoreLanguage lang) {
        this.lang = lang;
        this.rules1 = new ArrayList();
        this.rules2 = new ArrayList();
    }

    public VerifyHelper(String lang) {
        this(CoreLanguage.getInstance(lang));
    }

    public VerifyHelper addRule(String name, String rule, Object... args) {
        if (name.indexOf('*') == -1) {
            this.rules1.add(new Rule(name, rule, Arrays.asList(args)));
        } else {
            this.rules2.add(new Rule(name, rule, Arrays.asList(args)));
        }
        return this;
    }

    public VerifyHelper setRule(String conf, String form) throws HongsException {
        CollConfig cnf = CollConfig.getInstance(conf);

        int i = 0;
        try {
            List<List> lst = (List<List>) cnf.getDataByKey(form);
            for (List  sub :  lst ) {
                i ++;
                String name = sub.get(0).toString();
                String rule = sub.get(0).toString();
                sub.remove(1);  sub.remove(0);
                Object[] args = sub.toArray();
                this.addRule(name,rule, args);
            }
        }
        catch (ClassCastException ex) {
            throw new HongsException(0x1000, "Failed to get rule: "+conf+":"+form);
        }
        catch (IndexOutOfBoundsException ex) {
            throw new HongsException(0x1000, "Failed to get rule: "+conf+":"+form+"#"+i);
        }

        return this;
    }

    /**
     * 针对 requestData 的校验
     * @param map
     * @return
     * @throws HongsException
     */
    public Map<String, List<String>> verify4RD(Map<String, Object> map) throws HongsException {
        Map<String, List<String>> values = new LinkedHashMap();
        Each each = new Each(values);
        Tree.each(map, each);
        return verify(values);
    }

    /**
     * 针对 parameterMap 的校验
     * @param map
     * @return
     * @throws HongsException
     */
    public Map<String, List<String>> verify4PM(Map<String, String[]> map) throws HongsException {
        Map <String, List<String>> values = new LinkedHashMap();
        for (Map.Entry et : map.entrySet()) {
            String   key  = (String  )et.getKey(  );
            String[] vals = (String[])et.getValue();
            List<String> valz = Arrays.asList(vals);
            values.put(key, valz);
        }
        return verify(values);
    }

    /**
     * 验证
     * @param map
     * @return
     * @throws HongsException
     */
    public Map<String, List<String>> verify(Map<String, List<String>> map) throws HongsException {
        Map <String, List<String>> errors = new LinkedHashMap();
        Map <String, List< Rule >> rules3 = new LinkedHashMap();
        String error;
        Item item;

        for (Rule rule : rules1) {
            addValue(rule.name, rule, rules3);
        }

        for (Rule rule : rules2) {
            List<String> names = getNames(rule.name, map);
            for (String  name  : names) {
                addValue(name , rule, rules3);
            }
        }

        for (Map.Entry et : rules3.entrySet()) {
            String name = (String)et.getKey();
            List<Rule> rules = (List)et.getValue();
            List<String> valuez = map.get(name);
            for (Rule rule : rules) {
                if (valuez==null || valuez.isEmpty()) {
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
                    item  = new Item(name , value, map);
                    error = verify  (item , rule);
                    if (error != null) {
                        addValue(rule.name, error, errors);
                    }
                }
            }
        }

        return errors;
    }

    private String verify(Item item, Rule rule) throws HongsException {
        if ("required".equals(rule.rule)) {
            return required(item.value);
        }
        else if ("isNumber".equals(rule.rule)) {
            return isNumber(item.value);
        }
        else if ("isEmail".equals(rule.rule)) {
            return isEmail(item.value);
        }
        else if ("isURL".equals(rule.rule)) {
            return isURL(item.value);
        }
        else if ("isDate".equals(rule.rule)) {
            return isDate(item.value);
        }
        else if ("isTime".equals(rule.rule)) {
            return isTime(item.value);
        }
        else if ("isDatetime".equals(rule.rule)) {
            return isDatetime(item.value);
        }
        else if ("min".equals(rule.rule)) {
            return min(item.value, getParam(rule, 0, 1.0));
        }
        else if ("max".equals(rule.rule)) {
            return max(item.value, getParam(rule, 0, 1.0));
        }
        else if ("minLength".equals(rule.rule)) {
            return minLength(item.value, getParam(rule, 0, 1));
        }
        else if ("maxLength".equals(rule.rule)) {
            return maxLength(item.value, getParam(rule, 0, 1));
        }
        else if ("isMatch".equals(rule.rule)) {
            return isMatch(item.value, getParam(rule, 0, ""), getParam(rule, 1, ""));
        }
        else if ("isRepeat".equals(rule.rule)) {
            return isRepeat(item.value, item.values, getParam(rule, 0, ""));
        }
        else if ("isUnique".equals(rule.rule)) {
            List<String> fields = new ArrayList();
            fields.addAll(rule.params);
            fields.remove(0);
            return isUnique(item.value, item.values, getParam(rule, 0, ""),
                item.name, fields.toArray(new String[]{}));
        }
        else {
            // 调用 rule 指定的静态方法进行校验
            int pos = rule.rule.lastIndexOf(".");
            if (pos < 0) {
                throw new HongsException(0x1113, "Unknown rule '"+rule.rule+"'");
            }
            String cls = rule.rule.substring(0,pos);
            String mtd = rule.rule.substring(1+pos);

            try {
                Class  kls = Class.forName(cls);
                Method wtd = kls.getMethod(mtd ,
                       new Class[] { Item.class, Rule.class, CoreLanguage.class });
                return (String) wtd.invoke(item, rule);
            }
            catch (ClassNotFoundException ex) {
                throw new HongsException(0x1115, "Class '"+cls+"' for '"+rule.name+"' is not exists");
            }
            catch (NoSuchMethodException ex) {
                throw new HongsException(0x1117, "Method '"+rule.rule+"' for '"+rule.name+"' is not exists");
            }
            catch (SecurityException ex) {
                throw new HongsException(0x1117, ex);
            }
            catch (IllegalAccessException ex) {
                throw new HongsException(0x1117, ex);
            }
            catch (IllegalArgumentException ex) {
                throw new HongsException(0x1117, ex);
            }
            catch (InvocationTargetException ex) {
                throw new HongsException(0x1117, ex.getCause());
            }
        }
    }

    public static <T>T getParam(Rule rule, int idx, T def) throws HongsException {
        Object val = rule.params.get(idx);
        if (val == null) {
            return def;
        }
        try {
            return (T) val;
        }
        catch (ClassCastException ex) {
            throw new HongsException(0x1119,
                "Wrong type for "+rule.name+":"+rule.rule+"["+idx+"]", ex);
        }
    }

    public static String getValue(Item item, String key, String def) {
        List<String> values = item.values.get(key);
        if (values == null || values.isEmpty()) {
            return def;
        }
        else {
            return values.get(0);
        }
    }

    private static List<String> getNames(String name, Map<String, List<String>> values) {
        name = "^"+Util.escapeRegular(name).replace("\\u002a", "[^\\.]+")+"$";
        Pattern pa = Pattern.compile(name);
        List<String> names = new ArrayList();
        for (String  namc  : values.keySet()) {
            if (pa.matcher( namc ).matches()) {
                names.add ( namc );
            }
        }
        return names;
    }

    private static void addValue(String name, Object value, Map values) {
        List valuez = (List)values.get(name);
        if (valuez == null) {
            valuez = new ArrayList();
            values.put(name, valuez);
        }
        valuez.add(value);
    }

    private String requires(Object value) {
        if (value == null) {
            return lang.translate("fore.form.requires");
        }
        return null;
    }

    private String required(String value) {
        if (value == null || "".equals(value)) {
            return lang.translate("fore.form.required");
        }
        return null;
    }

    private String isNumber(String value) {
        return null;
    }

    private String isEmail(String value) {
        return null;
    }

    private String isURL(String value) {
        return null;
    }

    private String isDate(String value)  {
        return null;
    }

    private String isTime(String value) {
        return null;
    }

    private String isDatetime(String value) {
        return null;
    }

    private String minLength(String value, int num) {
        return null;
    }

    private String maxLength(String value, int num) {
        return null;
    }

    private String min(String value, double num) {
        if (num > Double.parseDouble(value)) {
            return lang.translate("fore.form.lt.min");
        }
        return null;
    }

    private String max(String value, double num) {
        if (num < Double.parseDouble(value)) {
            return lang.translate("fore.form.gt.max");
        }
        return null;
    }

    private String isMatch(String value, String regex, String error) {
        if (value != null && ! value.matches(regex)) {
            return lang.translate(error);
        }
        return null;
    }

    private String isRepeat(String value, Map<String, List<String>> values, String name2) {
        List<String> valuez = values.get(name2);
        String value2 = valuez != null && !valuez.isEmpty() ? valuez.get(0) : "";
        if ( ! value2.equals(value) ) {
            return lang.translate("fore.form.is.not.repeat" );
        }
        return null;
    }

    private String isUnique(String value, Map<String, List<String>> values, String model,
            String field, String... fields) throws HongsException {
        AbstractBaseModel mode = (AbstractBaseModel) Core.getInstance(model);
        FetchCase caze = new FetchCase();
        caze.where(".`"+field+"` = ?", value);

        List v = values.get(mode.table.primaryKey);
        if (v != null) {
            if ( v.size() > 1 ) {
                caze.where(".`"+mode.table.primaryKey+"` NOT IN (?)", v);
            }
            else {
                caze.where(".`"+mode.table.primaryKey+"` != ?", v);
            }
        }

        for (String f : fields) {
            v = values.get(f);
            if (v == null) {
                continue;
            }
            if ( v.size() > 1 ) {
                caze.where(".`"+f+"` IN (?)", v);
            }
            else {
                caze.where(".`"+f+"` = ?", v);
            }
        }

        Map row = mode.table.fetchLess(caze);
        if (! row.isEmpty()) {
            return lang.translate("fore.form.is.not.unique");
        }

        return null;
    }

    /** 内部类 **/

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

    private static class Each implements Tree.EachValue {
        Map<String, List<String>> values;
        public Each(Map<String, List<String>> values) {
            this.values = values;
        }
        @Override
        public void each(Object value, String path) {
            VerifyHelper.addValue(path, value.toString(), values);
        }
    }

}
