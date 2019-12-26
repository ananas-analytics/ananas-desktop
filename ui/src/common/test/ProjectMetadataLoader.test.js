const path = require('path')
const ProjectMetadataLoader = require('../model/ProjectMetadataLoader')

describe('load project metadata', () => {
  test('should load metadata from project', async () => {
    let loader = ProjectMetadataLoader.getInstance()

    let dir = path.join(__dirname, 'resources/exampleExtensionProject')
    let metadata = await loader.loadFromDir(dir, {
      'ananas-extension-example': {
        version: '0.1.0',
        checksum: '',
        resolved: '', 
      }
    })

    expect(metadata.node.length).toBe(1)
    expect(Object.values(metadata.editor).length).toBe(1)
    expect(metadata.node[0].id).toBe('org.ananas.extension.example.transform.sql')
    expect(metadata.node[0].type).toBe('Transform')
    expect(metadata.editor['org.ananas.extension.example.transform.sql'].id)
      .toBe('org.ananas.extension.example.transform.sql')
  })
})

