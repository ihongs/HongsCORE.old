package app.hongs.dl.search;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.VerifyHelper;
import app.hongs.cmdlet.CmdletHelper;
import app.hongs.cmdlet.anno.Cmdlet;
import app.hongs.util.Data;
import app.hongs.util.Synt;
import java.util.List;
import java.util.Map;

/**
 * 索引命令
 * @author Hongs
 */
@Cmdlet("hongs.search")
public class SearchCmdlet {

    @Cmdlet("search")
    public void search(String[] args) throws HongsException {
        Map opts = CmdletHelper.getOpts(args, new String[ ] {
            "conf=s",
            "name=s",
            "id*s",
            "wd*s",
            "rb*s",
            "ob*s",
            "pn:i",
            "gn:i",
            "rn:i"
        });
        
        String conf = Synt.declare(opts.remove("conf"), String.class);
        String name = Synt.declare(opts.remove("name"), String.class);
        ActionHelper ah = Core.getInstance(ActionHelper.class);
        SearchRecord so = new SearchRecord(conf, name);
        Map req = ah.getRequestData();
        req.putAll(opts);
        Map rsp = so.retrieve ( req );
        
        Data.dumps(rsp );
    }
    
    @Cmdlet("update")
    public void update(String[] args) throws HongsException {
        Map opts = CmdletHelper.getOpts(args, new String[ ] {
            "conf=s",
            "name=s",
            "id*s",
        });

        String conf = Synt.declare(opts.remove("conf"), String.class);
        String name = Synt.declare(opts.remove("name"), String.class);
        List<String> ds = Synt.declare(opts.remove("id"), List.class);
        ActionHelper ah = Core.getInstance(ActionHelper.class);
        SearchRecord so = new SearchRecord(conf, name);
        Map  rd = ah.getRequestData();

        if (!rd.isEmpty()) {
            // 有数据则校验数据
            VerifyHelper vh = new VerifyHelper();
            vh.addRulesByForm(conf, name);
            rd = vh.verify(rd);
            
            try {
                so.trnsct( );
                for (String id  : ds) {
                    so.set( id  , rd);
                }
                so.commit( );
            }
            catch (HongsException ex) {
                so.rolbak();
                throw ex;
            }
            finally {
                so.destroy();
            }
        }
        else {
            // 不给内容即为删除
            try {
                so.trnsct( );
                for (String id  : ds) {
                    so.del( id );
                }
                so.commit( );
            }
            catch (HongsException ex) {
                so.rolbak( );
                throw ex;
            }
            finally {
                so.destroy();
            }
        }
    }

}
