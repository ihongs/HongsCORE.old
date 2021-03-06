package app.hongs.action.anno;

import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.NaviMap;
import java.lang.annotation.Annotation;

/**
 * 权限过滤处理器
 * @author Hongs
 */
public class PermitInvoker implements FilterInvoker {

    @Override
    public void invoke(ActionHelper helper, ActionRunner chains, Annotation anno) throws HongsException {
        Permit ann  = (Permit) anno;
        String role = ann.role();
        String conf = ann.conf();

        NaviMap map = NaviMap.getInstance( conf );
        boolean has;
        if (null == role || "".equals(role)) {
            has = map.chkAuth(chains.getAction());
        } else {
            has = map.chkRole(role);
        }

        if (has) {
            chains.doAction();
        } else {
            throw new HongsException(0x1103);
        }
    }

}
