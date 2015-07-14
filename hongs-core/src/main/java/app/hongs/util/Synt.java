package app.hongs.util;

import app.hongs.HongsError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    /**
     * 取默认值(null 视为无值)
     * @param <T>
     * @param vals
     * @return
     */
    public static <T> T defoult(T... vals) {
        for (T  val  : vals) {
            if (val != null) {
                return val ;
            }
        }
        return  null;
    }

    /**
     * 取默认值(null,false,0,"" 均视为无值)
     * @param <T>
     * @param vals
     * @return
     */
    public static <T> T defxult(T... vals) {
        for (T  val  : vals) {
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
     * 确保此变量类型为 def 的类型
     * 当 val 为空时返回 def
     * 其他说明请参见 declare(Object, Class)
     * @param <T>
     * @param val
     * @param def
     * @return
     */
    public static <T> T declare(Object val, T def) {
        val = declare(val, def.getClass());
        return null == val ? def : (T) val;
    }

    /**
     * 确保此变量类型为 cls 类型
     * string,number(int,long...) 类型间可互转;
     * cls 为 Boolean  时:
     *      非 0 数字为 true,
     *      空字符串为 false,
     *      字符串 1,y,t,yes,true 为真,
     *      字符串 0,n,f,no,false 为假;
     * cls 为 List,Set 时:
     *      val 非 List,Set,Map 时构建 List,Set 后将 val 加入其下,
     *      val 为 Map 则取 values;
     * 但其他类型均无法转为 Map.
     * @param <T>
     * @param val
     * @param cls
     * @return
     */
    public static <T> T declare(Object val, Class<T> cls) {
        if (val == null) {
            return null;
        }

        if (List.class.isAssignableFrom(cls)) {
            if (val instanceof List) {
            } else if (val instanceof Set) {
                val = new ArrayList(( Set) val);
            } else if (val instanceof Map) {
                val = ((Map) val).values();
            } else if (val instanceof Object[]) {
                val = Arrays.asList(( Object[]) val);
            } else {
                List lst = new ArrayList();
                lst.add(val);
                val = lst;
            }
        } else
        if ( Set.class.isAssignableFrom(cls)) {
            if (val instanceof Set) {
            } else if (val instanceof List) {
                val = new LinkedHashSet((List) val);
            } else if (val instanceof Map ) {
                val = new LinkedHashSet(((Map) val).values());
            } else if (val instanceof Object[]) {
                val = new LinkedHashSet(Arrays.asList((Object[]) val));
            } else {
                Set set = new LinkedHashSet( );
                set.add(val);
                val = set;
            }
        } else {
            /**
             * 针对 servlet 的 requestMap 制定的规则
             * 如果只需要一个值, 而 requestMap 是数组
             * 则取第一个值
             */
            if (val instanceof Collection) {
                val = ((Collection) val).toArray(new String[0])[0];
            } else
            if (val instanceof Object [ ]) {
                val = ((Object [ ]) val)[0];
            }

            if ( String.class.isAssignableFrom(cls)) {
                val = val.toString();
            } else
            if ( Number.class.isAssignableFrom(cls)) {
                if (EMPT.equals(val)) {
                    return null; // 空串视为未取值
                }
                if (Integer.class.isAssignableFrom(cls)) {
                    if (val instanceof Number) {
                        val = ((Number) val).intValue();
                    } else
                    if (val instanceof String) {
                        val = Integer.parseInt(((String) val).trim());
                    }
                } else if (Byte.class.isAssignableFrom(cls)) {
                    if (val instanceof Number) {
                        val = ((Number) val).byteValue();
                    } else
                    if (val instanceof String) {
                        val = Byte.parseByte(((String) val).trim());
                    }
                } else if (Short.class.isAssignableFrom(cls)) {
                    if (val instanceof Number) {
                        val = ((Number) val).shortValue();
                    } else
                    if (val instanceof String) {
                        val = Short.parseShort(((String) val).trim());
                    }
                } else if (Long.class.isAssignableFrom(cls)) {
                    if (val instanceof Number) {
                        val = ((Number) val).longValue();
                    } else
                    if (val instanceof String) {
                        val = Long.parseLong(((String) val).trim());
                    }
                } else if (Float.class.isAssignableFrom(cls)) {
                    if (val instanceof Number) {
                        val = ((Number) val).floatValue();
                    } else
                    if (val instanceof String) {
                        val = Float.parseFloat(((String) val).trim());
                    }
                } else {
                    if (val instanceof Number) {
                        val = ((Number) val).doubleValue();
                    } else
                    if (val instanceof String) {
                        val = Double.parseDouble(((String) val).trim());
                    }
                }
            } else
            if (Boolean.class.isAssignableFrom(cls)) {
                if (val instanceof Number) {
                    val = ((Number) val).intValue() != 0;
                } else if (val instanceof String) {
                    String str = ((String) val).trim(  );
                    if (TRUE.matcher(str).matches()) {
                        val = true ;
                    } else
                    if (FAKE.matcher(str).matches()) {
                        val = false;
                    }
                }
            }
        }

        try {
            return (T) val;
        }   catch  (ClassCastException ex) {
            throw new HongsError(0x46, ex);
        }
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

    private static final Number  ZERO = 0 ;
    private static final String  EMPT = "";
    private static final Boolean FALS = false;
    public  static final Pattern TRUE = Pattern.compile( "^(1|y|t|yes|true)$", Pattern.CASE_INSENSITIVE);
    public  static final Pattern FAKE = Pattern.compile("^(|0|n|f|no|false)$", Pattern.CASE_INSENSITIVE);

    /**
     * 在 Each.each 里
     * 返回 EACH.NEXT 则排除此项
     * 返回 EACH.LAST 则跳出循环
     */
    public static enum LOOP {NEXT, LAST};

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
