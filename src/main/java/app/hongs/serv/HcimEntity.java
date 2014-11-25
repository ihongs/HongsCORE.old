package app.hongs.serv;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.CollConfig;
import app.hongs.db.Model;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import app.hongs.db.Table;
import app.hongs.db.sync.TableSync;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 通用信息模型处理类
 * @author Hongs
 */
public class HcimEntity extends Model {

    public HcimEntity() throws HongsException {
        super(DB.getInstance("hcim").getTable("a_hcim_entity"));
    }

    @Override
    public String add(Map<String, Object> info)
    throws HongsException {
        cleanData(info);
        String id = super.add (info);
        this.updateEntityTable( id );
        return id;
    }

    @Override
    public int put(String id, FetchCase caze, Map<String, Object> info)
    throws HongsException {
        cleanData(info);
        int rn = super.put(id, caze, info);
        this.updateEntityTable( id );
        return rn;
    }

    @Override
    public int del(String id, FetchCase caze)
    throws HongsException {
        int rn = super.del(id, caze);
        this.removeEntityTable( id );
        return rn;
    }

    private void cleanData(Map<String, Object> data) {
        if (data.containsKey("a_hcim_entity_cols")) {
            List<Map<String, String>> cols = (List<Map<String, String>>)
                new ArrayList(((Map)data.get("a_hcim_entity_cols")).values());
            int i = 0;
            for (Map col : cols) {
                col.put("serialno", i++);
            }
        }
        if (data.containsKey("a_hcim_entity_rels")) {
            List<Map<String, String>> cols = (List<Map<String, String>>)
                new ArrayList(((Map)data.get("a_hcim_entity_rels")).values());
            int i = 0;
            for (Map col : cols) {
                col.put("serialno", i++);
            }
        }
    }
    
    public void updateEntityTable(String tn)
    throws HongsException {
        // 属性类型
        Map domainTypes = getFieldTypes();

        // 实体信息
        FetchCase caze  = new FetchCase();
                  caze.where(".`id` = ?", tn);
        Map    tableInfo = this.table.fetchLess(caze);
        String tableName = "a_haim_"+tn;
        String tc = tableInfo.get("name").toString();
        String xn = Core.getUniqueId();

        // 实体配置
        File file = new File(Core.CONF_PATH + "/haim.db.xml");
        Element root = loadDbConfig (  file  );
        Document doc = root.getOwnerDocument();
        NodeList tbs = root.getElementsByTagName("table");
        Element tableElem = getElemByName(tbs, tableName);
        if (tableElem == null) {
            tableElem = doc.createElement("table");
            tableElem.setAttribute("name" , tableName);
            tableElem.setAttribute("primaryKey", "id");
            root.appendChild(tableElem);
        }

        Map<String, StringBuilder> sqls = new LinkedHashMap();
        StringBuilder tableSql = new StringBuilder();
        sqls.put("" , tableSql);

        tableSql.append("CREATE TABLE `x_haim_").append(xn).append("` (\r\n");
        tableSql.append("  `id` CHAR(20) NOT NULL,\r\n");

        // 字段SQL
        List<Map> cols = (List) tableInfo.get("a_hcim_entity_cols");
        if (cols != null) {
            for(Map col : cols) {
                Map tab = (Map) col.get("a_hcim_domain");
                if (tab == null) tab = new HashMap();

                String fn = (String) tab.get( "id" );
                String ft = (String) tab.get("type");
                       ft = domainTypes.get( ft ).toString( );
                String fc = (String) col.get("name");
                if  (  fc == null  ) {
                       fc = (String) tab.get("name");
                }
                int size = Integer.parseInt(tab.get("size").toString());
                int scale = Integer.parseInt(tab.get("scale").toString());
                int signed = Integer.parseInt(tab.get("signed").toString());
                int required = Integer.parseInt(col.get("required").toString());

                buildFieldSql(tableSql, fn, ft, fc, size, scale, signed, required);
            }
        }

        // 关联SQL 及 关系表配置
        cols = (List) tableInfo.get("a_hcim_entity_rels");
        if (cols != null) {
            for(Map col : cols) {
                Map tab = (Map) col.get("a_hcim_entity" );

                String ln = (String) tab.get( "id" );
                String lt = (String) col.get("type");

                String assocName = "a_haim_" + ln;
                String relatName = "a_haim_" + buildRelatName(ln, tn);
                String assocType = "2".equals( lt ) ? "HAS_ONE" : "HAS_MANY";

                buildAssocElem(doc, tableElem, assocType, assocName, relatName, tn, ln);
                StringBuilder relatSql = buildAssocSql(xn, tn, ln);
                sqls.put(ln , relatSql);

                Element relatElem = getElemByName(tbs , relatName);
                if (relatElem == null) {
                    relatElem = doc.createElement("table");
                    relatElem.setAttribute("name", relatName);
                    root.appendChild(relatElem);
                }
            }
        }

        tableSql.append("  PRIMARY KEY (`id`)\r\n");
        tableSql.append(") ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='").append(tc).append("'");

        TableSync ts;
        DB tb = DB.getInstance("haim");
        for (Map.Entry et : sqls.entrySet()) {
            tableSql = (StringBuilder) et.getValue();
            String s = (String) et.getKey();
            if (s.length() > 0) s = "_" + s;
            String a = "a_haim_" + tn + s;
            String b = "x_haim_" + xn + s;
            tb.execute(tableSql.toString());
            ts = new TableSync(Table.getInstanceByTableName(tb, b) );
            ts.syncSlaver(Table.getInstanceByTableName(tb, a), true);
        }

        saveDbConfig(file, doc);
    }

