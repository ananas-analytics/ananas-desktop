export default class Connection {
  /**
   * Constructor of connection
   * @param{string} source  the id of the source node
   * @param{string} target  the id of the target node
   */
  constructor(source, target) {
    this.source = source
    this.target = target
  }
}
