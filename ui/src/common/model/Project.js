// @flow

const fs       = require('fs')
const path     = require('path')
const util     = require('util')
const YAML     = require('yaml')
const ObjectID = require('bson-objectid')
const mkdirp   = require('mkdirp')

const log            = require('../log')
const MetadataLoader = require('./MetadataLoader')

import type { PlainProject } from './flowtypes'

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
    let projectData = { ... this.project }  
    delete projectData['description']
    delete projectData['path']

    // remove internal config property 
    let newSteps = {}
    for (let k in projectData.steps) {
      if (!projectData.steps[k].deleted) {
        newSteps[k] = projectData.steps[k]
        delete newSteps[k]['dirty']
        delete newSteps[k]['expressions']
        delete newSteps[k]['variables']
        // keep only 10 data from dataframe if exists
        if (newSteps[k].hasOwnProperty('dataframe') 
            && Array.isArray(newSteps[k].dataframe.data)) {
          let sampleData = newSteps[k].dataframe.data.slice(0, 5)
          newSteps[k].dataframe.data = sampleData 
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
        metadata: node.metadata.id,
        x: node.x,
        y: node.y,
      }
    })

    let projectContent = YAML.stringify(projectData)

    let ananasFile = path.join(this.path, 'ananas.yml')
    return util.promisify(mkdirp)(this.path)
      .then(() => {
        return util.promisify(fs.writeFile)(ananasFile, projectContent, 'utf8')
      })
      .then(() => {
        // save readme
        return util.promisify(fs.writeFile)(path.join(this.path, 'README.md'), description, 'utf8')
      })
      .then(() => {
        return util.promisify(fs.writeFile)(path.join(this.path, 'layout.yml'), layout, 'utf8')
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

  static Load(projectPath: string) :Promise<Project> {
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
        //return new Project(projectPath, projectData)
      })
      .then(() => {
         return util.promisify(fs.readFile)(path.join(projectPath, 'layout.yml'))
      })
      .then(data => {
        // parse layout
        layout = YAML.parse(data.toString())
      })
      .catch(() => { // default layout
        return Promise.resolve([])
      })
      .then(() => {
        // load node metadata 
        return MetadataLoader.getInstance().load()
      })
      .then(metadata => {
        return layout
          .filter(node => metadata.hasOwnProperty(node.meta))
          .map(node => {
            return {
              id       : node.id,
              label    : node.name,
              type     : metadata[node.metadata].type,
              x        : node.x,
              y        : node.y,
              metadata : metadata[node.metadata],
            }
          })  
      })
      .then(nodes => {
        projectData.dag.nodes = nodes
        return new Project(projectPath, projectData)
      })
      
  }
}

module.exports = Project
