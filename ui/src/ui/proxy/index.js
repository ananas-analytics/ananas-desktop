// @flow

// proxy is the only place to allow native api calls

const { shell, remote } = require('electron')
const app = remote.app

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

  getSharedVariable(name: string) :any {
    let shared = remote.getGlobal('shared')
    return shared ? shared[name] : null
  }

  getProjectPathSync(projectID: string) :string {
    return `${app.getPath('userData')}/${projectID}`
  }

  openFileExploreSync(path :string) {
    shell.openItem(path)
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

  getProjectPath(id) {
    return ipc('get-project-path', id)
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
