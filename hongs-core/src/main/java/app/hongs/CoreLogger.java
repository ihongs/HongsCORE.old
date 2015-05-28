package app.hongs;

import app.hongs.action.ActionHelper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志记录工具
 *
 * @author Hongs
 */
public class CoreLogger
{

    /**
     * 获取 slf4j 的 Logger 实例
     * @param name
     * @return
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    /**
     * 补充动作环境信息
     * @param text
     * @return
     */
    public static String envir(String text) {
        StringBuilder line = new StringBuilder();

        // add IP Address
        if (Core.ENVIR == 1) {
            Core core = Core.getInstance();
            if ( core.containsKey(ActionHelper.class.getName( ))) {
                ActionHelper helper = Core.getInstance(ActionHelper.class);
                if (/**/null != helper.getRequest()) {
                    line.append(helper.getRequest( ).getRemoteAddr( )/**/)
                        .append(' ');
                } else {
                    line.append("ACTION ");
                }
            } else {
                line.append("ACTION ");
            }
        } else {
           line.append("CMDLET ");
        }

        // add Action Name
        if (/**/null != Core.ACTION_NAME.get()) {
            line.append(Core.ACTION_NAME.get())
                .append(' ');
        }

        return  line.append( text ).toString( );
    }

    /**
     * 输出
     * @param text
     */
    public static void print(String text, Object... args) {
        if (16 == (16 & Core.DEBUG)) {
            return; // 禁止了输出
        }
        getLogger("hongs.print").trace(envir(text), args);
    }

    /**
     * 调试
     * @param text
     */
    public static void debug(String text, Object... args) {
        if (8 == (8 & Core.DEBUG)) {
            return; // 禁止了调试
        }
        if (1 == (1 & Core.DEBUG)) {
            getLogger("hongs.print").debug(envir(text), args);
        }
        if (2 == (2 & Core.DEBUG)) {
            getLogger("hongs.debug").debug(envir(text), args);
        }
    }

    /**
     * 错误
     * @param text
     */
    public static void error(String text, Object... args) {
        if (4 == (4 & Core.DEBUG)) {
            return; // 禁止了错误
        }
        if (1 == (1 & Core.DEBUG)) {
            getLogger("hongs.print").error(envir(text), args);
        }
        if (2 == (2 & Core.DEBUG)) {
            getLogger("hongs.error").error(envir(text), args);
        }
    }

    /**
     * 错误
     * @param t
     */
    public static void error(Throwable t)
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(b));
        CoreLogger.error(b.toString());
    }

}
