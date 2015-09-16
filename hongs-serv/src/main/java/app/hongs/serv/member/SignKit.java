package app.hongs.serv.member;

import app.hongs.HongsException;
import app.hongs.action.MenuSet;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录工具
 * @author Hongs
 */
public class SignKit {

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
            throw HongsException.common(null, ex);
        }
    }

    private static final byte[] pazz = {'A','B','C','D','E','F','1','2','3','4','5','6','7','8','9','0'};

}
