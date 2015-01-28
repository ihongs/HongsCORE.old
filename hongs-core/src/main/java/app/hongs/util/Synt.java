package app.hongs.util;

import app.hongs.HongsError;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 语法补充
 * @author Hongs
 */
public class Synt {

    /**
     * 确保此变量类型为 cls 类型
     * string,number(int,long...) 类型间可互转
     * 当 cls 为 List,Set 时: val 为 Map 则取 values; val 为非 List,Set,Map 时将构建 List或Set, 然后将 val 加入其下.
     * @param <T>
     * @param val
     * @param cls
     * @return
     */
    public static <T> T declare(Object val, Class<T> cls) {
        if (val == null) {
            return null;
        }

        if (String.class.isAssignableFrom(cls)) {
            val = val.toString();
        } else if (List.class.isAssignableFrom(cls)) {
            if (val instanceof List) {
            } else if (val instanceof Set) {
                val = new ArrayList((Set) val);
            } else if (val instanceof Map) {
                val = ((Map) val).values();
            } else {
                List lst = new ArrayList();
                lst.add(val);
                val = lst;
            }
        } else if ( Set.class.isAssignableFrom(cls)) {
            if (val instanceof Set ) {
            } else if (val instanceof List) {
                val = new LinkedHashSet((List) val);
            } else if (val instanceof Map ) {
                val = new LinkedHashSet(((Map) val).values());
            } else {
                Set set = new LinkedHashSet( );
                set.add(val);
                val = set;
            }
        } else if (Boolean.class.isAssignableFrom(cls)) {
            if (val instanceof Number) {
                val = ((Number) val).intValue() != 0;
            } else if (val instanceof String) {
                String str = ((String) val).trim(  );
                if ("".equals(str)) {
                    val = false;
                } else if (FLSE.matcher(str).matches()) {
                    val = false;
                } else if (TRUE.matcher(str).matches()) {
                    val = true ;
                }
            }
        } else if (Integer.class.isAssignableFrom(cls)) {
            if (val instanceof Number) {
                val = ((Number) val).intValue();
            } else if (val instanceof String) {
                val = Integer.parseInt(((String) val).trim());
            }
        } else if (Byte.class.isAssignableFrom(cls)) {
            if (val instanceof Number) {
                val = ((Number) val).byteValue();
            } else if (val instanceof String) {
                val = Byte.parseByte(((String) val).trim());
            }
        } else if (Short.class.isAssignableFrom(cls)) {
            if (val instanceof Number) {
                val = ((Number) val).shortValue();
            } else if (val instanceof String) {
                val = Short.parseShort(((String) val).trim());
            }
        } else if (Long.class.isAssignableFrom(cls)) {
            if (val instanceof Number) {
                val = ((Number) val).longValue();
            } else if (val instanceof String) {
                val = Long.parseLong(((String) val).trim());
            }
        } else if (Float.class.isAssignableFrom(cls)) {
            if (val instanceof Number) {
                val = ((Number) val).floatValue();
            } else if (val instanceof String) {
                val = Float.parseFloat(((String) val).trim());
            }
        } else if (Number.class.isAssignableFrom(cls)) {
            if (val instanceof Number) {
                val = ((Number) val).doubleValue();
            } else if (val instanceof String) {
                val = Double.parseDouble(((String) val).trim());
            }
        }

        try {
            return (T) val;
        } catch (ClassCastException ex) {
            throw new HongsError(70, ex);
        }
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
        if (val == null) {
            return def ;
        }

        return (T) declare(val, def.getClass());
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

    public static Pattern TRUE = Pattern.compile("^(1|y|t|yes|true)$", Pattern.CASE_INSENSITIVE);
    public static Pattern FLSE = Pattern.compile("^(0|n|f|no|false)$", Pattern.CASE_INSENSITIVE);

    /**
     * 在 Each.each 里
     * 返回 EACH.NEXT 则排除此项
     * 返回 EACH.LAST 则跳出循环
     */
    public static enum LOOP { NEXT, LAST };

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

        public void setObj(Object o) {
            this.o = o;
        }

        public void setKey(Object k) {
            this.k = k;
        }

        public void setIdx(int i) {
            this.i = i;
        }
    }

    /**
     * 遍历叶子节点
     */
    public static abstract class LeafNode extends EachNode {
        public abstract Object leaf(Object v);

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
