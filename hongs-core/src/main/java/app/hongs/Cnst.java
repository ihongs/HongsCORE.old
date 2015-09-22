package app.hongs;

/**
 * 常量
 * @author Hongs
 */
public class Cnst {

    //** 查询参数 **/

    final public static String ID_KEY =  "id"; // 编号参数

    final public static String MD_KEY =  "md"; // 模式      (Mode)

    final public static String WD_KEY =  "wd"; // 关键词    (Word)

    final public static String PN_KEY =  "pn"; // 页码编号  (Page num)

    final public static String GN_KEY =  "gn"; // 分页数量  (Pags num)

    final public static String RN_KEY =  "rn"; // 每页行数  (Rows num)

    final public static String OB_KEY =  "ob"; // 排序字段  (Order By)

    final public static String RB_KEY =  "rb"; // 应答字段  (Reply By)
    
    final public static String OR_KEY =  "or"; // 或关系    (Or)

    final public static String AR_KEY =  "ar"; // 与或      (And Or)

    final public static String XR_KEY =  "xr"; // 否或      (Lucene 特有)

    //** 关系符号 **/

    final public static String EQ_REL = "~eq"; // 等于

    final public static String NE_REL = "~ne"; // 不等于

    final public static String LT_REL = "~lt"; // 小于

    final public static String LE_REL = "~le"; // 小于或等于

    final public static String GT_REL = "~gt"; // 大于

    final public static String GE_REL = "~ge"; // 大于或等于

    final public static String IN_REL = "~in"; // 包含

    final public static String NI_REL = "~ni"; // 不包含    (Not in)

    final public static String AI_REL = "~ai"; // 全包含    (All in, Lucene 特有)

    final public static String OI_REL = "~oi"; // 或包含    (Or  in, Lucene 特有)

    final public static String OR_REL = "~or"; // 或等于    (Or    , Lucene 特有)

    final public static String WT_REL = "~wt"; // 权重      (        Lucene 特有)

}
