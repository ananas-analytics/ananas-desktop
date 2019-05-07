// @flow

const { 
	app, 
	dialog, 
	ipcMain } = require('electron')
const path  = require('path')

const LocalDB        = require('./LocalDB.js')
const log            = require('../common/log')
const Workspace      = require('../common/model/Workspace')
const Project        = require('../common/model/Project')
const User           = require('../common/model/User')
const MetadataLoader = require('../common/model/MetadataLoader')

const { getResourcePath } = require('./util')

// init localDB
const home = app.getPath('userData')
const dbPath = path.join(home, 'db')
const localDB = new LocalDB(dbPath)
log.debug('local db path', dbPath)

// load node metadata
let metadata = MetadataLoader.getInstance().loadFromDir(getResourcePath('metadata'))

ipcMain.on('login', (event, url, email, password) => {
	User.Login(url, email, password)
		.then(user => {
      event.sender.send('login-result', { code: 200, data: user.toPlainObject() })
    })
    .catch(err => {
      event.sender.send('login-result', { code: 500, message: err.message })
    })
})

ipcMain.on('get-variable-dict', (event, projectId) => {
  localDB.getProjectVariableDict(projectId)
    .then(dict => {
      event.sender.send('get-variable-dict-result', { code: 200, data: dict })
    })
    .catch(err => {
      event.sender.send('get-variable-dict-result', { code: 500, message: err.message })
    })
})

ipcMain.on('save-variable-dict', (event, projectId, dict) => {
  localDB.saveProjectVariableDict(projectId, dict)
    .then(() => {
      event.sender.send('save-variable-dict-result', { code: 200, data: 'OK' })
    })
    .catch(err => {
      event.sender.send('save-variable-dict-result', { code: 500, message: err.message })
    })
})

ipcMain.on('load-projects', (event) => {
	Workspace.Load(path.join(home, 'workspace.yml'))
		.then(workspace => {
			return workspace.loadProjects(metadata)
		})
		.then(projects => {
      log.debug('load projects', projects)
			let plainProjects = projects.map(project => project.toPlainObject())
      event.sender.send('load-projects-result', { code: 200, data: plainProjects })
		})
		.catch(err => {
      log.debug('failed to load projects', err.stack)
      event.sender.send('load-projects-result', { code: 500, message: err.message })
    })
})

ipcMain.on('load-project', (event, projectId) => {
	Workspace.Load(path.join(home, 'workspace.yml'))
		.then(workspace => {
			return workspace.loadProject(projectId, metadata)
		})
		.then(project => {
      event.sender.send('load-project-result', { code: 200, data: project.toPlainObject() })
		})
		.catch(err => {
      event.sender.send('load-project-result', { code: 500, message: err.message })
    })
})

ipcMain.on('import-project', (event) => {
	dialog.showOpenDialog({
		title: 'Import Ananas Project',
		defaultPath: app.getPath('home'),
		buttonLabel: 'Import',
		properties: ['openDirectory'],
		message: 'Select Ananas Project'
	}, (filePaths) => {
		log.debug('import project', filePaths)
		if (!filePaths || filePaths.length === 0) {
			return event.sender.send('import-project-result', { code: 500, message: 'cancelled' })
		} 		

		let wks
		let tmpProject
		Project.VerifyProject(filePaths[0])
			.then(() => {
				return Workspace.Load(path.join(home, 'workspace.yml'))
			}) 
			.then(workspace => {
				wks = workspace
				return workspace.importProject(filePaths[0], metadata)
			})
			.then(project => {
				tmpProject = project	
				return wks.save()
			})
			.then(() => {
				event.sender.send('import-project-result', { code: 200, data: tmpProject.toPlainObject() })
			})
			.catch(err => {
				event.sender.send('import-project-result', { code: 500, message: err.message })
			})
	})
})

ipcMain.on('save-project', (event, project) => {
	Workspace.Load(path.join(home, 'workspace.yml'))
		.then(workspace => {
			if (!project.path) {
				project.path = path.join(home, project.id)
			}
			workspace.insertOrUpdateProject({
				id: project.id,
				path: project.path,
			})
			return workspace.save()
		})
		.then(() => {
			let projectObject = new Project(project.path, project)
			return projectObject.save()
		})
		.then(() => {
      event.sender.send('save-project-result', { code: 200, data: 'OK' })
		})
		.catch(err => {
      event.sender.send('save-project-result', { code: 500, message: err.message })
    })
})

ipcMain.on('delete-project', (event, projectId) => {
	Workspace.Load(path.join(home, 'workspace.yml'))
		.then(workspace => {
			workspace.removeProject(projectId)
			return workspace.save()
		})
    .then(() => {
      event.sender.send('delete-project-result', { code: 200, data: 'OK' })
    })
    .catch(err => {
      event.sender.send('delete-project-result', { code: 500, message: err.message })
    })
})
