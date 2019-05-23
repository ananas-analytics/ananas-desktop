const path = require('path')
const EditorMetadataLoader = require('../model/EditorMetadataLoader')

describe('load metadata', () => {
  test('should load editor metadata from a directory', async () => {
    let loader = EditorMetadataLoader.getInstance() 

    let dir = path.join(__dirname, 'resources/editor')
    let metadatas = await loader.loadFromDir(dir)
    expect(metadatas['org.ananas.source.file.csv'].id).toBe('org.ananas.source.file.csv')
    expect(metadatas['org.ananas.source.file.csv'].layout.children.length).toBe(2)
    expect(Object.keys(metadatas['org.ananas.source.file.csv'].components).length).toBe(10)
  })

})
