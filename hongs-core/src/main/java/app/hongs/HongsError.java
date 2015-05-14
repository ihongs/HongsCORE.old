package app.hongs;

/**
 * 通用错误类
 *
 * <h3>取值范围:</h3>
 * <pre>
 * 核心: 0x10~0xFF (16~255)
 * 用户: 0x100~0xFFF (256~4095)
 * </pre>
 *
 * @author Hongs
 */
public class HongsError extends Error implements HongsCause {

    protected HongsLocalized that;

    public HongsError(int code, String desc, Throwable cause) {
        super(cause);

        that = new HongsLocalized(code, desc, this);

        if (code < 0x10 || code > 0xFFF) {
            throw new HongsError(0x21,
                "Error code must be from 0x10(16) to 0xFFF(4095).");
        }
    }

    public HongsError(int code, Throwable cause) {
        this(code, cause.getMessage(), cause);
    }

    public HongsError(int code, String desc) {
        this(code, desc, null);
    }

    public HongsError(int code) {
        this(code, null, null);
    }

    @Override
    public int getCode() {
        return that.getCode();
    }

    @Override
    public String getDesc() {
        return that.getDesc();
    }

    @Override
    public String toString() {
        return this.getMessage();
    }

    @Override
    public String getMessage() {
        return that.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return that.getLocalizedMessage();
    }

    @Override
    public String getLocalizedSection() {
        return that.getLocalizedSection();
    }

    @Override
    public String[] getLocalizedOptions() {
        return that.getLocalizedOptions();
    }

    @Override
    public HongsError setLocalizedSection(String lang) {
        that.setLocalizedSection(lang);
        return this;
    }

    @Override
    public HongsError setLocalizedOptions(String... opts) {
        that.setLocalizedOptions(opts);
        return this;
    }

    public static  final int COMMON = 0x10;

    public static  final int NOTICE = 0x11;

    /**
     * 常规错误(无需错误代码)
     * @param desc
     * @param cause
     * @return 
     */
    public static  HongsError common(String desc, Throwable cause) {
        return new HongsError(COMMON, desc, cause);
    }

    /**
     * 常规错误(无需错误代码)
     * @param desc
     * @return 
     */
    public static  HongsError common(String desc ) {
        return new HongsError(COMMON, desc, null );
    }

    /**
     * 通知错误(解释作为翻译)
     * 与 setLocalizedSection, setLocalizedOptions 配套使用
     * @param desc
     * @param cause
     * @return 
     */
    public static  HongsError notice(String desc, Throwable cause) {
        return new HongsError(NOTICE, desc, cause);
    }
    
    /**
     * 通知错误(解释作为翻译)
     * 与 setLocalizedSection, setLocalizedOptions 配套使用
     * @param desc
     * @return 
     */
    public static  HongsError notice(String desc ) {
        return new HongsError(NOTICE, desc, null );
    }

}
