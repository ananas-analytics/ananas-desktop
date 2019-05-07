// @flow 

const fs       = require('fs')
const util     = require('util')
const readdirp = require('readdirp')
const YAML     = require('yaml')

import type { PlainNodeMetadata } from './flowtypes'

class MetadataLoader {
  static INSTANCE :?MetadataLoader

  metadata :?{[string]: PlainNodeMetadata}

  static defaultProps = {
    metadata: null
  }

  loadFromDir(dir: string) :Promise<{[string]: PlainNodeMetadata}> {
    return readdirp.promise(dir, {
      fileFilter: ['*.yaml', '*.yml'],
    }) 
    .then(entries => {
      let tasks = entries.map(entry => {
        return util.promisify(fs.readFile)(entry.fullPath) 
      })
      // FIXIT: Promise.all fails when one task fails
      return Promise.all(tasks)
    })
    .then(contents => {
      let output = {}
      contents.map(content => {
        return YAML.parse(content.toString())
      })
      .forEach(meta => {
        output = { ... output, ... meta }
      })
      return output
    })
  }

  static getInstance() :MetadataLoader {
    if (MetadataLoader.INSTANCE !== null &&
        MetadataLoader.INSTANCE !== undefined) {
      return MetadataLoader.INSTANCE
    } 

    MetadataLoader.INSTANCE = new MetadataLoader()
    return MetadataLoader.INSTANCE
  }

} 

module.exports = MetadataLoader