    public void removeEntityTable(String id)
    throws HongsException {
        FetchCase caze;
        Map       info;
        DB        dd;
        String    dn, tn, sql;

        caze = new FetchCase(   );
        caze.select(".module_id");
        caze.where (".id = ?",id);
        info = this.table.fetchLess(caze);
        dn   =   "hcxm/" + info.get("module_id").toString();
        tn   = "a_hcxm_" + id;
        dd   = DB.getInstance(dn);
        sql  = "DROP TABLE `" + tn + "`";

        dd.execute(sql);
    }

    /** 私有工具 **/

    private Map getFieldTypes()
    throws HongsException {
        CollConfig dc = CollConfig.getInstance( "hcim" );
        List dl = (List) dc.getDataByKey("DOMAIN_TYPES");
        Map dm = new HashMap();
        for (Object oo : dl) {
            List di = (List) oo;
            dm.put(di.get(0).toString(), di.get(2).toString());
        }
        return dm;
    }

    private Element getElemByName(NodeList list, String name)
    {
        for (int i = 0; i < list.getLength(); i ++) {
            Element item = (Element) list.item( i );
            String nama = item.getAttribute("name");
            if (nama.equals(name)) {
                return item;
            }
        }
        return null;
    }

    private StringBuilder buildFieldSql(StringBuilder sql, String fn, String ft, String cm, int size, int scale, int signed, int required) {
        StringBuilder sql2 = new StringBuilder();
        sql2.append("  `").append(fn).append("` ").append(ft);

        if (size > 0) {
            if (scale > 0) {
                sql2.append("(").append(size).append(",").append(scale).append(")");
            } else {
                sql2.append("(").append(size).append(")");
            }
        }
        if (signed == 0 && "DECIMAL".equals(ft)) {
            sql2.append(" UNSIGNED");
        }
        if (required != 0) {
            sql2.append(" NOT NULL");
        } else {
            sql2.append(" NULL");
        }

        if (cm != null && ! cm.equals("")) {
            sql2.append(" COMMENT '").append(cm).append("'");
        }

        sql2.append(",\r\n");
        sql .append(sql2);
        return sql2;
    }

    private StringBuilder buildAssocSql(String xn, String tn, String ln) {
        StringBuilder sql2 = new StringBuilder();
        sql2.append("CREATE TABLE `x_haim_" ).append( xn ).append("_").append(ln).append("` (\r\n");
        sql2.append("  `").append(tn).append("_id` CHAR(20) NOT NULL").append(",\r\n");
        sql2.append("  `").append(ln).append("_id` CHAR(20) NOT NULL").append(",\r\n");
        sql2.append("  PRIMARY KEY (`").append(tn).append("_id`,`").append(ln).append("_id`),\r\n");
        sql2.append("  KEY `fk_").append(ln).append("_").append(tn).append("_id` (`").append(tn).append("_id`),\n");
        sql2.append("  KEY `fk_").append(tn).append("_").append(ln).append("_id` (`").append(ln).append("_id`) \n");
        sql2.append(") ENGINE=MyISAM DEFAULT CHARSET=utf8");
        return sql2;
    }

