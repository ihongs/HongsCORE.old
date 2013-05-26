package app.action;

import java.util.Map;

import app.hongs.HongsException;
import app.hongs.Core;
import app.hongs.action.ActionHelper;

/**
 * 演示
 *
 * @author hongs
 */
public class Demo {

  app.model.Demo demo;
  app.model.DemoTree demoTree;

  public Demo() {
    this.demo = (app.model.Demo)Core.getInstance("app.model.Demo");
    this.demoTree = (app.model.DemoTree)Core.getInstance("app.model.DemoTree");
  }

  public void actionTest(ActionHelper helper)
  {
    helper.pass(helper.getRequestData());
  }

  public void actionTree(ActionHelper helper)
  throws HongsException {
    Map data = this.demoTree.getTree(helper.getRequestData());
    helper.pass(data);
  }

  public void actionList(ActionHelper helper)
  throws HongsException {
    Map data = this.demoTree.getPage(helper.getRequestData());
    helper.pass(data);
  }

  public void actionInfo(ActionHelper helper)
  throws HongsException {
    Map data = this.demoTree.getInfo(helper.getRequestData());
    helper.pass(data);
  }

  public void actionSave(ActionHelper helper)
  throws HongsException {
    this.demoTree.save(helper.getRequestData());
    helper.pass();
  }

  public void actionRemove(ActionHelper helper)
  throws HongsException {
    this.demoTree.remove(helper.getRequestData());
    helper.pass();
  }

  public void actionExists(ActionHelper helper)
  throws HongsException {
    if (this.demoTree.exists(helper.getRequestData())) {
      helper.pass();
    }
    else {
      helper.fail();
    }
  }

}
