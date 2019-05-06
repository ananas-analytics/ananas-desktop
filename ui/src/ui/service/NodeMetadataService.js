import { PlainNodeMetadata } from '../../common/model/flowtypes'

export default class NodeMetadataService {
  store: any

  getServiceName() {
    return 'NodeMetadataService'
  }

  setStore(store) {
    this.store = store
  }

  /**
   * load all NodeMetadata
   * @returns {Promise<Array<PlainNodeMetadata>>}
   */
  load() :Promise<Array<PlainNodeMetadata>>{
    
  }
}
