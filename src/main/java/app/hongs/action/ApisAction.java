package app.hongs.action;

import app.hongs.util.Data;
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
 * <pre>
 *
 * @author Hongs
 */
public class ApisAction
  extends HttpServlet
{

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doForward(req, rsp, "list", "tree");
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doForward(req, rsp, "modify", "save");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doForward(req, rsp, "create", "save");
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doForward(req, rsp, "remove");
    }

    /**
     * 将请求转发到动作处理器
     * @param req
     * @param rsp
     * @param mts
     * @throws ServletException
     * @throws IOException
     */
    private void doForward(HttpServletRequest req, HttpServletResponse rsp, String... mts)
            throws ServletException, IOException {
        String act = req.getServletPath().substring(1);

        if (act == null || act.length() == 0) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "API uri can not be empty.");
            return;
        }

        // 去掉扩展名
        act  = act.substring(0 , act.lastIndexOf('.'));
        String mtd = mts[0];

        /**
         * 映射规则:
         * /module/assoc/0{assocId}/model/0{modelId}/_{action}.api
         * /module/model/action.act?assoc_id={assocId}&id={modelId}
         * 其中 /assoc/0{assocId} 可以有零或多组; 0{modelId} 可以有零或多个;
         * _{action} 可以省略, 默认以 Http 的 Method 来判断
         */
        StringBuilder pms = new StringBuilder();
        Matcher mat = _API_PMS.matcher("/"+act);
        if (mat.find()) {
            String pmz = mat.group(1);
            String acn = mat.group(2);
            String vaz = mat.group(3);
            String mtz = mat.group(4);

            // 指定资源
            act = act.substring(0, mat.start() - 1) + acn;

            // 指定方法
            if (mtz != null && mtz.length() != 0) {
                mtd  = mtz.substring( 2 );
                mts  = new String[] {mtd};
            } else
            if (mtd.equals("list")
            &&  vaz != null && vaz.length() != 0) {
                mtd  =     "info";
                mts  = new String[] {mtd};
            }

            // 限定主键
            if (vaz != null && vaz.length() != 0) {
                String   key = "id";
                String[] vas = vaz.substring(2).split ("_");
                if (vas.length >  1 ) {
                    key += "[]";
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
                    if (vas.length >  1 ) {
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

        do {
            // 获取动作类
            Class cls = ActionCaller.getActions().get(act);
            if (cls == null) {
                break;
            }

            // 获取动作方法
            for (String mtn : mts) {
                try {
                    cls.getMethod("action_"+mtn, new Class[] {ActionHelper.class});
                    mtd = mtn;
                    break;
                } catch (NoSuchMethodException ex) {
                } catch (    SecurityException ex) {
                }
            }
        } while (false);

        boolean send = false;
        if (req.getAttribute(ActionLoader.DONT_SEND) == null) {
            req.setAttribute(ActionLoader.DONT_SEND  ,  true);
            send = true;
        }

        // 将请求转发到动作处理器
        req.getRequestDispatcher("/"+act+"/"+mtd + ".act" + pms).include(req, rsp);

        if (rsp.getStatus( ) == HttpServletResponse.SC_OK && send) {
            Map data  = (Map) req.getAttribute(ActionLoader.SEND_DATA);
            if (data != null) {
                rsp.setContentType("application/json");
                rsp.getWriter( ).print(Data.toString(data , true));
            }
        }
    }

    private static final Pattern _API_PMS = Pattern.compile("((?:/[^0]\\w+/0\\w+)*)?(/[^0]\\w+)(/0\\w+)?(/_\\w+)?$");

}
