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
public class HongsException extends Exception {

    /**
     * 通用异常(不确定异常号)
     */
    public static final int COMMON = 0x1000;

    /**
     * 通知异常(消息作为翻译)
     */
    public static final int NOTICE = 0x1001;

    /**
     * 动作异常
     */
    public static final int ACTION = 0x1100;

    private HongsLocalized that;

    public HongsException(int code, String desc, Throwable cause) {
        super(cause);

        that = new HongsLocalized(code, desc);

        if (code < 0x1000 || code > 0xFFFFF) {
            throw new HongsError(0x13,
                "Exception code must be from 0x1000(65536) to 0xFFFFF(1048575).");
        }
    }

    public HongsException(int code, Throwable cause) {
        this(code, cause.getMessage(), cause);
    }

    public HongsException(int code, String error) {
        this(code, error, null);
    }

    public HongsException(int code) {
        this(code, null, null);
    }

    public int getCode() {
        return that.getCode();
    }

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

    public String getLocalizedSection() {
        return that.getLocalizedSection();
    }

    public void setLocalizedSection(String lang) {
        that.setLocalizedSection(lang);
    }

    public String[] getLocalizedOptions() {
        return that.getLocalizedOptions();
    }

    public void setLocalizedOptions(String... opts) {
        that.setLocalizedOptions(opts);
    }

}
