package app.hongs.serv;

import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Model;
import java.util.Map;

/**
 * 通用信息模型处理类
 * @author Hongs
 */
public class HcimDomain extends Model {

    public HcimDomain() throws HongsException {
        super(DB.getInstance("hcim").getTable("a_hcim_domain"));
    }

    private void cleanData(Map<String, Object> data) {
        String type  =  data.get("type").toString();
        if ("2".equals(type)) {
            // 数字取值范围
            String min = data.get("min").toString();
            String max = data.get("max").toString();
            String rule = min + "," + max;
            data.put("rule", rule);
        }
        else if ("1".equals(type)) {
            // 字符
            data.put("scale", "0");
            data.put("signed","0");
        }
        else if ("6".equals(type)) {
            // 文本
            data.put("rule" , "" );
            data.put("scale", "0");
            data.put("signed","0");
        }
        else {
            // 其他
            data.put("rule" , "" );
            data.put("size" , "0");
            data.put("scale", "0");
            data.put("signed","0");
        }
    }
    
    @Override
    public String add(Map<String, Object> data) throws HongsException {
        cleanData(data);
        return super.add(data);
    }
    
    @Override
    public int put(String id, FetchCase caze, Map<String, Object> data) throws HongsException {
        cleanData(data);
        return super.put(id, caze, data);
    }
    
}
