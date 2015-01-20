package app.hongs.action.serv;

import app.hongs.action.ActionWarder;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.util.Synt;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 应用程序接口
 *
 * <p>
 * REST 适配器, 可将不同 Method 请求转发到原有的 Action 方法:<br/>
 * <pre>
 * GET      List, Tree or Info
 * PUT      Modify, Save
 * POST     Create, Save
 * DELETE   Remove
 * </pre>
 * </p>
 *
 * <h3>web.xml配置:</h3>
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;ApisAction&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;app.hongs.action.ApisAction&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;ApisAction&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;*.api&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 *
 * @author Hongs
 */
public class ApisAction
  extends HttpServlet
{

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doAction(req, rsp, "retrieve", "list");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doAction(req, rsp, "create", "save");
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doAction(req, rsp, "update", "save");
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doAction(req, rsp, "delete");
    }

    /**
     * 将请求转发到动作处理器
     * @param req
     * @param rsp
     * @param mts
     * @throws ServletException
     * @throws IOException
     */
    private void doAction(HttpServletRequest req, HttpServletResponse rsp, String... mts)
            throws ServletException, IOException {
        String act = ActionWarder.getCurrPath(req);
        String mtd = mts[0];

        if (act == null || act.length() == 0) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "API URI can not be empty.");
            return;
        }

        // 去掉扩展名
        act = act.substring(1);
        int pos;
            pos = act.lastIndexOf('.');
        if (pos > -1)
            act = act.substring(0,pos);

        /**
         * 映射规则:
         * /module/assoc/_{assocId}/model/_{modelId}/.{action}.api
         * /module/model/action.act?assoc_id={assocId}&id={modelId}
         * 其中 /assoc/_{assocId} 可以有零或多组; _{modelId} 可以有零或多个;
         * .{action} 可以省略, 默认以 Http 的 Method 来判断
         */
        StringBuilder pms = new StringBuilder();
        Matcher mat = _API_PMS.matcher("/"+act);
        if (mat.find()) {
            String pmz = mat.group(1);
            String acn = mat.group(2);
            String vaz = mat.group(3);
            String mtz = mat.group(4);

            // 指定资源
            act = act.substring(0, mat.start()) + acn;

            // 指定方法
            if (mtz != null && mtz.length() != 0) {
                mtd  = mtz.substring( 2 );
                mts  = new String[] {mtd};
            }

            // 限定主键
            if (vaz != null && vaz.length() != 0) {
                String   key = "id";
                String[] vas = vaz.substring(2).split("_");
                if (vas.length > 1  ) {
                    key += "[]";
                } else {
                    if ( mtd.equals("retrieve")) {
                         mts = new String[]{"info", "retrieve", "list"};
                    }
                }
                for(String val : vas) {
                    pms.append("&").append(key).append("=").append(val);
                }
            }

            // 限定外键
            if (pmz != null && pmz.length() != 0) {
                String[] pns = pmz.substring(1).split("/");
                for(int i = 0; i < pns.length; i += 2) {
                    String   key = pns[i]+"_id";
                    String[] vas = pns[i + 1 ].substring(1).split ("_");
                    if (vas.length > 1  ) {
                        key += "[]";
                    }
                    for(String val : vas) {
                        pms.append("&").append(key).append("=").append(val);
                    }
                }
            }

            // QueryString 必须以 ? 开头才行
            if (pms.length ( )  >  0 ) {
                pms.replace(0, 1, "?");
            }
        }

        for (String mtn : mts) {
            if (ActionRunner.getActions().containsKey(act+"/"+mtn)) {
                mtd  = mtn ;
                break;
            }
        }

        // 将请求转发到动作处理器
        req.getRequestDispatcher("/"+act+"/"+mtd+".act"+pms).include(req, rsp);

        // 将应答数据格式化后传递
        ActionHelper hlpr = ActionWarder.getWorkCore(req)
                         .get(ActionHelper.class);
        Map  data  = hlpr.getResponseData();
        if ( data != null ) {
             hlpr.reply(Synt.foreach(data, new Synt.LeafNode() {
                public Object leaf(Object val) {
                    if (val == null) {
                        return "";
                    } else if (val instanceof Boolean) {
                        return ((Boolean) val) ? "1" : "0";
                    } else {
                        return val.toString( );
                    }
                }
             }));
        }
    }

    private static final Pattern _API_PMS = Pattern.compile("((?:/[^_]\\w+/_\\w+)*)?(/[^_]\\w+)(/_\\w+)?(/\\.\\w+)?$");

}
