package app.hongs;

/**
 * 异常本地化工具
 * @author Hongs
 */
public class HongsLocalized {

    private int code;
    private String desc;
    private String[ ] opts;

    public HongsLocalized(int code, String desc) {
        this.code = code;
        this.desc = desc;
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
        return "(Ex" + Integer.toHexString(code) + ")"
              + ( desc == null  ?  "" : ": " + desc );
    }

    /**
     * 获取本地消息
     * @return
     */
    public String getLocalizedMessage() {
        CoreLanguage lang = new CoreLanguage("_error_");
        String ckey, dkey, codx, desx; String[] optx;
        codx = "Ex" + Integer.toHexString(code);
        desx = desc != null ? desc : "" ;
        optx = opts != null ? opts : new String[] {};

        // 0x10,0x11,0x1000,0x1001 为保留的代号
        // 0x11,0x1001 使用消息作为语言键
        switch (code) {
            case 0x1000:
            case 0x10:
                ckey = "fore.error";
                dkey = "";
                break;
            case 0x1001:
            case 0x11:
                ckey = "fore.error";
                dkey = desx;
                break;
            default:
                ckey = "core.error";
                dkey = "error." + codx;
        }

        if (lang.containsKey(ckey)) {
            codx = lang.translate(ckey, codx);
        }
        if (lang.containsKey(dkey)) {
            desx = lang.translate(dkey, optx);
        }

        return codx + " " + desx;
    }

    /**
     * 获取翻译选项
     * @return
     */
    public String[] getLocalizedOptions() {
        return this.opts;
    }

    /**
     * 设置翻译选项
     * @param opts
     */
    public void setLocalizedOptions(String... opts) {
        this.opts = opts;
    }

}
