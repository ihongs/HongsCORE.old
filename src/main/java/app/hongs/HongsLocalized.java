package app.hongs;

/**
 * 异常本地化工具
 * @author Hongs
 */
public class HongsLocalized {

    private int code;
    private String desc;
    private String lang;
    private String[] opts;
    private Throwable that;

    public HongsLocalized(int code, String desc, Throwable that) {
        this.code = code;
        this.desc = desc;
        this.that = that;
    }

    /**
     * 获取代号
     * @return
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 获取描述
     * @return
     */
    public String getDesc() {
        return this.desc;
    }

    /**
     * 获取消息
     * @return
     */
    public String getMessage()
    {
        String codx = "Ex" + Integer.toHexString(code);
        String desx = desc != null ? desc : "";
        if (null  !=  lang) {
            codx   =  lang
                  .replaceAll("[/\\\\]", ".")
                .replaceFirst("\\.error$","")
              + "." + codx;
        }
        if (null  ==  desc) {
            Throwable cause = that.getCause();
            if (null != cause && cause instanceof HongsCause) {
                return  cause.getMessage(   );
            }
        }
        return codx + ": " + desx;
    }

    /**
     * 获取本地消息
     * @return
     */
    public String getLocalizedMessage() {
        CoreLanguage trns;
        String ckey, dkey;
        String codx, desx;
        String[] optx;

        trns = new CoreLanguage("_error_");
        codx = "Ex" + Integer.toHexString(  code  );
        desx = desc != null ? desc :  ""  ;
        optx = opts != null ? opts : new String[]{};

        // 0x10,0x11,0x1000,0x1001 为保留的代号
        // 0x11,0x1001 使用消息作为语言键
        switch (code) {
            case HongsException.COMMON:
            case HongsError.COMMON:
                ckey = "fore.error";
                dkey = "";
                break;
            case HongsException.NOTICE:
            case HongsError.NOTICE:
                ckey = "fore.error";
                dkey = desx;
                break;
            default:
                ckey = "core.error";
                dkey = "error." + codx;
        }

        if (null  !=  lang) {
            trns.load(lang);
            codx   =  lang
                  .replaceAll("[/\\\\]", ".")
                .replaceFirst("\\.error$","")
              + "." + codx ;
        }
        if (trns.containsKey(ckey)) {
            codx = trns.translate(ckey, codx);
        }
        if (trns.containsKey(dkey)) {
            desx = trns.translate(dkey, optx);
        } else {
            Throwable cause = that.getCause();
            if (null != cause && cause instanceof HongsCause) {
                return  cause.getLocalizedMessage();
            }
        }

        return codx + ' ' + desx;
    }

    /**
     * 获取翻译章节(模块语言)
     * @return
     */
    public String getLocalizedSection() {
        return this.lang;
    }

    /**
     * 设置翻译章节(模块语言)
     * @param lang
     */
    public void setLocalizedSection(String lang) {
        this.lang = lang;
    }

    /**
     * 获取翻译选项(填充参数)
     * @return
     */
    public String[] getLocalizedOptions() {
        return this.opts;
    }

    /**
     * 设置翻译选项(填充参数)
     * @param opts
     */
    public void setLocalizedOptions(String... opts) {
        this.opts = opts;
    }

}
