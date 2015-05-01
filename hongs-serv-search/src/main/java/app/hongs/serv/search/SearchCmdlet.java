package app.hongs.serv.search;

import app.hongs.HongsException;
import app.hongs.action.VerifyHelper;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.util.Data;
import app.hongs.util.Synt;
import java.util.Map;

/**
 * 索引服务
 * @author Hongs
 */
@Cmdlet("hongs.search")
public class SearchCmdlet {

    @Cmdlet("index")
    public void index(String[] args) throws HongsException {
        String sn = args[0];
        String id = args[1];

        int    pos = sn.lastIndexOf('/');
        String mod = sn.substring(0,pos);
        String ent = sn.substring(pos+1);

        SearchRecord so = new SearchRecord(mod, ent);

        // 不给内容即为删除
        if (args.length == 2) {
            so.connect();
            so.del(id);
            return;
        }

        Map rd = Synt.declare(Data.toObject(args[2]), Map.class);
        rd.put("id", id);

        // 校验数据
        VerifyHelper vh = new VerifyHelper();
        vh.addRulesByForm(mod, ent);
        rd = vh.verify(rd);

        so.connect();
        so.add( rd );
        so.commit( );
        so.destroy();
    }

}
