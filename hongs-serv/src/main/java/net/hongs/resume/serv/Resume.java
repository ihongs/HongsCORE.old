package net.hongs.resume.serv;

import app.hongs.HongsException;
import app.hongs.dl.lucene.LuceneRecord;

/**
 * 简历模型
 * @author Hongs
 */
public class Resume extends LuceneRecord {

    public Resume(String conf, String form) throws HongsException {
        super(conf, form);
    }

}
