// @flow

import proxy from '../proxy'

import type { ID, PlainProject } from '../../common/model/flowtypes.js'

export default class ModelService {
  url: string
  store: any

  constructor(url :string) {
    this.url = url
  }

  getServiceName() {
    return 'ModelService'
  }

  setStore(store:any) {
    this.store = store
  }
  
  loadProjects() {
    return proxy.loadProjects()
  }

  saveProject(project :PlainProject) :Promise<'OK'> {
    return proxy.saveProject(project)
  }

  loadProject(id :ID) :Promise<PlainProject> {
    return proxy.loadProject(id)
  }

  importProject() :Promise<PlainProject> {
    return proxy.importProject()
  }

  deleteProject(id :ID) :Promise<'OK'> {
    return proxy.deleteProject(id)
  }

}

