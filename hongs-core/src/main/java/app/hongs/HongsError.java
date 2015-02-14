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

    /**
     * 通用错误
     */
    public static final int COMMON = 0x10;

    /**
     * 通知错误
     */
    public static final int NOTICE = 0x11;

    /**
     * 动作错误(异常数据已进过)
     */
    public static final int ACTION = 0x12;

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
