package app.hcim.model;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.DataConfig;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.util.Tree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用信息模型处理类
 * @author Hongs
 */
public class Entity extends AbstractBaseModel {

    public Entity() throws HongsException {
        super("hcim", "a_hcim_entity");
    }

    @Override
    public String add(Map<String, Object> info)
    throws HongsException {
        String id = super.add (info);
        this.updateEntityTable( id );
        return id;
    }

    @Override
    public String put(String id, Map<String, Object> info)
    throws HongsException {
        id = super.put ( id , info );
        this.updateEntityTable( id );
        return id;
    }

    @Override
    public int del(String id, FetchCase caze)
    throws HongsException {
        int rn = super.del(id, caze);
        this.removeEntityTable( id );
        return rn;
    }

    public void updateEntityTable(String id)
    throws HongsException {
        FetchCase caze;
        Map       info;
        DB        dd;
        String    dn, tn, xn;
        StringBuilder sb;

        caze = new FetchCase(   );
        info = this.get(id, caze);
        xn   = Core.getUniqueId();
        xn   = "x_hcxm_" + xn;
        tn   = "a_hcxm_" + id;
        dn   = "hcxm/" + info.get("module_id").toString();
        //dd   = DB.getInstance(dn);
        sb   =new StringBuilder();
        
        sb.append("CREATE TABLE `").append(xn).append("` (");
        sb.append("  `id` CHAR(20) NOT NULL,\r\n");
        
        // 提取属性类型
        DataConfig dc = DataConfig.getInstance( "hcim" );
        List dl = (List) dc.getDataByKey("DOMAIN_TYPES");
        Map  dm = new HashMap();
        for (Object oo : dl) {
            List di = (List) oo;
            dm.put(di.get(0).toString(), di.get(2).toString());
        }
        
        // 组织建表语句
        List<Map> cols = (List) info.get("a_hcim_entity_cols");
        if (cols == null) cols = new ArrayList();
        for(Map col : cols) {
            Map dom = (Map) col.get("a_hcim_domain");
            if (dom == null) dom = new HashMap();
            
            String cm = (String) col.get("name");
            if (cm == null) {
                cm = (String) dom.get("name");
            }
            
            String fn = (String) dom.get( "id" );
            String ft = (String) dom.get("type");
            
            int size = Integer.parseInt(dom.get("size"  ).toString());
            int scale = Integer.parseInt(dom.get("scale" ).toString());
            int signed = Integer.parseInt(dom.get("signed").toString());
            int required = Integer.parseInt(col.get("required").toString());
            
            sb.append("  `").append(fn).append("` ").append(dm.get(ft));
            
            if (size > 0) {
                if (scale > 0) {
                    sb.append("(").append(size).append(",").append(scale).append(")");
                } else {
                    sb.append("(").append(size).append(")");
                }
            }
            
            if (signed == 0) {
                sb.append(" UNSIGNED");
            }
            
            if (required != 0) {
                sb.append(" NOT NULL");
            } else {
                sb.append(" NULL");
            }
            
            sb.append(",\r\n");
        }
        
        sb.append("  PRIMARY KEY (`id`)\r\n");
        sb.append(")");
        
        System.out.println(sb);
    }

    public void removeEntityTable(String id)
    throws HongsException {
        FetchCase caze;
        Map       info;
        DB        dd;
        String    dn, tn, sql;

        caze = new FetchCase(   );
        caze.select(".module_id");
        info = this.get(id, caze);
        dn   =   "hcxm/" + info.get("module_id").toString();
        tn   = "a_hcxm_" + id;
        dd   = DB.getInstance(dn);
        sql  = "DROP TABLE `" + tn + "`";

        dd.execute(sql);
    }

}
