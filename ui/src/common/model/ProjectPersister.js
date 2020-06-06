// @flow

import log from '../log'
import promiseHelper from '../util/promise'
import Project from './Project'

export default class ProjectPersister {
  static INSTANCE: ?ProjectPersister
  static DEFAULT_INTERVAL: number = 60000

  projects: {[string]: Array<Project>} = {}
  timer = null

  constructor(interval: number) {
    this.timer = setInterval(() => {
      this.persist()
    }, interval)
  }

  requestPersistence(project: Project, shallow: boolean) {
    log.debug('request persist project', project.project.id)
    let id = project.project.id
    if (!Object.prototype.hasOwnProperty.call(this.projects, id)) {
      this.projects[id] = []
    }
    // $FlowFixMe
    project.shallow = shallow
    this.projects[id].push(project)
  }

  persist() :Promise<any>{
    log.debug('persisting requested projects')
    if (Object.keys(this.projects).length === 0) {
      return Promise.resolve()
    }
    let projs = this.projects
    this.projects = {}

    let tasks = []
    for (let id in projs) {
      let reqs = projs[id]
      if (reqs.length > 0) {
        log.debug('persisting project', id)
        let p = reqs[reqs.length - 1]
        // $FlowFixMe
        tasks.push(p.save(p.shallow))
      }
    }
    return promiseHelper.promiseAllWithError(tasks)
  }

  static getInstance() :ProjectPersister {
    if (!ProjectPersister.INSTANCE) {
      ProjectPersister.INSTANCE = new ProjectPersister(ProjectPersister.DEFAULT_INTERVAL)
    }
    return ProjectPersister.INSTANCE
  }
}
