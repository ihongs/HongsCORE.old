package app.hongs.dl.anno;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.FilterInvoker;
import app.hongs.dl.ITransc;
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
            core.put("__IN_TRANSC_MODE__", true);

            try {
                // 开启
                for(Object o : core.values()) {
                    if (o instanceof ITransc) {
                        ((ITransc) o).transc();
                    }
                }

                chains.doAction();

                // 提交
                for(Object o : core.values()) {
                    if (o instanceof ITransc) {
                        ((ITransc) o).commit();
                    }
                }
            } catch (Exception|Error ex) {
                // 回滚
                for(Object o : core.values()) {
                    if (o instanceof ITransc) {
                        ((ITransc) o).revoke();
                    }
                }

                if (ex instanceof HongsException) {
                    throw  (  HongsException  ) ex;
                } else {
                    throw HongsException.common(null, ex);
                }
            }
        } finally {
            core.remove("__IN_TRANSC_MODE__");
        }
    }
}
