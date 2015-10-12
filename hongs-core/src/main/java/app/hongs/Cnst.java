package app.hongs;

/**
 * 常量
 * @author Hongs
 */
public class Cnst {

    //** 查询参数 **/

    public static final String ID_KEY =  "id"; // 编号参数

    public static final String MD_KEY =  "md"; // 模式      (Mode)

    public static final String WD_KEY =  "wd"; // 关键词    (Word)

    public static final String PN_KEY =  "pn"; // 页码编号  (Page num)

    public static final String GN_KEY =  "gn"; // 分页数量  (Pags num)

    public static final String RN_KEY =  "rn"; // 每页行数  (Rows num)

    public static final String OB_KEY =  "ob"; // 排序字段  (Order By)

    public static final String RB_KEY =  "rb"; // 应答字段  (Reply By)

    public static final String OR_KEY =  "or"; // 或关系    (Or)

    public static final String AR_KEY =  "ar"; // 与或      (And Or)

    public static final String SR_KEY =  "sr"; // 可或      (Lucene 特有)

    //** 关系符号 **/

    public static final String EQ_REL = "~eq"; // 等于

    public static final String NE_REL = "~ne"; // 不等于

    public static final String LT_REL = "~lt"; // 小于

    public static final String LE_REL = "~le"; // 小于或等于

    public static final String GT_REL = "~gt"; // 大于

    public static final String GE_REL = "~ge"; // 大于或等于

    public static final String IN_REL = "~in"; // 包含

    public static final String NI_REL = "~ni"; // 不包含    (Not in)

    public static final String AI_REL = "~ai"; // 全包含    (All in, Lucene 特有)

    public static final String OI_REL = "~oi"; // 或包含    (Or  in, Lucene 特有)

    public static final String OR_REL = "~or"; // 或等于    (Or    , Lucene 特有)

    public static final String WT_REL = "~wt"; // 权重      (        Lucene 特有)

    //** 分页 **/

    public static final int DEF_ROWS_PER_PAGE = 20; // 默认每页行数

    public static final int DEF_PAGS_FOR_PAGE =  5; // 默认分页数量

    //** 其他 **/

    public static final String CORE_ATTR = "__HONGS_CORE__"; // 核心对象

    public static final String PATH_ATTR = "__HONGS_PATH__"; // 请求路径

    public static final String RESP_ATTR = "__HONGS_RESP__"; // 返回数据

    public static final String RUNNER_ATTR = "__RUNNER__"; // 动作执行器

    public static final String UPLOAD_ATTR = "__UPLOAD__"; // 上传参数键

    public static final String UPDATE_ATTR = "__UPDATE__"; // 更新时间戳(当会话、属性改变时设置)

    public static final String OBJECT_MODE = "__IN_OBJECT_MODE__"; // 对象模式

    public static final String TRNSCT_MODE = "__IN_TRNSCT_MODE__"; // 事务模式

}
