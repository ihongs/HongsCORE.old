package app.hcim.model;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.action.CollConfig;
import app.hongs.db.AbstractBaseModel;
import app.hongs.db.DB;
import app.hongs.db.FetchCase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
public class Entity extends AbstractBaseModel {

    public Entity() throws HongsException {
        super("hcim", "a_hcim_entity");
    }

    @Override
    public String add(Map<String, Object> info)
    throws HongsException {
        String id = super.add (info);
        this.updateEntityTable( id );
        return id;
    }

    @Override
    public String put(String id, Map<String, Object> info)
    throws HongsException {
        id = super.put ( id , info );
        this.updateEntityTable( id );
        return id;
    }

    @Override
    public int del(String id, FetchCase caze)
    throws HongsException {
        int rn = super.del(id, caze);
        this.removeEntityTable( id );
        return rn;
    }

    public void updateEntityTable(String tn)
    throws HongsException {
        // 属性类型
        Map domainTypes = getFieldTypes();

        // 实体信息
        Map    tableInfo = this.get(tn);
        String tableName = "a_haim_"+tn;
        String xn = Core.getUniqueId( );
        String tc = tableInfo.get("name").toString();

        // 实体配置
        File file = new File( Core.CONF_PATH + "/haim.xml" );
        Element root = loadDbConfig (  file  );
        Document doc = root.getOwnerDocument();
        NodeList lst = root.getElementsByTagName ( "table" );
        Element tableElem = getElemByName(lst, tableName);
        if (tableElem == null) {
            tableElem = doc.createElement("table");
            tableElem.setAttribute("name" , tableName );
            tableElem.setAttribute("primaryKey" , "id");
            root.appendChild(tableElem);
        }

        Map<String,StringBuilder> sqls = new LinkedHashMap();

        StringBuilder tableSql = new StringBuilder();
        sqls.put(tn , tableSql);
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
                String cm = (String) tab.get("name");

                String assocName = "a_haim_" + ln;
                String relatName = buildRelatName(ln, tn);
                String assocType = "2".equals( lt ) ? "HAS_ONE" : "HAS_MANY";

                buildAssocSql(sqls, relatName, xn, tn, ln, tc, cm);
                buildAssocElem(doc, tableElem, assocType, assocName, ln, relatName, tn);

                Element assocElem = getElemByName(lst, tableName );
                if (assocElem == null) {
                    assocElem = doc.createElement("table");
                    assocElem.setAttribute("name", relatName);
                    root.appendChild(assocElem);
                }
            }
        }

        tableSql.append("  PRIMARY KEY (`id`)\r\n");
        tableSql.append(") ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='").append(tc).append("'");

        for (Map.Entry et : sqls.entrySet()) {
            tn  = (String) et.getKey();
            tableSql = (StringBuilder) et.getValue();
            System.out.print(tn + ":");
            System.out.println(tableSql);
        }

        System.out.println(root.toString());
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
        info = this.get(id, caze);
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

    private StringBuilder buildAssocSql(Map sqls, String relatName, String xn, String tn, String ln, String tm, String lm) {
        StringBuilder sql2 = new StringBuilder();
        sql2.append("CREATE TABLE `x_haim_").append(xn).append("` (\r\n");
        sql2.append("  `").append(tn).append("_id` CHAR(20) NOT NULL COMMETN '").append(tm).append("',\r\n");
        sql2.append("  `").append(ln).append("_id` CHAR(20) NOT NULL COMMENT '").append(lm).append("' \r\n");
        sql2.append(") ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='").append(tm).append(" :: ").append(lm).append("'");
        sqls.put(relatName, sql2);
        return sql2;
    }

    private String buildRelatName(String tn, String ln) {
        long tnn = app.hongs.util.Text.as36Hex(tn);
        long lnn = app.hongs.util.Text.as36Hex(ln);
        if (tnn > lnn) {
            return tn + "_" + ln;
        } else {
            return ln + "_" + tn;
        }
    }
    
    private Element buildAssocElem(Document doc, Element tableElem, String assocType, String assocName, String ln, String relatName, String tn) {
        Element assocElem;
        assocElem = buildAssocElem(doc, tableElem, assocType, relatName, tn);
        assocElem = buildAssocElem(doc, assocElem, "BLS_TO" , assocName, ln);
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
            root.setAttribute("xsi:schemaLocation", "http://hongs-core ../db.xsd");
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
