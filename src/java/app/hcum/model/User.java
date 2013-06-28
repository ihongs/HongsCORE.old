package app.hcum.model;

import app.hongs.HongsException;
import app.hongs.action.ActionConfig;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.FetchBean;
import app.hongs.db.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户基础信息模型
 * @author Hongs
 */
public class User
extends AbstractBaseModel {

    public User()
    throws HongsException {
        super("hcum", "au_user_base_info");
    }

    public Set<String> getGroups(String userId)
    throws HongsException {
        if (userId == null) throw new HongsException(0x10000, "User Id required!");

        Table asoc = this.db.getTable("au_user_group_asoc");
        FetchBean fa = new FetchBean();
        fa.setSelect(".group_name")
          .setWhere(".user_id = ?", userId);

        List<Map> rows = asoc.fetchMore(fa);
        Set<String> groups = new HashSet( );
        for (Map row : rows) {
            groups.add((String)row.get("group_name"));
        }

        return groups;
    }

    public static List getPageGroups(String name)
    throws HongsException {
        List pageGroups = new ArrayList();
        ActionConfig ac = new ActionConfig(name);

        Map<String, Map> pages1 = ac.pages;
        for (Map.Entry et1 : pages1.entrySet()) {
            Map page1 = (Map)et1.getValue();
            if (!page1.containsKey("pages")) {
                continue;
            }

            Map<String, Map> pages2 = (Map<String, Map>)page1.get("pages");
            for (Map.Entry et2 : pages2.entrySet()) {
                Map page2 = (Map)et2.getValue();
                if (!page2.containsKey("pages")) {
                    continue;
                }

            // 读取第2层的页面信息
            Map page_a = page2;
            Map page_b = new HashMap();
            List pages = new ArrayList();
            page_b.put("uri" , page_a.get("uri" ));
            page_b.put("name", page_a.get("name"));
            page_b.put("pages", pages);
            pageGroups.add(page_b);

                Map<String, Map> pages3 = (Map<String, Map>)page2.get("pages");
                for (Map.Entry et3 : pages3.entrySet()) {
                    Map page3 = (Map)et3.getValue();
                    if (!page3.containsKey("groups")) {
                        continue;
                    }

            // 读取第3层的页面信息
            Map page_c = page3;
            Map page_d = new HashMap();
            page_d.put("uri" , page_c.get("uri" ));
            page_d.put("name", page_c.get("name"));
            pages.add(page_d);

            // 读取页面分组信息
            List groups = new ArrayList();
            page_d.put("groups",  groups);
            Set<String> groupz = (Set)page_c.get("groups");
            for (String k : groupz) {
                Map group1 = ac.getGroup(k);
                Map group2 = new HashMap( );
                groups.add( group2 );
                group2.put("key" , group1.get("key" ));
                group2.put("name", group1.get("name"));
                group2.put("groups", ac.getAllGroups(k).keySet());
            }

                }
            }
        }

        return pageGroups;
    }

    @Override
    protected void getFilter(Map req, FetchBean fa)
    throws HongsException {
        super.getFilter(req, fa);

        /**
         * 如果有指定dept_id
         * 则关联a_user_detp_asoc来约束范围
         */
        if (req.containsKey("dept_id")) {
            fa.join("au_user_dept_asoc",
                    ".user_id = :user_id")
              .where("dept_id = ?", req.get("dept_id"));
        }
    }

}
