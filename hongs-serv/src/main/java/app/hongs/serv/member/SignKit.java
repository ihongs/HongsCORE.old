package app.hongs.serv.member;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.MenuSet;
import app.hongs.db.DB;
import app.hongs.db.Table;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpSession;

/**
 * 登录工具
 * @author Hongs
 */
public class SignKit {

    private static final byte[] pazz = {'A','B','C','D','E','F','1','2','3','4','5','6','7','8','9','0'};

    /**
     * 获取特征加密字符串
     * @param pswd
     * @return
     * @throws HongsException
     */
    public static String getCrypt(String pswd) throws HongsException {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] pzwd = m.digest(pswd.getBytes());
            byte[] pxwd = new byte[pzwd.length * 2];
            int i = 0, j = 0;
            for ( ; i < pzwd.length; i ++) {
                byte pzbt = pzwd[i];
                pxwd[j++] = pazz[pzbt >>> 4 & 0xf ];
                pxwd[j++] = pazz[pzbt /***/ & 0xf ];
            }
            return new String(pxwd);
        } catch (NoSuchAlgorithmException ex) {
            throw new HongsException.Common(ex);
        }
    }

    /**
     * 获取 menu 配置里的 role 集合
     * @param name
     * @return
     * @throws HongsException
     */
    public static List getRoles(String name) throws HongsException {
        List units = new ArrayList();
        MenuSet ac = new MenuSet(name);
        Map<String, Map> menus1 = ac.menus;

        for(Map.Entry et1 : menus1.entrySet()) {
            Map menu1 = (Map) et1.getValue();
            if (!menu1.containsKey("menus")) {
                continue;
            }

            Map<String, Map> menus2 = (Map<String, Map>) menu1.get("menus");
            for(Map.Entry et2 : menus2.entrySet()) {
                Map menu2 = (Map) et2.getValue();
                if (!menu2.containsKey("menus")) {
                    continue;
                }

                Map menu_a = menu2;
                Map menu_b = new HashMap();
                List menus = new ArrayList();
                menu_b.put("href", (String) et2.getKey());
                menu_b.put("disp", menu_a.get("disp"));
                menu_b.put("menus", menus);
                units.add(menu_b);

                Map<String, Map> menus3 = (Map<String, Map>) menu2.get("menus");
                for(Map.Entry et3 : menus3.entrySet()) {
                    Map menu3 = (Map) et3.getValue();
                    if (!menu3.containsKey("roles")) {
                        continue;
                    }

                    Map menu_c = menu3;
                    Map menu_d = new HashMap();
                    List roles = new ArrayList();
                    menu_b.put("href", (String) et3.getKey());
                    menu_d.put("disp", menu_c.get("disp"));
                    menu_d.put("roles", roles);
                    menus.add(menu_d);

                    Set<String> rolez = (Set) menu_c.get("roles");
                    for(String k : rolez) {
                        Map role1 = ac.getRole(k);
                        Map role2 = new HashMap();
                        roles.add(role2);
                        role2.put("name", role1.get("name"));
                        role2.put("disp", role1.get("disp"));
                        role2.put("roles", ac.getMoreRoles(k).keySet());
                    }
                }
            }
        }

        return units;
    }

    /**
     * 自运营登录
     * @param ah
     * @param appid
     * @param usrid
     * @param uname
     * @return
     * @throws HongsException
     */
    public static Map  userSign(ActionHelper ah, String appid, String usrid, String uname)
    throws HongsException {
        HttpSession sd    = ah.getRequest().getSession(true);
        String      sesid = sd.getId();
        long        stime = System.currentTimeMillis()/1000 ;

        // 设置会话
        sd.setAttribute(  "uid", usrid);
        sd.setAttribute("appid", appid);
        sd.setAttribute("stime", stime);
        sd.setAttribute("uname", uname);

        // 返回数据
        Map rd = new HashMap();
        rd.put(  "uid", usrid);
        rd.put("appid", appid);
        rd.put("ssnid", sesid);
        rd.put("stime", stime);
        rd.put("uname", uname);

        // 记录登录
        DB    db = DB.getInstance("member");
        Table tb = db.getTable("user_sign");
        tb.delete("(`user_id` = ? AND `appid` = ?) OR `sesid` = ?", usrid, appid, sesid);
        Map ud = new HashMap();
        ud.put("user_id", usrid);
        ud.put("appid", appid);
        ud.put("sesid", sesid);
        ud.put("ctime", stime);
        tb.insert(ud);

        return rd;
    }

    /**
     * 第三方登录
     * @param ah
     * @param appid
     * @param opnid
     * @param uname
     * @return
     * @throws HongsException
     */
    public static Map  openSign(ActionHelper ah, String appid, String opnid, String uname)
    throws HongsException {
        DB    db = DB.getInstance("member");
        Table tb = db.getTable("user_open");
        Map   ud = tb.fetchCase()
                     .where ("`opnid` =? AND `appid` = ?", opnid, appid)
                     .select("user_id")
                     .one   (   );

        String usrid;
        if (ud != null && !ud.isEmpty()) {
            usrid = ud.get("user_id").toString();

            ud  =  new HashMap( );
            ud.put("name", uname);
            db.getModel("user").put( ud, usrid );
        } else {
            ud  =  new HashMap( );
            ud.put("name", uname);
            usrid = db.getModel("user").add(ud );

            ud  =  new HashMap( );
            ud.put("appid",appid);
            ud.put("opnid",opnid);
            ud.put("user_id",usrid);
            db.getTable("user_open").insert(ud );
        }

        return userSign(ah, appid, usrid, uname);
    }

}
