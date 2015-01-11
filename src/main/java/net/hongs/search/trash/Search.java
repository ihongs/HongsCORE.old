package net.hongs.search.trash;



import app.hongs.Core;
import app.hongs.CoreConfig;
import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.FetchPage;
import app.hongs.db.Model;
import app.hongs.db.Table;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Document;

/**
 * 搜索模型
 * @author Hongs
 */
//@Cmdlet("search")
public class Search extends Model {

    private String     wt = "10000";
    // 检索名, 参数名, 加权值
    private String[][] ws = new String[][] {
        {"subject", "sd",  null},
        {"keyword", "kd", "100"},
        {"related", "rd",   "1"}
    };

    public Search(Table table) throws HongsException {
        super(table);
    }
    
    public Search() throws HongsException {
        super(DB.getInstance("search").getTable("article"));
    }

    @Override
    public String create(Map rd)
    throws HongsException
    {
        try {
        String areaId = (String) rd.get("area_id");
        
        
        Directory dir = FSDirectory.open(new File(Core.VARS_PATH+"/lucene"));
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
        iwc.setOpenMode(OpenMode.CREATE);// 创建模式 OpenMode.CREATE_OR_APPEND 添加模式
	IndexWriter writer = new IndexWriter(dir, iwc);
        } catch (IOException ex) {
            
        }
        
        return null;
    }
    
    public Map searchArticle(Map rd) throws HongsException {
        FetchCase caze = new FetchCase();

        for (int i = 0; i < ws.length; i ++) {
            String k = ws[i][0];
            Object s = rd.get(ws[i][1]);
            if  (  s == null) continue ;
            Table  t = db.getTable("article_"+k);

            FetchCase caze2;
            if (!caze.hasFrom()) {
                caze2 = caze.from( t.tableName, t.name )
                        .groupBy (".article_id");
            } else {
                caze2 = caze.join( t.tableName, t.name )
                        .on(".article_id = :article_id");
            }
            if ("related".equals(k)) {
                caze2.where("(."+k+"_id IN (?) OR ."+k+"_id = NULL)", s);
            } else
            if ("keyword".equals(k)) {
                caze2.where( "."+k+"_id IN (?)", s);
            } else {
                int n = ((Collection) s).size( );
                caze2.where( "."+k+"_id IN (?)", s);
                caze2.havin( "COUNT(."+k+"_id) = ?", n);
            }

            ws[i][0] =  null;

            String w = ws[i][2];
            if  (  w == null) continue ;

            //caze2.orderBy("SUM(.weight) DESC");
        }

        FetchCase caze3;
        if (!caze.hasFrom()) {
            caze3 = caze.from(table.tableName, table.name)
                    .groupBy (".id");
        } else {
            caze3 = caze.join(table.tableName, table.name)
                    .on(".id = :article_id");
        }
        caze3.select (".id, .name, .note, .data")
             .orderBy(".weight DESC");

        for (int i = 0; i < ws.length; i ++) {
            String k = ws[i][0];
            if  (  k == null) continue ;
            Table  t = db.getTable("article_"+k);

            FetchCase caze2;
            caze2 = caze3.join(t.tableName, t.name)
                    .on(".article_id = :id");

            String w = ws[i][2];
            if  (  w == null) continue ;

            //caze2.orderBy("SUM(.weight) DESC");
        }

        //** 查询数据 **/

        // 获取页码, 默认为第一页
        int page = 1;
        if (rd.containsKey(this.pageKey))
        {
          page = Integer.parseInt((String)rd.get(this.pageKey));
        }

        // 获取行数, 默认依从配置
        int rows;
        if (rd.containsKey(this.rowsKey))
        {
          rows = Integer.parseInt((String)rd.get(this.rowsKey));
        }
        else
        {
          rows = CoreConfig.getInstance().getProperty("fore.rows.per.page", 20);
        }

        Map data = new HashMap();

        long x = System.currentTimeMillis();
        
        if (rows != 0)
        {
          FetchPage fp = new FetchPage(caze, db);
          fp.setPage(page != 0 ? page : 1);
          fp.setRows(rows >  0 ? rows : Math.abs(rows));

          // 页码等于 0 则不要列表数据
          if (page != 0 )
          {
            List list = fp.getList();
            data.put( "list", list );
          }

          // 行数小于 0 则不要分页信息
          if (rows >  0 )
          {
            Map  info = fp.getPage();
            data.put( "page", info );
          }
        }
        else
        {
          // 行数等于 0 则不要使用分页
            List list = db.fetchMore(caze);
            data.put( "list", list );
        }

        x = System.currentTimeMillis() - x;
        data.put("qt", x);
        data.put("kt", rd.get("kt"));
        
        return data;
    }

    public Map searchSubject(Map rd) throws HongsException {
        FetchCase caze = new FetchCase();

        for (int i = 0; i < ws.length; i ++) {
            String k = ws[i][0];
            Object s = rd.get(ws[i][1]);
            if  (  s == null) continue ;
            Table  t = db.getTable("article_"+k);

            FetchCase caze2;
            if (!caze.hasFrom()) {
                caze2 = caze.from( t.tableName, t.name );
            } else {
                caze2 = caze.join( t.tableName, t.name )
                        .on(".article_id = :article_id");
            }
            if ("realted".equals(k)) {
                caze2.where("(."+k+"_id IN (?) OR ."+k+"_id = NULL)", s);
            } else {
                int n = ((Collection) s).size( );
                caze2.where( "."+k+"_id IN (?)", s);
                caze2.havin( "COUNT(."+k+"_id) = ?", n);
            }

            ws[i][0] =  null;
        }

        FetchCase caze3;
        if (!caze.hasFrom()) {
            caze3 = caze.from(table.tableName, table.name);
        } else {
            caze3 = caze.join(table.tableName, table.name)
                    .on(".id = :article_id");
        }

        for (int i = 0; i < ws.length; i ++) {
            String k = ws[i][0];
            if  (  k == null) continue ;
            Table  t = db.getTable("article_"+k);

            FetchCase caze2;
            caze2 = caze.join( t.tableName, t.name )
                    .on(".article_id = :article_id");
        }

        caze.select ("article_subject.subject_id, COUNT(DISTINCT article.id) AS `count`")
            .groupBy("article_subject.subject_id");

        //** 查询数据 **/

        List list = db.fetchMore(caze);
        Map data = new HashMap();
        data.put( "list", list );

        return data;
    }

}
