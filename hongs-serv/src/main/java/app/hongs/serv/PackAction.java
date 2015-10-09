package app.hongs.serv;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.Action;
import app.hongs.util.Data;
import app.hongs.util.Synt;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用打包动作
 * 可一次调用多个动作
 * 批量执行后返回数据
 * 请求格式举例:
 * pack.ACTION/NAME=PARAMS&pack.ACTION/NAME2#0=PARAMS&pack.ACTION/NAME2#1=PARAMS&quit=1
 * PARAMS 需要 urlencode 编码
 * @author Hongs
 */
@Action("common/pack")
public class PackAction {

    @Action("exec")
    public void pack(ActionHelper helper)
    throws HongsException {
        Map<String, Object> acts = Synt.declare(helper.getRequestData().get("pack"), Map.class);
        if (acts == null) {
            helper.fault("Param 'pack' is not exists!");
            return;
        }
        boolean quit = Synt.declare(helper.getRequestData().get("quit"), false);

        Map<String, Object> data = new HashMap();
        Map<String, Object> rets = new HashMap();
        data.put ( "pack" , rets );

        for (Map.Entry<String, Object> et : acts.entrySet()) {
            String key = et.getKey(  );
            Object pms = et.getValue();
            String act = key.replaceFirst("(\\.act|\\.api)?#.*$", key);

            // 解析请求参数
            Map dat;
            if (pms instanceof Map) {
                dat = (Map)  pms;
            } else {
                String str = pms.toString();
                if (str.startsWith("{") && str.endsWith("}")) {
                    dat = ( Map ) Data.toObject  (str);
                } else {
                    dat = ActionHelper.parseQuery(str);
                }
            }

            // 代理执行动作
            ActionHelper hlp = helper.clone(   );
                         hlp.setRequestData(dat);
            ActionRunner run = new ActionRunner(act, hlp);
            try {
                run.doAction();
            } catch (HongsException ex) {
                if (quit) {
                    String msg = ex.getLocalizedMessage();
                    String err ="Ex"+Integer.toHexString(ex.getCode());
                    hlp.fault(msg, err);
                } else {
                    throw ex;
                }
            }
            rets.put(key, hlp.getResponseData());
        }

        helper.reply(data);
    }

}
