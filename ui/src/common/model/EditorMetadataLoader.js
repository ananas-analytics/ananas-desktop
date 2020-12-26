// @flow

import fs from 'fs'
import util from 'util'
import readdirp from 'readdirp'
import YAML from 'yaml'

import promiseHelper from '../util/promise'

class EditorMetadataLoader {
  static INSTANCE :?EditorMetadataLoader

  metadata :?{[string]: any}

  static defaultProps = {
    metadata: null,
  }

  loadFromDir(dir: string) :Promise<{[string]: any}> {
    return readdirp.promise(dir, {
      fileFilter: ['*.yaml', '*.yml', '*.json'],
    })
    .then(entries => {
      let tasks = entries.map(entry => {
        return util.promisify(fs.readFile)(entry.fullPath)
          .then(content => {
            try {
              if (entry.basename.endsWith('json')) {
                return JSON.parse(content.toString())
              } else {
                return YAML.parse(content.toString())
              }
            } catch (err) {
              return {
              }
            }
          })
      })
      return promiseHelper.promiseAllWithoutError(tasks)
    })
    .then(editors => {
      let output = {}
      editors.forEach(editor => {
        if (editor.id) {
          output[editor.id] = editor
        }
      })
      return output
    })
  }

  static getInstance() :EditorMetadataLoader {
    if (EditorMetadataLoader.INSTANCE !== null &&
        EditorMetadataLoader.INSTANCE !== undefined) {
      return EditorMetadataLoader.INSTANCE
    }

    EditorMetadataLoader.INSTANCE = new EditorMetadataLoader()
    return EditorMetadataLoader.INSTANCE
  }
}

export default EditorMetadataLoader
