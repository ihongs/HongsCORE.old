package app.hongs.util;

import app.hongs.CoreLogger;
import app.hongs.HongsError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 常用语法补充
 * @author Hongs
 */
public class Synt {

    private static final Number  ZERO = 0 ;
    private static final String  EMPT = "";
    private static final Boolean FALS = false;
    private static final Pattern MEXP = Pattern.compile("\\s*:\\s*");
    private static final Pattern SEXP = Pattern.compile("\\s*[,\\+\\s]\\s*");

    /**
     * 视为真的字符串有: 1,y,t,yes,true
     */
    public  static final Pattern TRUE = Pattern.compile( "^(1|y|t|yes|true)$", Pattern.CASE_INSENSITIVE);

    /**
     * 视为假的字符串有: 0,n,f,no,false 和空串
     */
    public  static final Pattern FAKE = Pattern.compile("^(|0|n|f|no|false)$", Pattern.CASE_INSENSITIVE);

    /**
     * 在 Each.each 里
     * 返回 LOOP.NEXT 则排除此项
     * 返回 LOOP.LAST 则跳出循环
     */
    public  static enum  LOOP  { NEXT, LAST };

    /**
     * 取默认值(null 视为无值)
     * @param <T>
     * @param vals
     * @return
     */
    public static <T> T defoult(T... vals) {
        for (T  val :  vals) {
            if (val != null) {
                return val ;
            }
        }
        return  null;
    }

    /**
     * 取默认值(null,false,0,"" 均视为无值, 同 javascript)
     * @param <T>
     * @param vals
     * @return
     */
    public static <T> T defxult(T... vals) {
        for (T  val :  vals) {
            if (val != null
            && !FALS.equals(val)
            && !EMPT.equals(val)
            && !ZERO.equals(val)) {
                return val ;
            }
        }
        return  null;
    }

