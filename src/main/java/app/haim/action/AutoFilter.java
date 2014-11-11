package app.haim.action;

import app.hongs.Core;
import app.hongs.HongsException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Hongs
 */
public class AutoFilter implements Filter {

    Set<String> set = new HashSet();
    
    public void init(FilterConfig config)
    throws ServletException {
        String confName = config.getInitParameter("conf-name");
        File confFile = new File(Core.CONF_PATH + "/"+confName+".db.xml");
        
        try {
            Element root = loadDbConfig(confFile);
            NodeList tbs = root.getElementsByTagName("table");
            
            for (int i = 0; i < tbs.getLength(); i ++) {
                Element item = (Element) tbs.item( i );
                String  name = item.getAttribute("name");
                if (item.getAttribute("primaryKey") != null) {
                    set.add(name);
                }
            }
            
            
        } catch (HongsException ex) {
            
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    }

    public void destroy() {
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
            throw new HongsException(0x1001);
        }

        return elem;
    }

}
