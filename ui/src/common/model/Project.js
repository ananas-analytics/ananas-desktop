// @flow

import fs from 'fs'
import path from 'path'
import util from 'util'
import YAML from 'yaml'
import ObjectID from 'bson-objectid'
import mkdirp from 'mkdirp'

const fsPromises = fs.promises

import log from '../log'
import { calculateLayout } from '../util/dag'
import ProjectMetadataLoader from './ProjectMetadataLoader'
import ExtensionHelper from './ExtensionHelper'


import type { PlainProject, PlainNodeMetadata } from './flowtypes'

export default class Project {
  path: string
  project: PlainProject
  valid: boolean

  constructor(projectPath: string, project: PlainProject) {
    this.path = projectPath
    this.project = project
    this.valid = true
  }

  async save(shallowSave: boolean) :Promise<any> {
    log.debug('save project', this.project.id, 'to', this.path)
    let description = this.project.description
    let settings = this.project.settings
    let projectData = { ... this.project }
    let extensions = projectData.extensions || {}

    delete projectData['description']
    delete projectData['path']
    delete projectData['settings']
    delete projectData['extensions']
    delete projectData['metadata']

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
    if (!nodes) {
      nodes = []
    }
    log.debug('prepare layout to save', nodes)
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

    log.debug('writing files ...')

    await util.promisify(mkdirp)(this.path)
    await util.promisify(fs.writeFile)(ananasFile, projectContent, 'utf8')
    await util.promisify(fs.writeFile)(path.join(this.path, 'README.md'), description, 'utf8')
    if (!shallowSave) {
      await util.promisify(fs.writeFile)(path.join(this.path, 'settings.yml'), YAML.stringify(settings), 'utf8')
      await util.promisify(fs.writeFile)(path.join(this.path, 'layout.yml'), YAML.stringify(layout), 'utf8')
      await util.promisify(fs.writeFile)(path.join(this.path, 'triggers.yml'), YAML.stringify(triggers), 'utf8')
      await util.promisify(fs.writeFile)(path.join(this.path, 'extension.yml'), YAML.stringify(extensions), 'utf8')
    } else {
      log.debug('shallow save, ignore settings, layout, and extensions')
    }

    log.debug('save done.')
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

  static getMetadataType(type: string) :string {
    switch(type) {
      case 'connector':
        return 'Source'
      case 'transformer':
        return 'Transform'
      case 'loader':
        return 'Destination'
      case 'viewer':
        return 'Visualization'
    }
    return 'Transform'
  }

  static getDebugMetadataId(type: string) :string {
    let mode = global.shared.devMode ? 'debug' : 'unknown'
    let prefix = 'org.ananas.dev.'
    switch(type) {
      case 'connector':
        return prefix + mode + '.source'
      case 'transformer':
        return prefix + mode + '.transform'
      case 'loader':
        return prefix + mode + '.destination'
      case 'viewer':
        return prefix + mode + '.visualization'
    }
    return prefix + mode + '.transform'

  }

  // $FlowFixMe
  static getNodeFromLayoutItem(node, projectData, globalNodeMeta, projectNodeMeta) {
    let step = projectData.steps[node.id] || {}
    // merge global node metadata with project node metadata
    let metadata = { ... projectNodeMeta, ... globalNodeMeta }

    if (Object.prototype.hasOwnProperty.call(metadata, node.metadataId)) {
      return {
        id       : node.id,
        label    : step.name,
        type     : metadata[node.metadataId].type,
        x        : node.x,
        y        : node.y,
        metadata : metadata[node.metadataId],
      }
    } else {
      let metadataId = Project.getDebugMetadataId(step.type)
      let metadataObj = { ... metadata[metadataId] }
      metadataObj.id = step.metadataId
      return {
        id       : node.id,
        label    : step.name,
        type     : Project.getMetadataType(step.type),
        x        : node.x,
        y        : node.y,
        metadata : metadataObj,
      }
    }
  }

  static async Load(projectPath: string, metadata: {[string]: PlainNodeMetadata}, shallowLoad: boolean) :Promise<Project> {
    // $FlowFixMe
    log.debug(`Load project ${projectPath}. shallowMode: ${shallowLoad}`)
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
      extensions: {},
      metadata: {
        node: [],
        editor: {},
      },
    }

    try {
      await fsPromises.access(path.join(projectPath, 'ananas.yml'), fs.constants.F_OK)
    } catch (err) {
      let projectObject = new Project(projectPath, projectData)
      // mark this project as not valid, so that the workspace can ignore it
      projectObject.valid = false
      return projectObject
    }

    let layout = []
    // ananas.yml and readme
    try {
      const ananasYml = await fsPromises.readFile(path.join(projectPath, 'ananas.yml'))
      projectData = YAML.parse(ananasYml.toString())
      const readMeYml  = await fsPromises.readFile(path.join(projectPath, 'README.md'))
      projectData.description = readMeYml.toString()
    } catch (err) {
      log.warn(err.message)
      projectData.description = ''
    }
    if (!projectData.id) {
      projectData.id = ObjectID.generate()
    }

    // extension
    try {
      const extensionYml = await fsPromises.readFile(path.join(projectPath, 'extension.yml'))
      projectData.extensions = YAML.parse(extensionYml.toString())
    } catch (err) {
      log.info(err.message)
      projectData.extensions = {}
    }

    if (!shallowLoad) {
      // Do NOT install extension in shallowLoad mode
      await ExtensionHelper.MakeSureAllExtensionsInstalled(projectPath, projectData.extensions)
      // $FlowFixMe
      projectData.metadata = await ProjectMetadataLoader.getInstance().loadFromDir(projectPath,
        projectData.extensions)

      // layout
      try {
        const layoutYml = await fsPromises.readFile(path.join(projectPath, 'layout.yml'))
        layout = YAML.parse(layoutYml.toString())
        if (!layout) layout = []
      } catch(err) {
        // calculate the layout
        log.error('failed to load layout, now calculating it')
        let stepList = []
        for (let key in projectData.steps) {
          stepList.push(projectData.steps[key])
        }
        layout = calculateLayout(stepList, projectData.dag.connections)
        log.debug(layout)
      }
      const nodes = layout.map(node => {
          let projectNodeMeta = {}
          for (let n of projectData.metadata.node) {
            projectNodeMeta[n.id] = n
          }
          return Project.getNodeFromLayoutItem(node, projectData, metadata, projectNodeMeta)
        })
      projectData.dag.nodes = nodes
    }


    // settings
    try {
      const settingsYml = await fsPromises.readFile(path.join(projectPath, 'settings.yml'))
      const settings = YAML.parse(settingsYml.toString())
      projectData.settings = settings
    } catch (err) {
      projectData.settings = {}
    }

    // await new Promise(resolve => setTimeout(resolve, 5000))

    return new Project(projectPath, projectData)

    /*
    // TODO: check file existance in an async way
    if (!fs.existsSync(path.join(projectPath, 'ananas.yml'))) {
      //$FlowFixMe
      let projectObject = new Project(projectPath, projectData)
      // mark this project as not valid, so that the workspace can ignore it
      projectObject.valid = false
      return Promise.resolve(projectObject)
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
      // load extensions
      .then(() => {
        return util.promisify(fs.readFile)(path.join(projectPath, 'extension.yml'))
      })
      .then(data => {
        return YAML.parse(data.toString())
      })
      .catch(()=> {
        return Promise.resolve({})
      })
      .then(extensions => {
        projectData.extensions = extensions
        return extensions
      })
      // load metadata
      .then(extensions => {
        console.log('--------------> load metadata from extensions')
        console.log(extensions)
        return ProjectMetadataLoader.getInstance().loadFromDir(projectPath, extensions)
      })
      .then(metadata => {
        projectData.metadata = metadata
        return projectData
      })
      // load layout or calculate it
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
          .map(node => {
            let projectNodeMeta = {}
            for (let n of projectData.metadata.node) {
              projectNodeMeta[n.id] = n
            }
            return Project.getNodeFromLayoutItem(node, projectData, metadata, projectNodeMeta)
          })
      })
      .then(nodes => {
        projectData.dag.nodes = nodes
      })
      // load settings
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
            // finally return project object
      .then(() => {
        return new Project(projectPath, projectData)
      })
      */
  }
}
