package net.hongs.search.serv;

import app.hongs.HongsException;
import app.hongs.action.FormSet;
import app.hongs.dl.lucene.LuceneRecord;
import app.hongs.util.Synt;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

/**
 * 记录器
 * @author Hongs
 */
public class Search extends LuceneRecord {

    public Search(String conf, String name) throws HongsException {
        super(conf, name);
    }

    private static Map getFields(String name) throws HongsException {
        int    pos = name.lastIndexOf('/');
        String mod = name.substring(0,pos);
        String ent = name.substring(pos+1);
        return FormSet.getInstance (mod).getForm(ent);
    }

    public Map counts(Map rd) throws HongsException {
        initReader();

        Map  resp = new HashMap();
        Map  cnts = new HashMap();
        resp.put("info", cnts);

        Set<String> cnt2 = Synt.declare(rd.get("count"), Set.class);
        Map<String, Map<String, Integer>> cuntz = new HashMap();
        Map<String, Map<String, Integer>> cuntx = new HashMap();

        if (cnt2 == null ||  cnt2.isEmpty( )) {
            for(Object o : fields.entrySet()) {
                Map.Entry e = (Map.Entry) o;
                String k = (String) e.getKey();
                Map    m = (Map ) e.getValue();

                // id,wd 都不统计
                if (idCol.equals(k) || wdCol.equals(k)) {
                    continue;
                }

                // 未存储的不统计
               boolean s = Synt.declare(m.get("lucene-store"), true);
                if ( ! s ) {
                    continue;
                }

                // text 和 json 的不统计
                String g = Synt.declare(m.get("lucene-field"),  "" );
                if ("text".equals(g) || "json".equals(g)) {
                    continue;
                }

                cuntx.put(k, new HashMap());
            }
        } else {
            for(String   x : cnt2) {
                String[] a = x.split(":", 2);
                if (!fields.containsKey(a[0])) {
                    throw new HongsException(HongsException.COMMON, "Field "+a[0]+" not exists");
                }
                if (a.length > 1) {
                    cuntz.get(a[0]).put( a[1], 0 );
                } else
                if (! cuntx.containsKey( a[0] )  ) {
                    cuntx.put(a[0], new HashMap());
                }
            }
        }

        try {
            Query q = getQuery(rd);

            TopDocs docz = finder.search(q, 500);
            while ( docz.totalHits > 0) {
                ScoreDoc[] docs = docz.scoreDocs;
                for(ScoreDoc d  : docs) {
                    Document doc = reader.document(d.doc);

                    for(Map.Entry<String, Map<String, Integer>> et : cuntz.entrySet()) {
                        String k = et.getKey();
                        Map<String, Integer> cntc = et.getValue();
                        String[] vals = doc.getValues(k);

                        for ( String val : vals ) {
                            if (cntc.containsKey(val)) {
                                cntc.put(val , cntc.get(val) + 1);
                            }
                        }
                    }

                    for(Map.Entry<String, Map<String, Integer>> et : cuntx.entrySet()) {
                        String k = et.getKey();
                        Map<String, Integer> cntc = et.getValue();
                        String[] vals = doc.getValues(k);

                        for ( String val : vals ) {
                            if (cntc.containsKey(val)) {
                                cntc.put(val , cntc.get(val) + 1);
                            } else {
                                cntc.put(val , 1);
                            }
                        }
                    }
                }

                if (docs.length > 0) {
                    docz=finder.searchAfter(docs[docs.length - 1], q, 500);
                } else {
                    break;
                }
            }
        } catch (IOException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }

        cnts.putAll(cuntx);
        cnts.putAll(cuntz);

        return resp;
    }

}
