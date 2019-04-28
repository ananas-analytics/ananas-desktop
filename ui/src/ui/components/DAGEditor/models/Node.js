export default class Node {
  /**
   * Node constructor
   * @param{string} id    the id of the node
   * @param{string} type  the type of the node
   * @param{string} icon  the icon of the node
   * @param{number} x     the x position of the node
   * @param{number} y     the y position of the node
   * @param{string} label the label of the node 
   * @param{object} data  the data of the node
   */
  constructor(id, type, icon, x, y, label, data) {
    this.id = id
    this.label = label
    this.type = type
    this.icon = icon
    this.x = x
    this.y = y
    this.data = data
  }

  /**
   * Create a Node instance from plain object, the object is 
   * in the following form:
   *
   * {
   *  type: string, // the type of the node
   *  icon: string, // the url of the icon of the node
   *  label: string, // the label of the node
   *  subtype: string, // the subtype of the node
   *  pos: {
   *    x: number, // the x coordinator
   *    y: number, // the y coordinator
   *  }
   * }
   */
  static fromObject(object) {
    return new Node(object.id)
  }
}


