// @flow

const fs       = require('fs')
const path     = require('path')
const util     = require('util')
const YAML     = require('yaml')
const ObjectID = require('bson-objectid')
const mkdirp   = require('mkdirp')


const log = require('../log')
const { calculateLayout } = require('../util/dag')

import type { PlainProject, PlainNodeMetadata } from './flowtypes'

class Project { 
  path: string
  project: PlainProject

  constructor(projectPath: string, project: PlainProject) {
    this.path = projectPath
    this.project = project
  }

  save() :Promise<any> { 
    log.debug('save project', this.project.id, 'to', this.path)
    let description = this.project.description
    let settings = this.project.settings
    let projectData = { ... this.project }  
    delete projectData['description']
    delete projectData['path']
    delete projectData['settings']

    // let dataframe = {schema: {}, data: []}
    // remove internal config property 
    let newSteps = {}
    for (let k in projectData.steps) {
      if (!projectData.steps[k].deleted) {
        newSteps[k] = projectData.steps[k]
        delete newSteps[k]['dirty']
        delete newSteps[k]['expressions']
        delete newSteps[k]['variables']
        // clean up dataframe
        if (newSteps[k].hasOwnProperty('dataframe')) {
          newSteps[k].dataframe.data = [] 
          /*
          if (typeof newSteps[k].dataframe.schema === 'object' && 
              Array.isArray(newSteps[k].dataframe.schema.fields)) {
            newSteps[k].dataframe.schema.fields = newSteps[k].dataframe.schema.fields.map(field => {
              return {
                name: field.name,
                type: field.type,
              }
            })
          }
          */
          /*
          dataframe[k] = {
            schema: newSteps[k].dataframe.schema,
            data: newSteps[k].dataframe.data.slice(0, 5),
          }
          delete newSteps[k]['dataframe']
          */
        }

        let config = newSteps[k].config
        for (let configKey in config) {
          if (configKey.startsWith('__') && configKey.endsWith('__')) {
            delete newSteps[k].config[configKey]
          }
        }
      }
    }
    projectData.steps = newSteps

    // remove internal variable fields
    projectData.variables = projectData.variables.map(v => {
      return {
        name: v.name,
        description: v.description,
        scope: v.scope,
        type: v.type,
      }
    })

    // remove nodes in dag and build layout
    let nodes = projectData.dag ? projectData.dag.nodes : []
    let layout = nodes.map(node => {
      return {
        id: node.id,
        metadataId: node.metadata.id, // keep the reference to metadata
        x: node.x,
        y: node.y,
      }
    })
    delete projectData.dag['nodes']

    // remove triggers
    let triggers = projectData.triggers || []

    let projectContent = YAML.stringify(projectData)

    let ananasFile = path.join(this.path, 'ananas.yml')
    return util.promisify(mkdirp)(this.path)
      .then(() => {
        // save ananas.yml
        return util.promisify(fs.writeFile)(ananasFile, projectContent, 'utf8')
      })
      .then(() => {
        // save readme
        return util.promisify(fs.writeFile)(path.join(this.path, 'README.md'), description, 'utf8')
      })
      .then(() => {
        return util.promisify(fs.writeFile)(path.join(this.path, 'settings.yml'), YAML.stringify(settings), 'utf8')
      })
      .then(() => {
        // save layout
        return util.promisify(fs.writeFile)(path.join(this.path, 'layout.yml'), YAML.stringify(layout), 'utf8')
      })
      .then(() => {
        return util.promisify(fs.writeFile)(path.join(this.path, 'triggers.yml'), YAML.stringify(triggers), 'utf8')
      })
  }

  toPlainObject() :PlainProject {
    // return validate project
    if (!this.project.hasOwnProperty('dag')) {
      this.project.dag = { 
        nodes: [],
        connections: [],
      }
    }
    if (!this.project.hasOwnProperty('steps')) {
      this.project.steps = {}
    }
    if (!this.project.hasOwnProperty('variables')) {
      this.project.variables = []
    }
    this.project.path = this.path
    return this.project
  }

  static VerifyProject(projectPath: string) :Promise<true> {
    return util.promisify(fs.readFile)(path.join(projectPath, 'ananas.yml'))
      .then(data => {
        YAML.parse(data.toString())
        return true
      })
  }

  static Load(projectPath: string, metadata: {[string]: PlainNodeMetadata}) :Promise<Project> {
    // default project
    let projectData = {
      id: ObjectID.generate(),
      name: 'untitled project', 
      description: '# untitled project',
      dag: {
        connections: [],
        nodes: [],
      },
      steps: {},
      variables: [],
      settings: {},
      triggers: [],
    }

    let layout = []
    return util.promisify(fs.readFile)(path.join(projectPath, 'ananas.yml'))
      .then(data => {
        projectData = YAML.parse(data.toString())
        return util.promisify(fs.readFile)(path.join(projectPath, 'README.md'))
      })
      .catch(err => { // default README content
        log.warn(err.message)
        return Promise.resolve('')
      })
      .then(data => {
        projectData.description = data.toString()
        if (!projectData.id) {
          projectData.id = ObjectID.generate()
        }
      })
      .then(() => {
         return util.promisify(fs.readFile)(path.join(projectPath, 'layout.yml'))
      })
      .then(data => {
        // parse layout
        layout = YAML.parse(data.toString())
        if (!layout) layout = []
        return layout
      })
      .catch(() => { // default layout
        // calculate the layout
        log.error('failed to load layout, now calculating it')
        let stepList = []
        for (let key in projectData.steps) {
          stepList.push(projectData.steps[key])
        }
        layout = calculateLayout(stepList, projectData.dag.connections) 
        log.debug(layout)
        return Promise.resolve('')
      })
      .then(() => {
        return layout
          .filter(node => metadata.hasOwnProperty(node.metadataId))
          .map(node => {
            let step = projectData.steps[node.id] || {}
            return {
              id       : node.id,
              label    : step.name,
              type     : metadata[node.metadataId].type,
              x        : node.x,
              y        : node.y,
              metadata : metadata[node.metadataId],
            }
          })  
      })
      .then(nodes => {
        projectData.dag.nodes = nodes
      })
      .then(() => {
        return util.promisify(fs.readFile)(path.join(projectPath, 'settings.yml'))
      })
      .then(data => {
        let settings = YAML.parse(data.toString()) 
        return settings
      })
      .catch(() => {
        return Promise.resolve({}) 
      })
      .then(settings => {
        projectData.settings = settings
      })
      .then(() => {
        return util.promisify(fs.readFile)(path.join(projectPath, 'triggers.yml'))
      })
      .then(data => {
        return YAML.parse(data.toString())
      })
      .catch(()=> {
        return Promise.resolve([])
      })
      .then(triggers => {
        projectData.triggers = triggers
        return new Project(projectPath, projectData)
      })
  }
}

module.exports = Project
