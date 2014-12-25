package app.hongs.action;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.util.Tree;
import app.hongs.util.Util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

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

    private Map<String, Map<String, Map>> rules;

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
        Map<String,Object> opts = new HashMap();
        for(String   arg : args) {
            String[] arr = arg.split( "=" , 2 );
            opts.put(arr[0], arr[1]);
        }
        return addRule(name, rule, opts);
    }

    public VerifyHelper addRulesByForm(String coll, String form) throws HongsException {
        CollConfig cnf = CollConfig.getInstance(coll);

        int i = 0;
        try {
            Map map = cnf.getForm(form);
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry et = (Map.Entry)it.next();
                String  name = (String) et.getKey();
                Map     opts = (Map)  et.getValue();

                String  type = (String) opts.get("_type");
                String  extr = (String) opts.get("_extr");
                String  required = (String) opts.get("_required");
                String  repeated = (String) opts.get("_repeated");

                if (!"0".equals(required) && !"2".equals(required)) {
                    this.addRule(name, "_required");
                }
                if (!"0".equals(repeated)) {
                    this.addRule(name, "_repeated");
                }
                if (! "".equals(extr)) {
                    this.addRule(name, extr, opts);
                } else {
                    this.addRule(name, type, opts);
                }
            }
        }
        catch (ClassCastException ex) {
            throw new HongsException(0x1101, "Failed to get rule: "+coll+":"+form);
        }
        catch (IndexOutOfBoundsException ex) {
            throw new HongsException(0x1101, "Failed to get rule: "+coll+":"+form+"#"+i);
        }

        return this;
    }

    public Map verify(Map values, boolean update) throws Wrongs, HongsException {
        Map<String, Object> valuez = new LinkedHashMap();
        Map<String, Wrong > wrongz = new LinkedHashMap();

        for(Map.Entry<String, Map<String, Map>> et : rules.entrySet()) {
            Map<String, Map> rulez  =  et.getValue( );
            String name = et.getKey();
            Object data = Tree.getValue(values, name);

            Map<String, String> rq = rulez.remove("_required");
            Map<String, String> rp = rulez.remove("_repeated");

            // 注: required 等于 2 表示这是一个只读项
            if ("2".equals(rq)) {
                continue;
            } else
            if ("0".equals(rq) || rq == null || update) {
                try {
                    data = required(data);
                } catch (Wrong w) {
                    continue;
                }
            } else {
                try {
                    data = required(data);
                } catch (Wrong ex) {
                    wrongz.put(name , ex);
                    continue;
                }
            }

            if ("0".equals(rp) || rp == null) {
                try {
                    data = verify(name, data, values, rulez, update);
                } catch (Wrong  w) {
                    wrongz.put(name, w);
                    continue;
                } catch (Wrongs w) {
                    putWrons(wrongz, w.getWrongs(), name);
                    continue;
                }
            } else {
                try {
                    data = repeated(data);
                } catch (Wrong ex) {
                    wrongz.put(name, ex);
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
                            wrongz.put(name3, w);
                            continue;
                        } catch (Wrongs w) {
                            putWrons(wrongz, w.getWrongs(), name3);
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
                            wrongz.put(name3, w);
                            continue;
                        } catch (Wrongs w) {
                            putWrons(wrongz, w.getWrongs(), name3);
                            continue;
                        }
                        data2.add(data3);
                    }
                }
                data = data2;
            }

            Tree.setValue(valuez, name, data);
        }

        if (!wrongz.isEmpty()) {
            throw new Wrongs(wrongz);
        }

        return valuez;
    }

    private Object verify(String name, Object value, Map values, Map<String, Map> rules2, boolean update) throws Wrong, HongsException {
        for(Map.Entry<String, Map> rule2 : rules2.entrySet()) {
            String rule = rule2.getKey(  );
            Map  params = rule2.getValue();
            value = verify(name, value, values, rule, params, update);
        }
        return value;
    }

    private Object verify(String name, Object value, Map values, String rule, Map params, boolean update) throws Wrong, HongsException {
        // 放入环境参数
        params.put("__name__"  , name  );
        params.put("__update__", update);

        // 调用 rule 指定的静态方法进行校验
        String cls;
        String mtd;
        int pos = rule.lastIndexOf( "." );
        if (pos != -1) {
            cls = rule.substring(0 , pos);
            mtd = rule.substring(1 + pos);
        } else {
            cls = this.getClass().getName();
            String n = rule.substring(   1);
            String c = rule.substring(0, 1);
            mtd = "is" +c.toUpperCase()+ n ;
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
            throw new HongsException(0x1106, ex.getCause());
        }
    }

    private static void putWrons(Map<String, Wrong > wrongs, Map<String, Wrong > wrongz, String name) {
        for (Map.Entry<String, Wrong> et : wrongz.entrySet()) {
            String n = et.getKey  ( );
            Wrong  e = et.getValue( );
            wrongs.put(name+"."+n,e);
        }
    }

    private static List<String> getNames(Map<String, Object> values, String name) {
        name = "^"+Util.escapeRegular(name).replace("\\u002a", "[^\\.]+")+"$";
        Pattern pa = Pattern.compile (name);
        List<String> names = new ArrayList();
        for (String  namc  : values.keySet()) {
            if (pa.matcher( namc ).matches()) {
                names.add ( namc );
            }
        }
        return names;
    }

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

    public static String isString(Object value, Map values, Map params) throws Wrong, HongsException {
        norepeat(value);
        String str = value.toString();
        int minlen = Tree.getValue(params, "minlength", 0);
        if (minlen != 0 && minlen > str.length()) {
            throw new Wrong("fore.form.lt.minlength", Integer.toString(minlen));
        }
        int maxlen = Tree.getValue(params, "maxlength", 0);
        if (maxlen != 0 && maxlen < str.length()) {
            throw new Wrong("fore.form.lt.maxlength", Integer.toString(maxlen));
        }
        String pattern = Tree.getValue(params, "pattern", "");
        if (!"".equals(pattern)) {
            if (!Pattern.matches(pattern, str)) {
                throw new Wrong("fore.form.is.not.match.pattern", pattern);
            }
        }
        String defined = Tree.getValue(params, "defined", "");
        if (!"".equals(defined)) {
            pattern = ( String ) CollConfig.getInstance(  ).getEnum("PATTERNS").get(defined);
            if (!Pattern.matches(pattern, str)) {
            defined = CoreLanguage.getInstance().translate("fore.defined.patterns."+defined);
                throw new Wrong("fore.form.is.not.match.defined", defined);
            }
        }
        return str;
    }

    public static Number isNumber(Object value, Map values, Map params) throws Wrong {
        norepeat(value);
        double num;
        try {
            num = Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            throw new Wrong("fore.form.is.not.number");
        }
        double min = Tree.getValue(params, "minlength", 0D);
        if (min != 0 && min > num) {
            throw new Wrong("fore.form.lt.minlength", Double.toString(min));
        }
        double max = Tree.getValue(params, "maxlength", 0D);
        if (max != 0 && max < num) {
            throw new Wrong("fore.form.lt.maxlength", Double.toString(max));
        }
        return num;
    }

    public static Object isForm(Object value, Map values, Map params) throws Wrongs, HongsException {
        String[] formName = ((String) params.get("form")).split("\\.", 2);
        VerifyHelper veri = new VerifyHelper().addRulesByForm(formName[0],formName[1]);
        return veri.verify((Map) value, Tree.getValue( params, "__update__", false ) );
    }

    public static Object isEnum(Object value, Map values, Map params) throws Wrong , HongsException {
        String[] enumName = ((String) params.get("enum")).split("\\.", 2);
        Map      enumData = CollConfig.getInstance( enumName[0] ).getEnum(enumName[1]);
        if (!enumData.containsValue(value.toString())) {
            throw new Wrong("fore.form.not.in.enum");
        }
        return value;
    }

    public static String isFile(Object value, Map values, Map params) throws Wrong {
        String n = Tree.getValue(params, "__name__", "");
        Upload u = new Upload();
        String x;
        x = (String) params.get("uploadPath");
        if (x != null) u.setUploadPath(x);
        x = (String) params.get("uploadHref");
        if (x != null) u.setUploadHref(x);
        x = (String) params.get("uploadName");
        if (x != null) u.setUploadName(x);
        x = (String) params.get("allowTypes");
        if (x != null) u.setAllowTypes(x.split(","));
        x = (String) params.get("allowExtns");
        if (x != null) u.setAllowExtns(x.split(","));

        ActionHelper helper = (ActionHelper) Core.getInstance(ActionHelper.class);
        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator fit = upload.getItemIterator( helper.getRequest( ) );
            while (fit.hasNext()) {
                FileItemStream fis = fit.next(  );
                String fame  = fis.getFieldName();
                if (! n.equals(fame)) {
                    continue;
                }
                String name = fis.getName();
                String type = fis.getContentType( );
                InputStream strm = fis.openStream();
                return u.upload(strm, fame, name, type);
            }
            return null;
        } catch (FileUploadException ex) {
            throw new Wrong(ex, "fore.form.upload.failed");
        } catch (IOException ex) {
            throw new Wrong(ex, "fore.form.upload.failed");
        }
    }

    public static Date isDate(Object value, Map values, Map params) throws Wrong {
        return null;
    }

    public static Time isTime(Object value, Map values, Map params) throws Wrong {
        return null;
    }

    public static Date isDatetime(Object value, Map values, Map params) throws Wrong {
        return null;
    }

    public static String isPassword(Object value, Map values, Map params) throws Wrong {
        return null;
    }

    public static String isExists(Object value, Map values, Map params) throws Wrong {
        return null;
    }

    public static String isUnique(Object value, Map values, Map params) throws Wrong {
        return null;
    }

    /** 内部类 **/

    public static class Wrong  extends HongsException {
        public Wrong(Throwable cause, String desc, String... prms) {
            super(HongsException.NOTICE, desc, cause);
            this.setLocalizedOptions(prms);
        }

        public Wrong(String desc, String... prms) {
            super(HongsException.NOTICE, desc);
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
                Tree.setValue(errors, n, e);
            }
            return errors;
        }
    }

    public static class Upload {
        private String uploadPath = Core.VARS_PATH + "/upload";
        private String uploadHref = null;
        private String uploadName = null;
        private Set<String> allowTypes = null;
        private Set<String> allowExtns = null;
        private File   resultFile;

        public Upload setUploadPath(String path) {
            this.uploadPath = path;
            return this;
        }
        public Upload setUploadHref(String href) {
            this.uploadHref = href;
            return this;
        }
        public Upload setUploadName(String name) {
            this.uploadName = name;
            return this;
        }
        public Upload setAllowTypes(String... type) {
            this.allowTypes = new LinkedHashSet(Arrays.asList(type));
            return this;
        }
        public Upload setAllowExtns(String... extn) {
            this.allowExtns = new LinkedHashSet(Arrays.asList(extn));
            return this;
        }

        public String upload(InputStream stream, String fame, String name, String type) throws Wrong {
            /**
             * 检查文件类型
             */
            if (this.allowTypes != null
            && !this.allowTypes.contains(type)) {
                // 文件类型不对
                throw new Wrong("fore.form.unallowed.types", this.allowTypes.toString());
            }

            String extn = this.getExtn(name);

            /**
             * 检查扩展名
             */
            if (this.allowExtns != null
            && !this.allowExtns.contains(extn)) {
                // 扩展名不对
                throw new Wrong("fore.form.unallowed.extns", this.allowExtns.toString());
            }

            /**
             * 将路径放入数据中
             */
            String famc = Core.getUniqueId() + "." + extn;
            if (this.uploadName != null) {
                famc = new SimpleDateFormat(this.uploadName).format(new Date()) + famc;
            }
            String path = famc;
            if (this.uploadPath != null) {
                path = this.uploadPath + "/" + famc;
            }
            String href = famc;
            if (this.uploadHref != null) {
                href = this.uploadHref + "/" + famc;
            }

            try {
                File file = new File(path);
                BufferedOutputStream bos = new BufferedOutputStream(
                                           new FileOutputStream(file));
                BufferedInputStream  bis = new BufferedInputStream(stream);
                Streams.copy(bis, bos, true);
            } catch (IOException ex) {
                throw new Wrong(ex, "fore.form.upload.failed");
            }

            return href;
        }

        private String getExtn(String name) {
            // 如果有指定扩展名就依次去匹配
            if (this.allowExtns != null) {
              for (String ext : allowExtns) {
                if (name.endsWith("." + ext)) {
                  return  ext;
                }
              }
            }

            // 否则取最后的句点后作为扩展名
            name = new File(name).getName();
            int pos = name.lastIndexOf('.');
            if (pos > 1) {
              return  name.substring(pos+1);
            }
            else {
              return  "";
            }
        }

        public File getFile() {
            return resultFile;
        }
    }

}
