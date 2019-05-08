const fs = require('fs')
const path = require('path')
const util = require('util')
const tmp = require('tmp')
const del = require('del')
const Project = require('../model/Project')
const MetadataLoader = require('../model/MetadataLoader')

// node metadata
let metadatas = null


describe('load project', () => {
  beforeEach(async () => {
    let loader = MetadataLoader.getInstance()
    let metadataPath = path.join(__dirname, 'resources/metadata')
    metadatas = await loader.loadFromDir(metadataPath)
  })
  test('should load project from a directory', async () => {
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

  test('should calculate layout if not exist', async () => {
    let projectPath = path.join(__dirname, 'resources/exampleProjectWithoutLayout')
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


describe('save and load project', () => {
  beforeEach(async () => {
    let loader = MetadataLoader.getInstance()
    let metadataPath = path.join(__dirname, 'resources/metadata')
    metadatas = await loader.loadFromDir(metadataPath)
  })

  test('should save and load project', async () => {
    let tmpPath = await util.promisify(tmp.dir)({ 
      prefix: 'ananas_test' 
    }) 

    // console.log(tmpPath)

    let plainProject = {
      id: 'example-project',
      name: 'Example Project',
      description: 'Example Project Description',
      dag: {
        nodes:  [
          {
            id: 'step1',
            label: 'CSV Source',
            metadata: {
              id: 'org.ananas.source.file.csv',
              description: 'Connect data from a CSV file',
              icon: 'images/csv.svg',
              name: 'CSV',
              options: {
                maxIncoming: 0,
                maxOutgoing: -1,
              },
              step: {
                type: 'connector',
                config: {
                  subtype: 'file',
                  format: 'csv',
                }
              },
              type: 'Source', 
            },
            type: 'Source',
            x: 50,
            y: 50,
          },
          {
            id: 'step2',
            label: 'SQL transformer',
            metadata: {
              id: 'org.ananas.transform.sql',
              description: 'Transform your data with SQL',
              icon: 'images/sql.svg',
              name: 'SQL',
              options: {
                maxIncoming: 1,
                maxOutgoing: -1,
              },
              step: {
                type: 'transformer',
                config: {
                  subtype: 'sql',
                }
              },
              type: 'Transform'
            },
            type: 'Transform',
            x: 150,
            y: 50,
          }
        ],
        connections: [
          { source: 'step1', target: 'step2' }
        ]
      },
      steps: {
        step1: {
          id: 'step1',
          name: 'CSV Source',
          description: 'CSV source description',
          metadataId: 'org.ananas.source.file.csv',
          type: 'connector',
          config: {
            subtype: 'file',
            format: 'csv',
          },
        },
        step2: {
          id: 'step2',
          name: 'SQL transformer',
          description: 'transform with sql',
          metadataId: 'org.ananas.transform.sql',
          type: 'transformer',
          config: {
            subtype: 'sql',
            sql: 'SELECT * FROM PCOLLECTIOn' ,
          },
        }
      },
      variables: []
    }

    let project = new Project(tmpPath, JSON.parse(JSON.stringify(plainProject)))
    await project.save()

    let loadedProject = await Project.Load(tmpPath, metadatas)


    let loaded = loadedProject.toPlainObject()

    expect(loaded.name).toEqual(plainProject.name)
    expect(loaded.description).toEqual(plainProject.description)
    expect(loaded.variables).toEqual(plainProject.variables)
    expect(loaded.dag.connections).toEqual(plainProject.dag.connections)
    expect(loaded.dag.nodes).toEqual(plainProject.dag.nodes)
    expect(loaded.steps).toEqual(plainProject.steps)

    let deleted = await del(tmpPath, {
      force: true,
    })
  })
})
