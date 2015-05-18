package app.hongs.action.serv;

import app.hongs.HongsError;
import app.hongs.HongsException;
import app.hongs.action.ActionDriver;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.util.Data;
import app.hongs.util.Synt;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 应用程序接口
 *
 * <p>
 * REST 适配器, 可将不同 Method 请求转发到原有的 Action 方法:<br/>
 * <pre>
 * GET      retrieve, list or info
 * POST     create, save
 * PUT      update, save
 * DELETE   delete
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
  extends  ActionDriver
{

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doAction(req, rsp, "retrieve", "list");
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doAction(req, rsp, "update", "save");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        doAction(req, rsp, "create", "save");
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
        String act = ActionDriver.getCurrPath(req);
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
            act = act.substring(0, mat.start() - 1) + acn;

            // 指定方法
            if (mtz != null && mtz.length() != 0) {
                mtd  = mtz.substring( 2 );
                mts  = new String[] {mtd};
            }

            // 限定主键
            if (vaz != null && vaz.length() != 0) {
                String   key = "id";
                String[] vas = vaz.substring(2).split ("_");
                if (vas.length > 1  ) {
                    key += "[]";
                } else {
                    if ( mtd.equals("retrieve")) {
                         mts = new String[]{"retrieve", "info", "list"};
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

        // 将请求数据处理之后传递
        ActionHelper hlpr = ActionDriver.getWorkCore(req).get(ActionHelper.class);
        String  json = Synt.declare(req.getParameter("-api-data") , String.class);
        if (json != null) {
            try {
                Map send = Synt.declare(Data.toObject(json), Map.class);
                Map data = hlpr.getRequestData();
                data.putAll( send );
            } catch (HongsException ex) {
                throw new ServletException(ex);
            } catch (HongsError er) {
                throw new ServletException(er);
            }
        }

        // 将请求转发到动作处理器
        req.getRequestDispatcher("/"+act+ "/"+mtd+ ".act"+pms ).include(req, rsp);
        hlpr.reinitHelper(req, rsp);

        // 将应答数据格式化后传递
        Map resp  = hlpr.getResponseData();
        if (resp != null) {
            Boolean scok = Synt.declare(req.getParameter("-api-scok"), Boolean.class);
            String  back = Synt.declare(req.getParameter("-api-back"),  String.class);
            String  ccnv = Synt.declare(req.getParameter("-api-conv"),  String.class);
            Set     conv = Synt.declare(ccnv == null ? null : ccnv.split("[\\s\\+]+"), Set.class);

            // 状态总是 200
            if (scok != null && scok) {
                rsp.setStatus(javax.servlet.http.HttpServletResponse.SC_OK);
            }

            // 返回节点
            if (back != null) {
                Map map = new HashMap();
                Iterator it = resp.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry et = (Map.Entry) it.next();
                    Object k = et.getKey();
                    if (! _API_RSP.contains( k )) {
                        map.put(k, et.getValue());
                        it.remove();
                    }
                }
                resp.put(back, map);
            }

            // 转换策略
            if (conv != null) {
                Conv cnvr = new Conv();
                boolean     all =  conv.contains( "all2str");
                cnvr.all  = all ?  new  Conv2Str( ) : new Conv2Obj( ) ;
                cnvr.num  = all || conv.contains( "num2str") ? new Conv2Str(/**/) : cnvr.all;
                cnvr.nul  = all || conv.contains("null2str") ? new ConvNull2Str() : cnvr.all;
                cnvr.bool = conv.contains("bool2str") ? new ConvBool2Str()
                          :(conv.contains("bool2num") ? new ConvBool2Num() : new Conv2Obj());
                cnvr.date = conv.contains("date2mic") ? new ConvDate2Mic()
                          :(conv.contains("date2sec") ? new ConvDate2Sec() : new Conv2Obj());
                hlpr.reply (Synt.foreach(resp, cnvr));
            }
        }
    }

    private static final Pattern _API_PMS = Pattern.compile("((?:/[^_][^/]+/_[^/]+)*)?(/[^_][^/]+)(/_[^/]+)?(/ [^/]+)?$");

    private static final Set _API_RSP = new HashSet();
    static {
        _API_RSP.add("ok" );
        _API_RSP.add("err");
        _API_RSP.add("msg");
    }

    private static class Conv extends Synt.LeafNode {
        private Conv2Obj all;
        private Conv2Obj num;
        private Conv2Obj nul;
        private Conv2Obj bool;
        private Conv2Obj date;
        @Override
        public Object leaf(Object o) {
            if (o == null) {
                return nul.conv(o);
            } else
            if (o instanceof Number ) {
                return num.conv(o);
            } else
            if (o instanceof Boolean) {
                o = bool.conv(o);
            } else
            if (o instanceof Date) {
                o = date.conv(o);
            }
            return   all.conv(o);
        }
    }
    private static class Conv2Obj {
        public Object conv(Object o) {
            return o;
        }
    }
    private static class Conv2Str extends Conv2Obj {
        @Override
        public Object conv(Object o) {
            return o.toString();
        }
    }
    private static class ConvNull2Str extends Conv2Obj {
        @Override
        public Object conv(Object o) {
            return "";
        }
    }
    private static class ConvBool2Str extends Conv2Obj {
        @Override
        public Object conv(Object o) {
            return ((Boolean) o) ? "1" : "";
        }
    }
    private static class ConvBool2Num extends Conv2Obj {
        @Override
        public Object conv(Object o) {
            return ((Boolean) o) ?  1  :  0;
        }
    }
    private static class ConvDate2Mic extends Conv2Obj {
        @Override
        public Object conv(Object o) {
            return ((Date) o).getTime();
        }
    }
    private static class ConvDate2Sec extends Conv2Obj {
        @Override
        public Object conv(Object o) {
            return ((Date) o).getTime() / 1000;
        }
    }

}
