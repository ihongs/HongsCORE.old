package app.hongs.serv.module;

import app.hongs.Core;
import app.hongs.HongsException;
import app.hongs.db.DB;
import app.hongs.db.Mtree;
import app.hongs.db.Table;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 单元模型
 * @author Hongs
 */
public class Unit extends Mtree {

    public Unit() throws HongsException {
        this(DB.getInstance("module").getTable("unit"));
    }

    public Unit(Table table)
    throws HongsException {
        super(table);
    }

    /**
     * 添加/修改记录
     *
     * @param rd
     * @return 记录ID
     * @throws app.hongs.HongsException
     */
    public String save(Map rd) throws HongsException {
        String id = (String) rd.get(this.table.primaryKey);
        if (id == null || id.length() == 0) {
            id = this.add(rd);
        } else {
            this.put(rd , id);
        }

        // 建立菜单配置
        String name = (String) rd.get("name");
        if (name != null && !"".equals(name)) {
            updateOrCreateMenuSet();
        }

        return id;
    }

    public void updateOrCreateMenuSet() throws HongsException {
        List<Map> rows;

        // 1. 找出首层单元及其全部子单元
        rows = this.table.fetchCase()
            .select("id,name")
            .where ("pid='0'")
            .all();
        Map<String, String> unitMap = new LinkedHashMap( );
        Map<String, String> deepMap = new HashMap();
        for (Map row : rows) {
            String unitId = row.get(/***/"id").toString( );
            unitMap.put(unitId,row.get("name").toString());
            List<String> ids = this.getChildIds(unitId, true);
            for (String  cid : ids) {
                deepMap.put(cid, unitId);
            }
        }

        // 2. 找出首层单元下的表单
        rows = this.db.getTable("form").fetchCase()
            .select("id,unit_id")
            .where ("unit_id IN (?)", unitMap.keySet())
            .all();
        Map<String, Set<String>> formMap = new LinkedHashMap();
        for (Map row : rows) {
            String formId = row.get(/***/"id").toString();
            String unitId = row.get("unit_id").toString();

            Set formIds;
            if (formMap.containsKey(unitId)) {
                formIds=formMap.get(unitId);
            } else {
                formIds=new LinkedHashSet();
                formMap.put(unitId,formIds);
            }
            formIds.add(formId);
        }

        // 3. 找出二级单元下的表单
        rows = this.db.getTable("form").fetchCase()
            .select("id,unit_id")
            .where ("unit_id iN (?)", deepMap.keySet())
            .all();
        Map<String, Set<String>> hideMap = new LinkedHashMap();
        for (Map row : rows) {
            String formId = row.get(/***/"id").toString();
            String unitId = row.get("unit_id").toString();
            unitId = deepMap.get(unitId);

            Set hideIds;
            if (formMap.containsKey(unitId)) {
                hideIds=hideMap.get(unitId);
            } else {
                hideIds=new LinkedHashSet();
                hideMap.put(unitId,hideIds);
            }
            hideIds.add(formId);
        }

        Document docm = makeDocument();

        Element root = docm.createElement("root");
        docm.appendChild ( root );

        Element menu;
        Element hide;
        Element incl;

        for(Map.Entry<String, String> et : unitMap.entrySet()) {
            String unitId   = et.getKey(  );
            String unitName = et.getValue();

            menu = docm.createElement( "menu" );
            root.appendChild ( menu );
            menu.setAttribute("disp", unitName);
            menu.setAttribute("href", "common/menu/cell.act?m=manage/module/unit/"+unitId);

            for(String formId : formMap.get(unitId)) {
                incl = docm.createElement("include");
                incl.appendChild(docm.createTextNode("manage/module/form/"+formId));
            }

            hide = docm.createElement( "menu" );
            menu.appendChild ( hide );
            hide.setAttribute("disp", unitName);
            hide.setAttribute("href", "!hongs-module-unit-"+unitId);

            for(String formId : hideMap.get(unitId)) {
                incl = docm.createElement("include");
                incl.appendChild(docm.createTextNode("manage/module/form/"+formId));
            }
        }

        saveDocument("unit.menu.xml", docm);
    }

    private Document makeDocument() throws HongsException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder        builder = factory.newDocumentBuilder();
            return  builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw HongsException.common(null, e);
        }
    }

    private void saveDocument(String name, Document docm) throws HongsException {
        File file = new File(Core.CONF_PATH+"/manage/module/"+name);
        if (!file.getParentFile().exists()) {
             file.getParentFile().mkdirs();
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer tr = tf.newTransformer();
            DOMSource   ds = new DOMSource(docm);
            tr.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            tr.setOutputProperty(OutputKeys.INDENT  , "yes"  );

            PrintWriter  pw = new PrintWriter(new FileOutputStream(file));
            StreamResult sr = new StreamResult(pw);
            tr.transform(ds, sr);
        } catch (TransformerConfigurationException e) {
            throw HongsException.common(null, e);
        } catch (IllegalArgumentException e) {
            throw HongsException.common(null, e);
        } catch (TransformerException  e) {
            throw HongsException.common(null, e);
        } catch (FileNotFoundException e) {
            throw HongsException.common(null, e);
        }
    }

}
