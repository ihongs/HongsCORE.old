package app.hongs.tag;

import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import app.hongs.action.ActionFilter;

/**
 * <h1>动作权限判断标签</h1>
 *
 * <h2>使用方法:</h2>
 * <pre>
 * &lt;hs:act act="action" [not="true|false"] [els="true|false"]/&gt;
 * &lt;hs:act act="action" [not="true|false"] [els="true|false"]&gt;Some Text&lt;/hs:act&gt;
 * </pre>
 *
 * @author Hongs
 */
public class ActTag extends BodyTagSupport {

  private String act;
  private Boolean not = false;
  private Boolean els = false;
  private Boolean ebb = false;
  private Boolean checkLogin = false;
  private String configName = "default";
  private String sessionKey = "actions";

  @Override
  public int doStartTag() throws JspException {
    this.ebb = ActionFilter.checkAction(this.act,
    this.configName, this.sessionKey, this.checkLogin);

    if (this.not) {
        this.ebb =! this.ebb;
    }

    if (this.els || this.ebb) {
      return BodyTagSupport.EVAL_BODY_BUFFERED;
    } else {
      return BodyTagSupport.SKIP_BODY;
    }
  }

  @Override
  public int doEndTag() throws JspException {
    try {
      BodyContent body = this.getBodyContent();

      if (null != body) {
        String[] arr = body.getString().trim()
                      .split("<!--ELSE-->", 2);

        JspWriter out = body.getEnclosingWriter();

        if (this.ebb) {
          out.print(arr[0]);
        } else if (arr.length > 1) {
          out.print(arr[1]);
        }
      } else {
        JspWriter out = this.pageContext.getOut();

        if (this.ebb) {
          out.print("true");
        } else {
          out.print("false");
        }
      }
    } catch (IOException ex) {
      throw new JspException("Error in ActTag", ex);
    }

    return BodyTagSupport.EVAL_PAGE;
  }

  public void setAct(String act) {
    this.act = act;
  }

  public void setNot(Boolean not) {
    this.not = not;
  }

  public void setEls(Boolean els) {
    this.els = els;
  }

  public void setCkin(Boolean cl) {
    this.checkLogin = cl;
  }

  public void setConf(String cn) {
    this.configName = cn;
  }

  public void setSess(String sk) {
    this.sessionKey = sk;
  }

}