    private String buildRelatName(String tn, String ln) {
        long tnn = app.hongs.util.Util.as36Hex(tn);
        long lnn = app.hongs.util.Util.as36Hex(ln);
        if (tnn > lnn) {
            return tn + "_" + ln;
        } else {
            return ln + "_" + tn;
        }
    }
    
    private Element buildAssocElem(Document doc, Element tableElem, String assocType, String assocName, String relatName, String tn, String ln) {
        Element assocElem;
        assocElem = buildAssocElem(doc, tableElem, assocType, relatName, tn);
        assocElem = buildAssocElem(doc, assocElem, "BLS_TO" , assocName, ln);
        assocElem.setAttribute("primaryKey", tn+"_id");
        return  assocElem;
    }

    private Element buildAssocElem(Document doc, Element tableElem, String assocType, String assocName, String ln) {
        NodeList list = tableElem.getChildNodes( );
        Element assocElem = getElemByName(list, assocName);
        if (assocElem == null) {
            assocElem = doc.createElement("assoc");
            tableElem.appendChild  (  assocElem  );
        }
        assocElem.setAttribute("name" , assocName);
        assocElem.setAttribute("type" , assocType);
        assocElem.setAttribute("foreignKey", ln+"_id");

        return  assocElem;
    }

    private Element loadDbConfig(File file)
    throws HongsException {
        DocumentBuilderFactory dbf;
        DocumentBuilder dbo;
        Document doc;
        Element elem;

        if (file.exists()) {
            try {
                dbf = DocumentBuilderFactory.newInstance();
                dbo = dbf.newDocumentBuilder();
                doc = dbo.parse(file);
            } catch (ParserConfigurationException ex) {
                throw new HongsException(0x1000 , ex);
            } catch (SAXException ex) {
                throw new HongsException(0x1000 , ex);
            } catch (IOException  ex) {
                throw new HongsException(0x1000 , ex);
            }

            elem = (Element) doc.getElementsByTagName("tables").item(0);
        } else {
            try {
                dbf = DocumentBuilderFactory.newInstance();
                dbo = dbf.newDocumentBuilder();
                doc = dbo.newDocument();
            } catch (ParserConfigurationException ex) {
                throw new HongsException(0x1000 , ex);
            }

            Element root = doc.createElement("db");
            root.setAttribute("xmlns", "http://hongs-core");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("xsi:schemaLocation", "http://hongs-core .db.xsd");
            doc .appendChild(root);

            elem = doc.createElement("source");
            elem.setAttribute("link", "hcim");
            root.appendChild(elem);

            elem = doc.createElement("origin");
            elem.setAttribute("link", "hcim");
            root.appendChild(elem);

            elem = doc.createElement("tables");
            root.appendChild(elem);
        }

        return elem;
    }

    private void saveDbConfig(File file, Document doc)
    throws HongsException {
        if (!file.exists()) {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                 dir.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException ex) {
                throw new HongsException(0x1000, ex);
            }
        }

        FileOutputStream    fos;
        OutputStreamWriter  osw;
        try {
            fos = new FileOutputStream  (file);
            osw = new OutputStreamWriter(fos );
        } catch (FileNotFoundException ex) {
            throw new HongsException(0x1000, ex);
        }

        Source      src = new DOMSource   (doc);
        Result      rst = new StreamResult(osw);
        Transformer trs;
        try {
            trs = TransformerFactory.newInstance().newTransformer();
            trs.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            trs.transform(src, rst);
        } catch (TransformerConfigurationException ex) {
            throw new HongsException(0x1000, ex);
        } catch (TransformerException ex) {
            throw new HongsException(0x1000, ex);
        }
    }

}
