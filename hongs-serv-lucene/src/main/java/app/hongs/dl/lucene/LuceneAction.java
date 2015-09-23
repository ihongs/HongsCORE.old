package app.hongs.dl.lucene;

import app.hongs.CoreLocale;
import app.hongs.HongsException;
import app.hongs.action.ActionHelper;
import app.hongs.action.ActionRunner;
import app.hongs.action.anno.Action;
import app.hongs.action.anno.Supply;
import app.hongs.action.anno.Verify;
import app.hongs.action.anno.Picked;
import app.hongs.action.anno.CommitSuccess;
import app.hongs.dl.IAction;
import java.util.Map;

/**
 * Lucene 模型动作
 * @author Hongs
 */
@Action("hongs/dl/lucene")
public class LuceneAction implements IAction {

    @Action("retrieve")
    @Supply(conf="", form="")
    @Picked(conf="", form="")
    @Override
    public void retrieve(ActionHelper helper) throws HongsException {
        LuceneRecord sr = getModel(helper);
        Map    rd = helper.getRequestData();
               rd = getRqMap(helper, sr, "retrieve", rd);
        Map    sd = sr.retrieve(rd);
               sd = getRpMap(helper, sr, "retrieve", sd);
//             sr.destroy( );
        helper.reply(sd/**/);
    }

    @Action("create")
    @Verify(conf="", form="")
    @CommitSuccess
    @Override
    public void create(ActionHelper helper) throws HongsException {
        LuceneRecord sr = getModel(helper);
        Map    rd = helper.getRequestData();
               rd = getRqMap(helper, sr, "create", rd);
        Map    sd = sr.create(rd);
               sd = getRpMap(helper, sr, "create", sd);
        String ss = getRpMsg(helper, sr, "create", 1 );
//             sr.destroy( );
        helper.reply(ss, sd);
    }

    @Action("update")
    @Verify(conf="", form="")
    @CommitSuccess
    @Override
    public void update(ActionHelper helper) throws HongsException {
        LuceneRecord sr = getModel(helper);
        Map    rd = helper.getRequestData();
               rd = getRqMap(helper, sr, "update", rd);
        int    sn = sr.update(rd);
        String ss = getRpMsg(helper, sr, "update", sn);
//             sr.destroy( );
        helper.reply(ss, sn);
    }

    @Action("delete")
    @CommitSuccess
    @Override
    public void delete(ActionHelper helper) throws HongsException {
        LuceneRecord sr = getModel(helper);
        Map    rd = helper.getRequestData();
               rd = getRqMap(helper, sr, "delete", rd);
        int    sn = sr.delete(rd);
        String ss = getRpMsg(helper, sr, "delete", sn);
//             sr.destroy( );
        helper.reply(ss, sn);
    }

    /**
     * 获取模型对象
     * 注意:
     *  对象 Action 注解的命名必须为 "模型路径/实体名称"
     *  方法 Action 注解的命名只能是 "动作名称", 不得含子级实体名称
     * @param helper
     * @return
     * @throws HongsException
     */
    public LuceneRecord getModel(ActionHelper helper)
    throws HongsException {
        ActionRunner runner = (ActionRunner) helper.getAttribute("__RUNNER__");
        String mod = runner.getAction();
        String ent ;
        int    pos ;
        pos  = mod.lastIndexOf('/' );
        mod  = mod.substring(0, pos); // 去掉动作
        pos  = mod.lastIndexOf('/' );
        ent  = mod.substring(1+ pos); // 实体名称
        mod  = mod.substring(0, pos); // 模型名称
        return LuceneRecord.getInstance(mod, ent);
    }

    /**
     * 获取请求数据
     * @param helper
     * @param mod
     * @param opr
     * @param req
     * @return
     * @throws HongsException
     */
    protected  Map   getRqMap(ActionHelper helper, LuceneRecord mod, String opr, Map req)
    throws HongsException {
        return req;
    }

    /**
     * 整理返回数据
     * @param helper
     * @param mod
     * @param opr
     * @param rsp
     * @return
     * @throws HongsException
     */
    protected  Map   getRpMap(ActionHelper helper, LuceneRecord mod,  String opr, Map rsp)
    throws HongsException {
        return rsp;
    }

    /**
     * 获取返回消息
     * @param helper
     * @param mod
     * @param opr
     * @param num
     * @return
     * @throws HongsException
     */
    protected String getRpMsg(ActionHelper helper, LuceneRecord mod, String opr, int num)
    throws HongsException {
        CoreLocale lang = CoreLocale.getInstance(  );
        return lang.translate("fore."+opr+".success",
               lang.translate("fore.record"),
               Integer.toString(num));
    }

}
