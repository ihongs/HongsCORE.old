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

    public void setLocalizedOptions(String... options) {
        that.setLocalizedOptions(options);
    }

}
