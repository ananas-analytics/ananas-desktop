// @flow

import proxy from '../proxy'

import type { ID, PlainEngine, PlainProject } from '../../common/model/flowtypes.js'

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

  loadExecutionEngines() {
    return proxy.loadExecutionEngines()
      .catch(() => {
        return Promise.resolve([])
      })
  }

  saveExecutionEngines(engines: Array<PlainEngine>) {
    return proxy.saveExecutionEngines(engines)
  }

  loadProjects() {
    return proxy.loadProjects()
  }

  saveProject(project :PlainProject, shallow :boolean) :Promise<'OK'> {
    return proxy.saveProject(project, shallow)
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

  getProjectPath(id :ID) :Promise<string> {
    return proxy.getProjectPath(id)
  }

}

