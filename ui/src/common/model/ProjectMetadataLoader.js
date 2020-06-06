// @flow

import fs from 'fs'
import path from 'path'
import util from 'util'
import readdirp from 'readdirp'
import YAML from 'yaml'

import promiseHelper from'../util/promise'

import type { PlainUIMetadata, PlainExtension } from './flowtypes'

export default class ProjectMetadataLoader {
  static INSTANCE :? ProjectMetadataLoader

  metadata :?{[string]: PlainUIMetadata}

  loadFromDir(dir: string, extensions: {[string]:PlainExtension}) :Promise<PlainUIMetadata> {
    // load project metadata from folder {PROJECT_HOME}/metadata/
    // metadata (node & editor descripor) are organized by extension
    let output = {}
    let extensionNames = []
    for (let name in extensions) {
      extensionNames.push(name)
    }
    let metaEntries = []
    return readdirp.promise(path.join(dir, 'metadata'), {
        fileFilter: ['*.yaml', '*.yml', '*.json'],
        directoryFilter: [ ... extensionNames, 'editor' ],
        depth: 2,
      })
      .then(entries => {
        // copy entries
        metaEntries = entries
        // read metadata
        let tasks = entries.map(entry => {
          for (let name in extensions) {
            if (entry.path.startsWith(path.join(name, 'metadata'))) {
              return {
                entry,
                extension: name,
              }
            }
          }
          return null
        })
        .filter(item => item !== null)
        .map(item => {
          return util.promisify(fs.readFile)(item.entry.fullPath)
            .then(content => {
              let txt = this.injectResourceRoot(content.toString(), dir, item.extension)
              if (item.entry.basename.endsWith('json')) {
                return JSON.parse(txt)
              } else {
                return YAML.parse(txt)
              }
            })
        })
        return promiseHelper.promiseAllWithoutError(tasks)
      })
      .then(metadatas => {
        let nodes = {}
        for (let meta of metadatas) {
          nodes = { ... nodes, ... meta }
        }
        output.node = Object.values(nodes)
        return metaEntries
      })
      .then(entries => {
        // read editor
        let tasks = entries.filter(entry => {
          for (let name in extensions) {
            if (entry.path.startsWith(path.join(name, 'editor'))) {
              return true
            }
          }
          return false
        }).map(entry => {
          return util.promisify(fs.readFile)(entry.fullPath)
            .then(content => {
              if (entry.basename.endsWith('json')) {
                return JSON.parse(content.toString())
              } else {
                return YAML.parse(content.toString())
              }
            })
        })
        return promiseHelper.promiseAllWithoutError(tasks)
      })
      .then(editors => {
        let editorMap = {}
        for (let editor of editors) {
          editorMap[editor.id] = editor
        }
        return editorMap
      })
      .then(editors => {
        output.editor = editors
        return output
      })
  }

  injectResourceRoot(content: string, projectPath: string, extension: string) :string {
    let resLocation = 'file://' + path.join(projectPath, 'metadata', extension, 'resource')
    return content.replace(/\${resource}/g, resLocation)
  }

  static getInstance() :ProjectMetadataLoader {
    if (ProjectMetadataLoader.INSTANCE !== null &&
        ProjectMetadataLoader.INSTANCE !== undefined ) {
      return ProjectMetadataLoader.INSTANCE
    }

    ProjectMetadataLoader.INSTANCE = new ProjectMetadataLoader()
    return ProjectMetadataLoader.INSTANCE
  }
}
