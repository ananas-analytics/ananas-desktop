// @flow

import type { Setting } from '../../common/model/flowtypes'

import proxy from '../proxy'

export default class SettingService {
  store: any

  getServiceName() {
    return 'SettingService'
  }

  setStore(store: any) {
    this.store = store
  } 

  loadGlobalSettings() :Setting {
    return proxy.getGlobalSettings()
  }

  saveGlobalSettings() {
    let globalSettings = this.getGlobalSettings()
    return proxy.saveGlobalSettings(globalSettings)
  }

  getProjectSettings(projectId: string) :Setting {
    let globalSettings = this.getGlobalSettings()
    let localSettings = this.store.getState().model.projects[projectId] || {}
    // TODO: only copy necessary setting from global to the result
    return Object.assign({}, globalSettings, localSettings)
  }

  getGlobalSettings() :Setting {
    return this.store.getState().settings || {}
  }

  getProjectSettingValue(projectId: string, name: string) :any {
    let settings = this.getProjectSettings(projectId)
    if (settings.hasOwnProperty(name)) {
      return settings[name]
    }
    return null
  }

  getGlobalSettingValue(projectId: string, name: string) :any {
    let settings = this.getGlobalSettings()
    if (settings.hasOwnProperty(name)) {
      return settings[name]
    }
    return null
  }
}
