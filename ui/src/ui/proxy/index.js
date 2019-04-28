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

  getProjectVariableDict(projectId) {
    return ipc('get-variable-dict', projectId)
  }

  saveProjectVariableDict(projectId, dict) {
    return ipc('save-variable-dict', projectId, dict)
  }

  loadProject(projectId) {
    return ipc('load-project', projectId)
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

	login(apiEndpoint, email, password) {
		return ipc('login', `${apiEndpoint}/user/login`, email, password)
	}
}

export default Proxy.getInstance()
