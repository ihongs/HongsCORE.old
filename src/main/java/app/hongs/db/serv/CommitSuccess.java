package app.hongs.db.serv;

import app.hongs.action.anno.ActionWrapper;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 操作成功才提交数据更改
 * @author Hongs
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ActionWrapper(CommitInvoker.class)
public @interface CommitSuccess { }
