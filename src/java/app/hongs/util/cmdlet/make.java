package app.hongs.util.cmdlet;

import app.hongs.cmdlet.CmdletHelper;
import java.util.Map;

/**
 * 构建工具
 * 用于自动构建模块等
 * @author Hong
 */
public class make {
    public static void shell(Map<String, String[]> opts)
    throws app.hongs.HongsException {
        Map optz = CmdletHelper.getOpts(opts,
            "module=s",
            "with-shell",
            "with-shell-action",
            "!U", "!A", "?Useage:\nhongs.util.make -module [Module Name]"
        );
        
        String  mod = (String)optz.get("module");
        int     pos = mod.lastIndexOf('.');
        String  pkg = mod.substring(0,pos);
        String  cls = mod.substring(pos+1);
        
        
    }
}
