package app.hongs.db.serv;

import app.hongs.HongsException;
import app.hongs.action.SiteMap;
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
                        Map group1 = ac.getRole(k);
                        Map group2 = new HashMap();
                        roles.add(group2);
                        group2.put("name", group1.get("name"));
                        group2.put("disp", group1.get("disp"));
                        group2.put("roles", ac.getMoreRoles(k).keySet());
                    }
                }
            }
        }
        
        return units;
    }

}
