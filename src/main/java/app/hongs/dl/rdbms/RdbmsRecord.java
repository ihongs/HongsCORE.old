/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package app.hongs.dl.rdbms;

import app.hongs.HongsException;
import app.hongs.action.StructConfig;
import app.hongs.db.FetchCase;
import app.hongs.util.Tree;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Hongs
 */
public class RdbmsRecord {

    private String module;
    private String entity;
    private Map form;

    public RdbmsRecord(String module, String entity) throws HongsException {
        this.module = module;
        this.entity = entity;
        this.form = StructConfig.getInstance(module).getForm(entity);
    }

    protected void filter(String module, String entity, FetchCase caze, Map rd) {
        Iterator it = rd.entrySet( ).iterator( );
        while (it.hasNext()) {
            Map.Entry et = (Map.Entry) it.next();
            String k = et.getKey().toString();
            Object v = et.getValue();
            if ("wd".equals(k)) {

            } else
            if ("ob".equals(k)) {
                if (v instanceof Map) {

                }
            } else
            if ("cb".equals(k)) {

            }
        }
    }

    protected void permit(String module, String entity, FetchCase caze, String id) {

    }

    private void colsFilter(FetchCase caze, Map ob) {
        Set<String> cols = new HashSet();

        Iterator it = ob.entrySet( ).iterator( );
        while (it.hasNext()) {
            Map.Entry et = (Map.Entry) it.next();
            String k = et.getKey().toString();
            if (!form.containsKey(k)) {
                continue;
            }

            Object o = et.getValue();
            short  d = 1;
            if (o instanceof Number) {
                d = (Short) d;
            } else
            if (o instanceof String) {
                d = Short.parseShort((String) o);
            }

            if (d > 0) {
                cols.add(k);
            } else {
                if (cols.isEmpty()) {
                    Iterator it2 = form.entrySet( ).iterator();
                    while (it2.hasNext()) {
                        Map.Entry et2 = (Map.Entry) it2.next();
                        String k2 = et2.getKey().toString();
                        Map    m2 = (Map) et2.getValue();
                        String t2 = (String) m2.get("_type");
                        if ("form".equals(t2)) {
                            continue;
                        }
                        cols.add(k2);
                    }
                }
                cols.remove(k);
            }
        }

        for (String col : cols) {
            caze.select(".`"+col+"`");
        }
    }

    private void sortFilter(FetchCase caze, Map ob) {
        Iterator it = ob.entrySet( ).iterator( );
        while (it.hasNext()) {
            Map.Entry et = (Map.Entry) it.next();
            String k = et.getKey().toString();
            if (!form.containsKey(k)) {
                continue;
            }

            Object o = et.getValue();
            short  d = 1;
            if (o instanceof Number) {
                d = (Short) d;
            } else
            if (o instanceof String) {
                d = Short.parseShort((String) o);
            }

            caze.orderBy(".`"+k+"` "+(d > 0 ? "ASC" : "DESC"));
        }
    }

    private FetchCase join(FetchCase caze, String name, Map params) {
        FetchCase caze2 = caze.getJoin(name);
        if (caze2 != null) {
            return caze2;
        }

        String fnam = Tree.getValue(params, "", "form");
        String link = Tree.getValue(params, "", "link");
        String type = Tree.getValue(params, "BLS_TO", "type");

        return null;
    }

}
