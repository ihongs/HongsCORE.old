package app.hongs.db.serv;

import app.hongs.HongsException;
import app.hongs.action.SiteMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色工具
 * @author Hongs
 */
public class HcumRole {

    public static List getRoles(String name) throws HongsException {
        List units = new ArrayList();
        SiteMap ac = new SiteMap(name);
        Map<String, Map> pages1 = ac.pages;

        for(Map.Entry et1 : pages1.entrySet()) {
            Map page1 = (Map) et1.getValue();
            if (!page1.containsKey("pages")) {
                continue;
            }

            Map<String, Map> pages2 = (Map<String, Map>) page1.get("pages");
            for(Map.Entry et2 : pages2.entrySet()) {
                Map page2 = (Map) et2.getValue();
                if (!page2.containsKey("pages")) {
                    continue;
                }

                Map page_a = page2;
                Map page_b = new HashMap();
                List pages = new ArrayList();
                page_b.put("href", (String) et2.getKey());
                page_b.put("disp", page_a.get("disp"));
                page_b.put("pages", pages);
                units.add(page_b);

                Map<String, Map> pages3 = (Map<String, Map>) page2.get("pages");
                for(Map.Entry et3 : pages3.entrySet()) {
                    Map page3 = (Map) et3.getValue();
                    if (!page3.containsKey("roles")) {
                        continue;
                    }

                    Map page_c = page3;
                    Map page_d = new HashMap();
                    List roles = new ArrayList();
                    page_b.put("href", (String) et3.getKey());
                    page_d.put("disp", page_c.get("disp"));
                    page_d.put("roles", roles);
                    pages.add(page_d);

                    Set<String> rolez = (Set) page_c.get("roles");
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
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    private static byte[] pazz = {'8','9','A','B','C','D','E','F','0','1','2','3','4','5','6','7'};

}
