package app.model;

import app.hongs.Core;
import app.hongs.db.AbstractTreeModel;

/**
 * 演示树模型
 * @author Hongs
 */
public class DemoTree extends AbstractTreeModel {

  public DemoTree() {
    super((Demo)Core.getInstance("app.model.Demo"));
    this.snumKey = "order_num";
    this.cnumKey = "childs_num";
  }

}