    /**
     * 确保此变量类型为 cls 类型
     * string,number(int,long...) 类型间可互转;
     * cls 为 Boolean  时:
     *      非 0 数字为 true,
     *      空字符串为 false,
     *      字符串 1,y,t,yes,true 为真,
     *      字符串 0,n,f,no,false 为假;
     * cls 为 Array,List,Set 时:
     *      val 非 List,Set,Map 时构建 Array,List,Set 后将 val 加入其下,
     *      val 为 Map 则取 values;
     * 但其他类型均无法转为 Map.
     * 通常针对外部数据
     * @param <T>
     * @param val
     * @param cls
     * @return
     */
    public static <T> T declare(Object val, Class<T> cls) {
        if (val == null) {
            return null;
        }

        if (Object[].class.isAssignableFrom(cls)) {
            if (val instanceof Object[] ) {
            } else if (val instanceof Set ) {
                val = ((Set) val).toArray();
            } else if (val instanceof Map ) {
                val = ((Map) val).values( ).toArray();
            } else {
                val = new Object[ ] { val };
            }
        } else
        if (List.class.isAssignableFrom(cls)) {
            if (val instanceof List) {
            } else if (val instanceof Set ) {
                val = new ArrayList(( Set ) val);
            } else if (val instanceof Map ) {
                val = new ArrayList(((Map ) val).values());
            } else if (val instanceof Object[] ) {
                val = Arrays.asList(( Object[] ) val /**/);
            } else {
                List lst = new ArrayList ();
                lst.add(val);
                val = lst;
            }
        } else
        if ( Set.class.isAssignableFrom(cls)) {
            if (val instanceof Set ) {
            } else if (val instanceof List) {
                val = new LinkedHashSet((List) val);
            } else if (val instanceof Map ) {
                val = new LinkedHashSet(((Map) val).values());
            } else if (val instanceof Object[] ) {
                val = new LinkedHashSet(Arrays.asList((Object[]) val));
            } else {
                Set set = new LinkedHashSet();
                set.add(val);
                val = set;
            }
        } else
        if ( Map.class.isAssignableFrom(cls)) {
            if (val instanceof Map ) {
            } else {
                // 其他类型均无法转换为 Map
                throw new HongsError(0x43, "'" + val + "' can not be cast to Map");
            }
        } else {
            /**
             * 针对 servlet 的 requestMap 制定的规则, 多个值取第一个值
             */
            if (val instanceof Object[]) {
                val = ((Object[]) val)[0];
            } else
            if (val instanceof List) {
                val = ((List) val).get(0);
            } else
            if (val instanceof Set ) {
                val = ((Set ) val).toArray()[0];
            }

            if (String.class.isAssignableFrom(cls)) {
                val = val.toString();
            } else
            if (Number.class.isAssignableFrom(cls)) {
                if (EMPT.equals(val)) {
                    return null; // 空串视为未取值
                }
                if (Integer.class.isAssignableFrom(cls)) {
                    if (val instanceof Number) {
                        val = ((Number) val).intValue();
                    } else
                    if (val instanceof String) {
                      try {
                        val = Integer.parseInt(((String) val).trim());
                      } catch (NumberFormatException ex) {
                        throw new HongsError(0x43, "'" + val + "' can not be cast to int");
                      }
                    }
                } else if (Byte.class.isAssignableFrom(cls)) {
                    if (val instanceof Number) {
                        val = ((Number) val).byteValue();
                    } else
                    if (val instanceof String) {
                      try {
                        val = Byte.parseByte(((String) val).trim());
                      } catch (NumberFormatException ex) {
                        throw new HongsError(0x43, "'" + val + "' can not be cast to byte");
                      }
                    }
                } else if (Short.class.isAssignableFrom(cls)) {
                    if (val instanceof Number) {
                        val = ((Number) val).shortValue();
                    } else
                    if (val instanceof String) {
                      try {
                        val = Short.parseShort(((String) val).trim());
                      } catch (NumberFormatException ex) {
                        throw new HongsError(0x43, "'" + val + "' can not be cast to short");
                      }
                    }
                } else if (Long.class.isAssignableFrom(cls)) {
                    if (val instanceof Number) {
                        val = ((Number) val).longValue();
                    } else
                    if (val instanceof String) {
                      try {
                        val = Long.parseLong(((String) val).trim());
                      } catch (NumberFormatException ex) {
                        throw new HongsError(0x43, "'" + val + "' can not be cast to long");
                      }
                    }
                } else if (Float.class.isAssignableFrom(cls)) {
                    if (val instanceof Number) {
                        val = ((Number) val).floatValue();
                    } else
                    if (val instanceof String) {
                      try {
                        val = Float.parseFloat(((String) val).trim());
                      } catch (NumberFormatException ex) {
                        throw new HongsError(0x43, "'" + val + "' can not be cast to float");
                      }
                    }
                } else {
                    if (val instanceof Number) {
                        val = ((Number) val).doubleValue();
                    } else
                    if (val instanceof String) {
                      try {
                        val = Double.parseDouble(((String) val).trim());
                      } catch (NumberFormatException ex) {
                        throw new HongsError(0x43, "'" + val + "' can not be cast to double");
                      }
                    }
                }
            } else
            if (Boolean.class.isAssignableFrom(cls)) {
                if (val instanceof Number) {
                    val = ((Number) val).intValue() != 0;
                } else if (val instanceof String) {
                    String str = ( (String) val ).trim();
                    if (TRUE.matcher(str).matches()) {
                        val = true ;
                    } else
                    if (FAKE.matcher(str).matches()) {
                        val = false;
                    } else {
                        throw new HongsError(0x43, "'" + str + "' can not be cast to boolean");
                    }
                }
            }
        }

        try {
            return (T) val;
        }   catch  (ClassCastException ex) {
            throw new HongsError(0x43, ex);
        }
    }

    /**
     * 确保此变量类型为 def 的类型
     * val 为空时则返回 def
     * 其他的说明请参见 declare(val, cls)
     * 通常针对外部数据
     * @param <T>
     * @param val
     * @param def
     * @return
     */
    public static <T> T declare(Object val, T def) {
        if  ( null == def) return (T)  val;
        val = declare(val, def.getClass());
        if  ( null != val) return (T)  val;
        else               return      def;
    }

    /**
     * 同 decalare(val, def)
     * 但转换不了则返回 def 而不抛出错误
     * 通常针对内部数据
     * @param <T>
     * @param val
     * @param def
     * @return
     */
    public static <T> T asserts(Object val, T def) {
        try {
            return declare(val, def);
        } catch (HongsError ex) {
            CoreLogger.getLogger(
                       Synt.class.toString()
                               + ".asserts")
                      .trace(ex.toString( ));
            return def;
        }
    }

