const path = require('path')
const Project = require('../model/Project')
const MetadataLoader = require('../model/MetadataLoader')

describe('load project', () => {
  test('should load project from a directory', async () => {
    let loader = MetadataLoader.getInstance()
    let metadataPath = path.join(__dirname, 'resources/metadata')
    let metadatas = await loader.loadFromDir(metadataPath)

    let projectPath = path.join(__dirname, 'resources/exampleProject')
    let project = await Project.Load(projectPath, metadatas)
    
    let plainProject = project.toPlainObject()
    expect(plainProject.id).toBe('example-project')
    expect(plainProject.name).toBe('Example Project')
    expect(plainProject.variables).toEqual([])
    expect(plainProject.dag.connections.length).toBe(2)
    expect(plainProject.dag.nodes.length).toBe(3)
    expect(Object.values(plainProject.steps).length).toBe(3)
    
  })
})
