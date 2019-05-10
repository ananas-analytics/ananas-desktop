const path = require('path')
const MetadataLoader = require('../model/MetadataLoader')

describe('load metadata', () => {
  test('should load metadata from a directory', async () => {
    let loader = MetadataLoader.getInstance() 

    let dir = path.join(__dirname, 'resources/metadata')
    let metadatas = await loader.loadFromDir(dir)
    expect(Object.values(metadatas).length).toBe(6) 
    expect(metadatas['org.ananas.source.file.csv'].name).toBe('CSV')
    expect(metadatas['org.ananas.source.file.csv'].icon).toBe('images/csv.svg')
    expect(metadatas['org.ananas.source.file.csv'].options.maxIncoming).toBe(0)
    expect(metadatas['org.ananas.source.file.csv'].options.maxOutgoing).toBe(-1)
  })

})
