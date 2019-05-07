// @flow

type SettingGroup = {[string]: string} 

export default class Settings {
  workspace: SettingGroup
  projects: { [string]: SettingGroup }

  static defaultProps = {
    workspace: {},
    projects: {},
  }
}
