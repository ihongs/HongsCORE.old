package app.hongs.serv;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.anno.Action;
import app.hongs.util.Data;
import app.hongs.util.Synt;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 通用打包动作
 * 可一次调用多个动作
 * 批量执行后返回数据
 * 请求格式举例:
 * /ACTION/NAME=PARAMS&/ACTION/NAME2#0=PARAMS&ACTION/NAME2#1=PARAMS&quit=1
 * PARAMS 需要 urlencode 编码
 * @author Hongs
 */
@Action("common/pack")
public class PackAction {

    @Action("__main__")
    public void pack(ActionHelper helper)
    throws HongsException {
        Map<String , Object> rets = new HashMap();
        Map<String , Object> acts = helper.getRequestData(  );
        boolean quit = Synt.declare(acts.get("!quit"), false);

        HttpServletRequest  req = helper.getRequest( );
        HttpServletResponse rsp = helper.getResponse();

        for (Map.Entry<String, Object> et : acts.entrySet( )) {
            Object pms = et.getValue();
            String key = et.getKey(  );

            if ( ! key.startsWith("/")) {
                continue;
            }

            // 解析请求参数
            Map dat;
            if (pms instanceof Map) {
                dat = ( Map )  pms;
            } else {
                String str = pms.toString();
                if (str.startsWith("{") && str.endsWith("}")) {
                    dat = ( Map ) Data.toObject  (str);
                } else {
                    dat = ActionHelper.parseQuery(str);
                }
            }

            // 代理执行动作
            String act = key.substring(1).replace("#.*$", "");
            helper.setRequestData ( dat );
            helper.reply( new HashMap() );
            try {
                req.getRequestDispatcher(act).include(req, rsp);
            } catch (ServletException ex) {
                if (quit) {
                    String msg = ex.getLocalizedMessage();
                    String err = "Er500";
                    helper.fault(msg, err);
                } else {
                    throw new HongsException.Common( ex );
                }
            } catch (IOException ex) {
                if (quit) {
                    String msg = ex.getLocalizedMessage();
                    String err = "Er500";
                    helper.fault(msg, err);
                } else {
                    throw new HongsException.Common( ex );
                }
            }
            rets.put(key, helper.getResponseData());
        }

        helper.reply(rets);
    }

}
