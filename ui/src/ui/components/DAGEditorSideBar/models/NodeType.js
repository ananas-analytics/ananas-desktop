class NodeType {
  /**
   * The constructor of NodeType
   * @param{string} id    the id of the nodeType
   * @param{string} name  the name of the node type
   * @param{string} description  the description of the node type
   * @param{string} icon  the icon url of the node type
   * @param{string} type  the type of the node type
   * @param{string} step  the step template of the node type
   * @param{object} options the other options
   * {
   *  maxIncoming: -1, the max number of incoming connections
   *  maxOutgoing: -1, the max number of outgoing connections
   * }
   */
  constructor(id, name, description, icon, type, step, options) {
    this.id = id
    this.name = name
    this.description = description
    this.icon = icon
    this.type = type
    this.step = step
    this.options = options
  }

  static fromObject(obj) {
    return new NodeType(
      obj.id,
      obj.name, 
      obj.description,
      obj.icon,
      obj.type,
      { ... obj.step },
      { ... obj.options },
    )
  }
}

export default NodeType
