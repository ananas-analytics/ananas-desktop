// @flow

const loki = require('lokijs')

const log = require('../common/log')


import type { 
  ID, 
  VariableDictionary, 
  PlainProject,
} from '../common/model/flowtypes.js'

class LocalDB {
  static PROJECT = 'PROJECT'
  static STEP = 'STEP'
  static VARIABLE = 'VARIABLE'
  static VARIABLE_DICT = 'VARIABLE_DICT'


  path: string
  db: any

  constructor(path: string) {
    this.path = path
    this.db = new loki(path, {
      verbose: true,
      autoload: true,
      autosave: true,
      autosaveInterval: 5000,
    })
  }

  /**
   * get or create a collection by its name
   * @name string collection name
   * @return Collection
   */
  getOrCreateCollection(name:string) {
    let col = this.db.getCollection(name)
    if (!col) {
      col = this.db.addCollection(name, this.getCollectionOptions(name))
    }
    return col
  } 

  getCollectionOptions(name:string) {
    switch(name) {
      case LocalDB.VARIABLE_DICT:
      case LocalDB.PROJECT:
        return {
          unique: ['projectId']
        }
      case LocalDB.STEP:
        return {
          unique: ['projectId', 'stepId'] 
        }
      default:
        return {}
    }
  }
  
  /**
   * get the variable dictionary by project id
   *
   * @projectId ID  the id of the project
   * 
   * @return Promise<VariableDictionary>
   */
  getProjectVariableDict(projectId: ID) :Promise<VariableDictionary> {
    log.debug('get project variable dict', projectId)
    let col = this.getOrCreateCollection(LocalDB.VARIABLE_DICT)
    return new Promise<VariableDictionary>(resolve => {
      let record = col.findOne({projectId})
      if (record) {
        resolve(record.dictionary)
      } else {
        resolve({})
      }
    })
  } 

  /**
   * save project variable dictionary
   * @projectId ID the id of the project
   * @dictionary VariableDictionary the dictionary
   * @return Promise<'OK'>
   */
  saveProjectVariableDict(projectId: ID, dictionary: VariableDictionary) {
    log.debug('save project variable dict', projectId, dictionary)
    let col = this.getOrCreateCollection(LocalDB.VARIABLE_DICT)
    let record = col.findOne({projectId})
    if (!record) {
      col.insert({projectId, dictionary})
    } else {
      record.dictionary = dictionary
      col.update(record)
    }
    // just return promise to unify the interface signature
    return new Promise<'OK'>(resolve=>{
      resolve('OK')
    })
  }

  loadProjects() :Promise<Array<PlainProject>> {
    log.debug('load projects')
    let projectCol = this.getOrCreateCollection(LocalDB.PROJECT)

    return new Promise<Array<PlainProject>>(resolve => {
      let projects = projectCol.find({})
      resolve(projects.map(project=>{
        let ret = { ...project }
        ret.id = project.projectId
        delete ret['projectId']
        return ret
      }))
    })
  }

  loadProject(id: ID) :Promise<PlainProject> {
    log.debug('get project', id)
    let projectCol = this.getOrCreateCollection(LocalDB.PROJECT)

    return new Promise<PlainProject>((resolve, reject)=>{
      // get project
      let project = projectCol.findOne({projectId:id})
      if (!project) {
        return reject(new Error('not found'))
      }

      let plainProject = {
        id,
        name: project.name,
        description: project.description,
        dag: project.dag,
        deleted: project.deleted,
        steps: project.steps,
        variables: project.variables,
        settings: {},
      }

      resolve(plainProject)
    })
  }

  saveProject(project: PlainProject) :Promise<'OK'> {
    log.debug('save project', project.id)
    let col = this.getOrCreateCollection(LocalDB.PROJECT)
    let record = col.findOne({projectId: project.id})
    if (!record) {
      col.insert({
        projectId: project.id,
        name: project.name,
        description: project.description,
        dag: project.dag,
        steps: project.steps,
        variables: project.variables,
        deleted: project.deleted,
      })
    } else {
      record.projectId = project.id
      record.name = project.name
      record.description = project.description
      record.dag = project.dag
      record.steps = project.steps
      record.variables = project.variables
      record.deleted = project.deleted
      col.update(record)
    }
    // just return promise to unify the interface signature
    return new Promise<'OK'>(resolve=>{
      resolve('OK')
    })
  }

  deleteProject(id: ID): Promise<'OK'> {
    log.debug('delete project', id)
    let col = this.getOrCreateCollection(LocalDB.PROJECT)
    col.findAndRemove({projectId: id})
    return new Promise<'OK'>(resolve=>{
      resolve('OK')
    })
  }
}

module.exports = LocalDB 
