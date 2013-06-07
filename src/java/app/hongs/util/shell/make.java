package app.hongs.util.shell;

import app.hongs.ShellHelper;
import java.util.Map;

/**
 * 构建工具
 * 用于自动构建模块等
 * @author Hong
 */
public class make {
    public static void shell(Map<String, String[]> opts)
    throws app.hongs.HongsException {
        Map optz = ShellHelper.getOpts(opts,
            "module",
            "!U", "!A", "?Useage:\nhongs.util.make -module [Module Name]"
        );
    }
}
