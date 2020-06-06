import { PlainNodeMetadata } from '../../common/model/flowtypes'

import proxy from '../proxy'

export default class MetadataService {
  store: any
  nodeMetadata: ?Array<PlainNodeMetadata> = null
  editorMetadata: ?{[string]: any} = null

  getServiceName() {
    return 'MetadataService'
  }

  setStore(store) {
    this.store = store
  }

  /**
   * load all NodeMetadata
   * @returns {Promise<Array<PlainNodeMetadata>>}
   */
  loadNodeMetadata() :Promise<Array<PlainNodeMetadata>>{
    if (this.nodeMetadata) {
      return Promise.resolve(this.nodeMetadata)
    }
    return proxy.getNodeMetadata()
      .then(metadata => {
        this.nodeMetadata = metadata
        return metadata
      })
  }

  loadEditorMetadata() :Promise<{[string]:any}>{
    if (this.editorMetadata) {
      return Promise.resolve(this.editorMetadata)
    }
    return proxy.getEditorMetadata()
      .then(metadata => {
        this.editorMetadata = metadata
        return metadata
      })
  }
}
