package app.hongs.action.anno;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.dl.ITrnsct;
import java.lang.annotation.Annotation;

/**
 * 操作成功才提交数据更改
 *
 * 由Action.doAction自动调用
 *
 * @author Hongs
 */
public class CommitInvoker implements FilterInvoker {
    @Override
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno)
    throws HongsException {
        Core core = Core.getInstance();

        try {
            core.put("__IN_TRNSCT_MODE__", true);

                // 开启
                for(Object o : core.values()) {
                    if (o instanceof ITrnsct) {
                        ((ITrnsct) o).trnsct();
                    }
                }

            try {
                chains.doAction();

                // 提交
                for(Object o : core.values()) {
                    if (o instanceof ITrnsct) {
                        ((ITrnsct) o).commit();
                    }
                }
            } catch (Exception|Error ex) {
                // 回滚
                for(Object o : core.values()) {
                    if (o instanceof ITrnsct) {
                        ((ITrnsct) o).rolbak();
                    }
                }

                if (ex instanceof HongsException) {
                    throw (HongsException) /***/ ex ;
                }
                throw new  HongsException.Common(ex);
            }
        } finally {
            core.remove("__IN_TRNSCT_MODE__");
        }
    }
}
