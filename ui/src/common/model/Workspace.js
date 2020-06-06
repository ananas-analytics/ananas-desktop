// @flow

import fs from 'fs'
import util from 'util'
import YAML from 'yaml'
import ObjectID from 'bson-objectid'

import log from '../log'
import Project from './Project'
import promiseHelper from '../util/promise'

import type { PlainEngine, ProjectMeta, PlainNodeMetadata } from './flowtypes'


export default class Workspace {
  static INSTANCE: ?Workspace

  path: string
  projects: Array<ProjectMeta> // metadata of the projects in the workspace
  settings: { [string]: any }

  static defaultProps = {
    INSTANCE : null,
    path     : '',
    project  : [],
    settings : {},
  }

  constructor(path: string, workspace: any) {
    let obj = workspace || {}
    this.path = path
    this.projects = obj.projects || []
    this.settings = obj.settings || {}
  }

  insertOrUpdateProject(project: ProjectMeta) {
    let exists = false
    this.projects.map(p => {
      if (p.id === project.id) {
        exists = true
        return project
      }
      return p
    })

    if (!exists) {
      this.projects.push(project)
    }
  }

  removeProject(projectId: string) {
    this.projects = this.projects.filter(project => project.id !== projectId)
  }

  toString() {
    return YAML.stringify({
      projects: this.projects,
      settings: this.settings,
    })
  }

  toPlainObject() {
    return {
      projects: this.projects,
      settings: this.settings,
    }
  }

  save() :Promise<any> {
    return util.promisify(fs.writeFile)(this.path, this.toString(), 'utf8')
  }

  getProjectMeta(projectId: string) :?ProjectMeta {
    return this.projects.find(project => project.id === projectId)
  }

  importProject(projectPath: string, metadata: {[string]: PlainNodeMetadata}) :Promise<Project> {
    let meta = this.projects.find(project => project.path === projectPath)
    if (!meta) {
      meta = { id: ObjectID.generate(), path: projectPath }
      return Project.Load(meta.path, metadata, true)
    } else {
      return Promise.reject(new Error('Project already exists'))
    }
  }

  loadProject(projectId: string, metadata: {[string]: PlainNodeMetadata}) :Promise<Project> {
    let meta = this.projects.find(project => project.id === projectId)
    if (!meta) {
      return Promise.reject(new Error('Can NOT find project'))
    }
    return Project.Load(meta.path, metadata, false)
  }

  loadProjects(metadata: {[string]: PlainNodeMetadata}) :Promise<Array<Project>> {
    let tasks = this.projects.map(project => {
      return Project.Load(project.path, metadata, true)
    })
    return promiseHelper.promiseAllWithoutError(tasks)
  }

  saveExecutionEngines(location :string, engines :Array<PlainEngine>) :Promise<any> {
    return util.promisify(fs.writeFile)(location, YAML.stringify(engines), 'utf8')
  }

  loadExecutionEngines(file :string) :Promise<PlainEngine> {
    return util.promisify(fs.readFile)(file)
      .then(data => {
        return YAML.parse(data.toString())
      })
  }

  static Load(file: string) :Promise<Workspace> {
    if (Workspace.INSTANCE !== null &&
       Workspace.INSTANCE !== undefined) {
      return Promise.resolve(Workspace.INSTANCE)
    }
    return util.promisify(fs.readFile)(file)
      .then(data => {
        let workspace = YAML.parse(data.toString())
        Workspace.INSTANCE = new Workspace(file, workspace)
        log.info('successfully load workspace instance')
        return Workspace.INSTANCE
      })
      .catch(err => {
        log.warn(err.message)
        log.warn('initiate workspace')
        return Promise.resolve(new Workspace(file))
      })
      .then(workspace => {
        log.info('workspace loaded')
        return workspace
      })
  }
}
