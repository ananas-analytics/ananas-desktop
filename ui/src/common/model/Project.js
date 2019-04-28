// @flow

const fs       = require('fs')
const path     = require('path')
const util     = require('util')
const YAML     = require('yaml')
const ObjectID = require('bson-objectid')
const mkdirp   = require('mkdirp')

const log = require('../log')

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
    let projectData = {}
    return util.promisify(fs.readFile)(path.join(projectPath, 'ananas.yml'))
      .then(data => {
        projectData = YAML.parse(data.toString())
        return util.promisify(fs.readFile)(path.join(projectPath, 'README.md'))
      })
      .catch(err => {
        return Promise.resolve('')
      })
      .then(data => {
        projectData.description = data.toString()
        if (!projectData.id) {
          projectData.id = ObjectID.generate()
        }
        return new Project(projectPath, projectData)
      })
      .catch(err => {
        log.warn(err.message) 
        return new Project(projectPath, { 
          id: ObjectID.generate(),
          name: 'untitled project', 
          description: '# untitled project',
          dag: {
            connections: [],
            nodes: [],
          },
          steps: {},
          variables: [],
        })
      })
  }
}

module.exports = Project
