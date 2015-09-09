package app.hongs.serv.member;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 用户组会话
 * @author Hongs
 */
public class RoleSes implements Set<String> {

    private Set<String> roles;
    public final String token;
    public final String appid;

    public RoleSes(final String token, String appid) throws HongsException {
        this.roles = new HashSet();
        this.token = token;
        this.appid = appid;

        //** 查询 user_id, 取得 roles **/

        DB        db;
        Table     tb;
        FetchCase fc;
        Map       rs;
        String    id;

        db = DB.getInstance("member");
        tb = db.getTable("user_sign");
        fc = new FetchCase( )
                .from   (tb.tableName, tb.name)
                .where  ("code = ? AND type = ?", token, appid)
                .select ("user_id");
        rs = db.fetchLess(fc);
        if (rs.isEmpty()) {
            return;
        }

        id = (String)rs.get("user_id");
        roles = new RoleSet(id).roles ;
    }

    public static RoleSes getInstance() throws HongsException {
        ActionHelper   helper = Core.getInstance(ActionHelper.class);
        String token = helper.getParameter("-token");
        String appid = helper.getParameter("-appid");

        if (token == null || "".equals(token)) {
            token = (String) helper.getSessvalue("sign_code");
            appid = (String) helper.getSessvalue("sign_type");
        }

        if (token == null || "".equals(token)) {
            token = (String) helper.getAttribute("sign_code");
            appid = (String) helper.getAttribute("sign_type");
        }

        if (token == null || "".equals(token)) {
            return   null;
        }

        return new RoleSes(token, appid);
    }

    //** Set 相关操作 **/

    @Override
    public int size() {
        return roles.size();
    }

    @Override
    public boolean isEmpty() {
        return roles.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return roles.contains(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        return roles.containsAll(c);
    }

    @Override
    public boolean add(String e) {
        return roles.add(e);
    }

    @Override
    public boolean addAll(Collection c) {
        return roles.addAll(c);
    }

    @Override
    public boolean remove(Object o) {
        return roles.remove(o);
    }

    @Override
    public boolean removeAll(Collection c) {
        return roles.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        return roles.retainAll(c);
    }

    @Override
    public void clear() {
        roles.clear();
    }

    @Override
    public Iterator iterator() {
        return roles.iterator();
    }

    @Override
    public Object[] toArray() {
        return roles.toArray( );
    }

    @Override
    public Object[] toArray(Object[] a) {
        return roles.toArray(a);
    }

}
