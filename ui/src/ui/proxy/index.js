// @flow

import { ipc } from './utils.js'


class Proxy {
  static instance: ?Proxy = null

  static getInstance(): Proxy {
    if (!Proxy.instance) {
      Proxy.instance = new Proxy()
    } 
    return Proxy.instance
  }

  constructor() {
  } 

  getLocalUserName() {
    return ipc('get-local-user')
  }

  getNodeMetadata() {
    return ipc('get-node-metadata')
  }

  getEditorMetadata() {
    return ipc('get-editor-metadata')
  }

  getProjectVariableDict(projectId) {
    return ipc('get-variable-dict', projectId)
  }

  saveProjectVariableDict(projectId, dict) {
    return ipc('save-variable-dict', projectId, dict)
  }

  loadProject(projectId) {
    return ipc('load-project', projectId)
  }

  getGlobalSettings() {
    return ipc('load-global-settings')
  }

  saveGlobalSettings(settings) {
    return ipc('save-global-settings', settings)
  }

	importProject() {
		return ipc('import-project')
	}

  saveProject(project) {
    return ipc('save-project', project)
  }

  deleteProject(projectId) {
    return ipc('delete-project', projectId)
  }

  loadProjects() {
    return ipc('load-projects')
  }

  loadExecutionEngines() {
    return ipc('load-execution-engines')
  }

  saveExecutionEngines(engines) {
    return ipc('save-execution-engines', engines)
  }

	login(apiEndpoint, email, password) {
		return ipc('login', `${apiEndpoint}/user/login`, email, password)
	}

  checkUpdate(notifyUpdated) {
    return ipc('check-update', notifyUpdated)
  }
}

export default Proxy.getInstance()
