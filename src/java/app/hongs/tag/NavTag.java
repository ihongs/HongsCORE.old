package app.hongs.tag;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import java.util.Map;

import app.hongs.Core;
import app.hongs.CoreLanguage;
import app.hongs.HongsException;
import app.hongs.action.ActionConfig;
import app.hongs.action.ActionFilter;
import app.hongs.action.ActionHelper;

/**
 * 导航标记
 * @author Hongs
 */
public class NavTag extends SimpleTagSupport {

    private String type = "title";
    private String conf = "default";
    private String sess = "actions";
    private boolean ckin = false;
    private int level = 0;
    private int depth = 0;

    private CoreLanguage cl;
    private ActionHelper ah;

    @Override
    public void doTag() throws JspException {
        JspWriter out = getJspContext().getOut();

        Core core = Core.getInstance(  );
        cl = (CoreLanguage)
             core.get(app.hongs.CoreLanguage.class);
        ah = (ActionHelper)
             core.get(app.hongs.action.ActionHelper.class);
        ActionConfig ac;
        try {
            ac = new ActionConfig(conf );
        } catch (HongsException ex) {
            throw new JspException( ex );
        }

        try {
            if ("title".equals(type)) {
                Map page = ac.getPage(Core.ACTION.get());
                if (page == null) {
                    out.print(cl.translate("page.untitled") );
                }
                else {
                    out.print(
                    this.translate((String)page.get("name")));
                }
            }
            else if ("menu".equals(type)) {
                out.print(this.buildmenu(ac.pages, 0));
            }

            JspFragment f = getJspBody();
            if (f != null) f.invoke(out);
        } catch (java.io.IOException ex) {
            throw new JspException("Error in NavTag tag", ex);
        }
    }

    public void setCkin(Boolean ckin) {
        this.ckin = ckin;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }

    public void setSess(String sess) {
        this.sess = sess;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    private StringBuilder buildmenu(Map<String, Map> pages, int i) {
        StringBuilder sb = new StringBuilder();
        if (i < level) {
            for (Map node : pages.values()) {
                String uri = (String)node.get("uri");
                if (! ActionFilter.checkAction(uri, conf, sess, ckin)) {
                    continue;
                }

                if (node.containsKey("pages")) {
                    sb.append(this.buildmenu((Map)node.get("pages"), i + 1));
                }
            }
        }
        else
        if (i < level + depth + 1) {
            String levelClass = i==level ? "" : "-"+(i-level);
            sb.append("<div class=\"nav-list")
              .append(levelClass)
              .append("\">");

            for (Map node : pages.values()) {
                String uri = (String)node.get("uri");
                if (! ActionFilter.checkAction(uri, conf, sess, ckin)) {
                    continue;
                }

                String name = (String)node.get("name");
                uri = Core.BASE_HREF + uri;
                name = this.translate(name);

                sb.append("<div class=\"nav-item")
                  .append(levelClass)
                  .append("\">");
                sb.append("<a class=\"nav-link")
                  .append(levelClass)
                  .append("\" href=\"")
                  .append(uri)
                  .append("\">")
                  .append(name)
                  .append("</a>");

                if (node.containsKey("pages")) {
                    sb.append(this.buildmenu((Map)node.get("pages"), i + 1));
                }

                sb.append("</div>");
            }
            
            sb.append("</div>");
        }
        return sb;
    }

    private String translate(String name) {
        if (name.indexOf("{opt}") != -1) {
            if (ah.getParameter("id") != null) {
                name = name.replace("{opt}", "set");
            }
            else {
                name = name.replace("{opt}", "add");
            }
        }
        return cl.translate(name);
    }

}
