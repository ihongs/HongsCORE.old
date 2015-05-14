package app.hongs;

/**
 * 通用异常类
 *
 * <h3>取值范围:</h3>
 * <pre>
 * 核心: 0x1000~0xFFFF (4096~65535)
 * 用户: 0x10000~0xFFFFF (65536~1048575)
 * </pre>
 *
 * @author Hongs
 */
public class HongsException extends Exception implements HongsCause {

    protected HongsLocalized that;

    public HongsException(int code, String desc, Throwable cause) {
        super(cause);

        that = new HongsLocalized(code, desc, this);

        if (code < 0x1000 || code > 0xFFFFF) {
            throw new HongsError(0x22,
                "Exception code must be from 0x1000(65536) to 0xFFFFF(1048575).");
        }
    }

    public HongsException(int code, Throwable cause) {
        this(code, cause.getMessage(), cause);
    }

    public HongsException(int code, String desc) {
        this(code, desc, null);
    }

    public HongsException(int code) {
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
    public HongsException setLocalizedSection(String lang) {
        that.setLocalizedSection(lang);
        return this;
    }

    @Override
    public HongsException setLocalizedOptions(String... opts) {
        that.setLocalizedOptions(opts);
        return this;
    }

    public static  final int COMMON = 0x1000;

    public static  final int NOTICE = 0x1001;

    /**
     * 常规异常(无需异常代码)
     * @param desc
     * @param cause
     * @return
     */
    public static  HongsException common(String desc, Throwable cause) {
        return new HongsException(COMMON, desc, cause);
    }

    /**
     * 常规异常(无需异常代码)
     * @param desc
     * @return
     */
    public static  HongsException common(String desc ) {
        return new HongsException(COMMON, desc, null );
    }

    /**
     * 通知异常(解释作为翻译)
     * 与 setLocalizedSection, setLocalizedOptions 配套使用
     * @param desc
     * @param cause
     * @return
     */
    public static  HongsException notice(String desc, Throwable cause) {
        return new HongsException(NOTICE, desc, cause);
    }

    /**
     * 通知异常(解释作为翻译)
     * 与 setLocalizedSection, setLocalizedOptions 配套使用
     * @param desc
     * @return
     */
    public static  HongsException notice(String desc ) {
        return new HongsException(NOTICE, desc, null );
    }

}
