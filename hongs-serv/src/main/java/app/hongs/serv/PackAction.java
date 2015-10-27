package app.hongs.serv;

import app.hongs.HongsCause;
import app.hongs.HongsError;
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

        for (Map.Entry<String, Object> ent : acts.entrySet()) {
            Object pms = ent.getValue();
            String uri = ent.getKey(  );

            if(!uri.startsWith("/") || !uri.endsWith(".act")) {
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
            helper.setRequestData ( dat );
            helper.reply( new HashMap() );
            try {
                req.getRequestDispatcher(uri).include(req, rsp);
            } catch (ServletException ex) {
                if (ex.getCause() instanceof HongsCause) {
                    HongsCause ez  = (HongsCause) ex.getCause();
                    if (quit) {
                        String msg =  ez.getLocalizedMessage ();
                        String err = "Ex" + Integer.toHexString(ez.getCode());
                        helper.fault(msg, err);
                    } else if (ez instanceof HongsError) {
                        throw (HongsError    ) ez;
                    } else{
                        throw (HongsException) ez;
                    }
                } else
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
            rets.put(uri, helper.getResponseData());
        }

        helper.reply(rets);
    }

}