    /**
     * 确保此变量为 List 类型
     * 本方法用于请求处理参数
     * 与 declare(Object, T) 不同
     * 当数据为字符串时
     * 空串会返回空 List
     * 否则按","或"+"或空格等拆分
     * @param val
     * @return
     */
    public static List declist(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            String   s = ((String) val).trim();
            if ("".equals(s)) {
                return new ArrayList( );
            }
            String[] a = SEXP.split (s);
            return Arrays.asList(a);
        }
        return declare(val, List.class);
    }

    /**
     * 确保此变量为 Set  类型
     * 本方法用于请求处理参数
     * 与 declare(Object, T) 不同
     * 当数据为字符串时
     * 空串会返回空 Set
     * 否则按","或"+"或空格等拆分
     * @param val
     * @return
     */
    public static Set declset(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            String   s = ((String) val).trim();
            if ("".equals(s)) {
                return new LinkedHashSet();
            }
            String[] a = SEXP.split (s);
            List   b = Arrays.asList(a);
            return new LinkedHashSet(b);
        }
        return declare(val, Set.class );
    }

    /**
     * 确保此变量为 Map  类型
     * 本方法用于请求处理参数
     * 与 declare(Object, T) 不同
     * 当数据为字符串时
     * 空串会返回空 Map
     * 否则按","或"+"或空格等拆分
     * 然后按":"拆分键值
     * @param val
     * @return
     */
    public static Map declmap(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            String   s = ((String) val).trim();
            if ("".equals(s)) {
                return new LinkedHashMap();
            }
            String[] a = SEXP.split (s);
            Map m = new LinkedHashMap();
            for (String   b : a) {
                 String[] c = MEXP.split(b);
                if (1==c.length) {
                    m.put(c[0] , c[0] );
                } else {
                    m.put(c[0] , c[1] );
                }
            }
            return m;
        }
        return declare(val, Map.class );
    }

    /**
     * 遍历 Map
     * @param data
     * @param conv
     * @return
     */
    public static Map foreach(Map data, Each conv) {
        conv.setObj(data);
        conv.setIdx(-1);
        Map dat = new LinkedHashMap();
        for (Object o : data.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            Object k = e.getKey();
            Object v = e.getValue();
            conv.setKey(k);
            v = conv.each(v);
            if (v == LOOP.NEXT) {
                continue;
            }
            if (v == LOOP.LAST) {
                break;
            }
            dat.put(k, v);
        }
        return dat;
    }

    /**
     * 遍历 Set
     * @param data
     * @param conv
     * @return
     */
    public static Set foreach(Set data, Each conv) {
        conv.setObj(data);
        conv.setKey(null);
        conv.setIdx(-1);
        Set dat = new LinkedHashSet();
        for (Object v : data) {
            v = conv.each(v);
            if (v == LOOP.NEXT) {
                continue;
            }
            if (v == LOOP.LAST) {
                break;
            }
            dat.add(v);
        }
        return dat;
    }

    /**
     * 遍历 List
     * @param data
     * @param conv
     * @return
     */
    public static List foreach(List data, Each conv) {
        conv.setObj(data);
        conv.setKey(null);
        List dat = new ArrayList();
        for (int i = 0; i < data.size(); i++) {
            Object v = data.get(i);
            conv.setIdx(i);
            v = conv.each(v);
            if (v == LOOP.NEXT) {
                continue;
            }
            if (v == LOOP.LAST) {
                break;
            }
            dat.add(v);
        }
        return dat;
    }

    /**
     * 遍历数组
     * @param data
     * @param conv
     * @return
     */
    public static Object[] foreach(Object[] data, Each conv) {
        conv.setObj(data);
        conv.setKey(null);
        List dat = new ArrayList();
        for (int i = 0; i < data.length; i++) {
            Object v = data[i];
            conv.setIdx(i);
            v = conv.each(v);
            if (v == LOOP.NEXT) {
                continue;
            }
            if (v == LOOP.LAST) {
                break;
            }
            dat.add(v);
        }
        return dat.toArray();
    }

    public static interface Each {
        public Object each(Object v);
        public void setObj(Object d);
        public void setKey(Object k);
        public void setIdx(int i);
    }

    /**
     * 遍历每个节点
     */
    public static abstract class EachNode implements Each {
        protected Object o;
        protected Object k;
        protected int i;

        @Override
        public void setObj(Object o) {
            this.o = o;
        }

        @Override
        public void setKey(Object k) {
            this.k = k;
        }

        @Override
        public void setIdx(int i) {
            this.i = i;
        }
    }

    /**
     * 遍历叶子节点
     */
    public static abstract class LeafNode extends EachNode {
        public abstract Object leaf(Object v);

        @Override
        public Object each(Object v) {
            if (v instanceof Map ) {
                return foreach((Map ) v, this);
            } else
            if (v instanceof Set ) {
                return foreach((Set ) v, this);
            } else
            if (v instanceof List) {
                return foreach((List) v, this);
            } else
            if (v instanceof Object[]) {
                return foreach((Object[]) v, this);
            } else {
                return leaf(v);
            }
        }
    }

}
