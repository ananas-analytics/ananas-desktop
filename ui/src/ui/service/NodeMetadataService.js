import { PlainNodeMetadata } from '../../common/model/flowtypes'

import proxy from '../proxy'

export default class NodeMetadataService {
  store: any
  metadata: ?Array<PlainNodeMetadata> = null

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
    if (this.metadata) {
      return Promise.resolve(this.metadata)
    }
    return proxy.getMetadata() 
      .then(metadata => {
        this.metadata = metadata
        return metadata
      })
    
  }
}
