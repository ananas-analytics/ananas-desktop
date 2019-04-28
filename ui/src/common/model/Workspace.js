// @flow

const fs       = require('fs')
const util     = require('util')
const YAML     = require('yaml')
const ObjectID = require('bson-objectid')

const log     = require('../log')
const Project = require('./Project')

import type { ProjectMeta } from './flowtypes'

class Workspace {
	path: string
	projects: Array<ProjectMeta>

	constructor(path: string, workspace: any) {
		let obj = workspace || {}
		this.path = path
		this.projects = obj.projects || []
	}

	insertOrUpdateProject(project: ProjectMeta) {
		let exists = false
		this.projects.map(p => {
			if (p.id === project.id) {
				exists = true
				return project
			}
			return p
		})

		if (!exists) {
			this.projects.push(project)
		}
	}

	removeProject(projectId: string) {
		this.projects = this.projects.filter(project => project.id !== projectId)
	}

	toString() {
		return YAML.stringify({
			projects: this.projects
		})
	}

	toPlainObject() {
		return {
			projects: this.projects
		}
	}

	save() :Promise<any> {
		return util.promisify(fs.writeFile)(this.path, this.toString(), 'utf8')	
	}

	getProjectMeta(projectId: string) :?ProjectMeta {
		return this.projects.find(project => project.id === projectId)
	}

	importProject(projectPath: string) :Promise<Project> {
		let meta = this.projects.find(project => project.path === projectPath)
		if (!meta) {
			meta = { id: ObjectID.generate(), path: projectPath }
			return Project.Load(meta.path)
		} else {
			return Promise.reject(new Error('Project already exists'))
		}
	}

	loadProject(projectId: string) :Promise<Project> {
		let meta = this.projects.find(project => project.id === projectId)
		if (!meta) {
			return Promise.reject(new Error('Can NOT find project'))
		}
		return Project.Load(meta.path)
	}

	loadProjects() :Promise<Array<Project>> {
		let tasks = this.projects.map(project => {
			return Project.Load(project.path)
		})

		return Promise.all(tasks)
	}

	static Load(file: string) :Promise<Workspace> {
		return util.promisify(fs.readFile)(file) 
			.then(data => {
				let workspace = YAML.parse(data.toString())
				return new Workspace(file, workspace)
			})	
			.catch(err => {
				log.warn(err.message)	
				return Promise.resolve(new Workspace(file))
			})
			.then(workspace => {
				log.warn('return default workspace')
				return workspace
			})
	}
}

module.exports = Workspace
