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
        if (act == null || act.length() == 0) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "API URI can not be empty.");
            return;
        }

        // 去前后缀
        String acl = act.substring( 1 );
        int pos  = acl.lastIndexOf('.');
        if (pos != -1) {
            acl  = acl.substring(0,pos);
        }

        // 解析路径
        Map acx = ActionRunner.getActions();
        if (acx.containsKey(acl) == false ) {
            act =  parseAct(acx, acl, mts );
        } else {
            act =  "/" + acl + ".act";
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
        req.getRequestDispatcher(act).include(req, rsp);
        hlpr.reinitHelper(req, rsp);

        // 将应答数据格式化后传递
        Map resp  = hlpr.getResponseData();
        if (resp != null) {
            Boolean scok = Synt.declare(req.getParameter("-api-scok"), Boolean.class);
            String  back = Synt.declare(req.getParameter("-api-wrap"),  String.class);
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

    private String parseAct(Map acx, String act, String... mts) {
        String[] ats = act.split("/");
        String   m   = mts[0];
        String   n   = ats[0];
        StringBuilder u = new StringBuilder();
        StringBuilder p = new StringBuilder();

        u.append(n);
        for(int i = 1; i < ats.length; i ++ ) {
            String  v = ats[i];
            if (v.startsWith(".")) {
                String[] a = v.substring(1).split("\\.");
                if (a.length == 0) {
                    continue;
                }

                /**
                 * 最后一个总是叫 id
                 * 且如果只有一个 id
                 * 则把 info 放到候选方法名列表里
                 * 并比 list 优先匹配
                 */
                if (i == ats.length - 1) {
                    if  (  a.length < 2 &&  "retrieve".equals(m )  )  {
                        mts = new String[] {"retrieve", "info", "list"};
                    }
                    n = "id";
                }

                if (a.length > 1) {
                    n += ".";
                }
                for(String x : a) {
                    p.append('&').append(n).append('=').append(x);
                }
            } else {
                n=v;u.append('/').append(n);
            }
        }

        n = u.toString(/**/);
        for (String x : mts) {
            if (acx.containsKey(n+"/"+x)) {
                m = x;
                break;
            }
        }

        if (p.length () > 0) {
            p.replace(0, 1, "?");
        }

        return "/"+ n +"/"+ m + ".act" + p.toString();
    }

    private static final Set _API_RSP = new HashSet();
    static {
        _API_RSP.add("ok" );
        _API_RSP.add("err");
        _API_RSP.add("msg");
    }

    private static class Conv extends Synt.LeafNode {
        private Conv2Obj all;
        private Conv2Obj nul;
        private Conv2Obj num;
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
