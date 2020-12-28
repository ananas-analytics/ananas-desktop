// @flow

import React, { Component } from 'react'

import styled from 'styled-components'

import { Box } from 'grommet/components/Box'
import { Image } from 'grommet/components/Image'
import { Stack } from 'grommet/components/Stack'
import { DropButton } from 'grommet/components/DropButton'
import { ResponsiveContext } from 'grommet/contexts/ResponsiveContext'

import { Add, UserSettings, Download } from 'grommet-icons'


import ProjectItem, { Item } from './ProjectItem'
import ProjectConfigurer from './ProjectConfigurer'
import UserMenu from './UserMenu'
import Loading from '../Common/Loading'

import type { Node } from 'react'
import type ModelService from '../../service/ModelService'
import type { MessageOptions, PlainProject, ID, Dispatch } from '../../../common/model/flowtypes.js'

type Props = {
  dispatch: Dispatch,
  modelService: ModelService,

  onNewProject: (PlainProject) => void,
  onUpdateProject: (PlainProject) => void,
  onDeleteProject: (ID) => void,

  onChangeCurrentProject: (ID) => void,

  onLogout: () => void,
  onCheckUpdate: () => void,

  onDisplayMessage: (title:string, level:string, options:MessageOptions) => void
}

type State = {
  projects: {[string]:PlainProject},
  selected: ?PlainProject,
  editing: boolean,
  loading: boolean,
}

const AutoOverflowStack = styled(Stack)`
  overflow: auto;
`


export default class AppDashboard extends Component<Props, State> {
  static defaultProps = {
    onNewProject: ()=>{},
    onUpdateProject: ()=>{},
    onDeleteProject: ()=>{},
    onChangeCurrentProject: ()=>{},
    onLogout: ()=>{},
    onCheckUpdate: ()=>{},
  }

  state = {
    projects: {}, // this.props.projects,
    selected: null,
    editing: false,
    loading: true,
  }

  componentDidMount() {
    this.loadProjects()
  }

  async loadProjects() {
    // load projects
    // get project index, and load them
    let modelService = this.props.modelService

    try {
      let projects = await modelService.loadProjects()
      // dispatch loaded
      let newStateProjects = {}
      projects.forEach(project=>{
        newStateProjects[project.id] = project
      })
      this.setState({projects: newStateProjects, loading: false})
    } catch(err) {
      this.props.onDisplayMessage('Failed to load projects', 'warning', {
        body: err.message
      })
    }
  }

  handleNewProject() {
    this.setState({editing: true, selected: null})
  }

  async handleImportProject() {
    let modelService = this.props.modelService
    try {
      let project = await modelService.importProject()
      this.handleSubmitChange(project)
    } catch(err) {
      this.props.onDisplayMessage('Failed to import project', 'danger', {
        body: err.message
      })
    }
  }

  handleDeleteProject(id: ID) {
    let projects = { ... this.state.projects }
    delete projects[id]
    this.setState({projects, selected: null, loading: false, editing: false})
    this.props.onDeleteProject(id)
  }

  handleSubmitChange(project: PlainProject) {
    if (!this.state.selected) {
      let projects = this.state.projects
      projects[project.id] = project
      this.setState({editing: false, projects, selected: null})
      this.props.onNewProject(project)
    } else {
      let projects = this.state.projects
      projects[project.id] = project
      this.setState({editing: false, projects, selected: null})
      this.props.onUpdateProject(project)
    }
  }

  handleEditProject(projectId: string) {
    this.props.onChangeCurrentProject(projectId)
  }

  renderProjects(size /*:string*/) :Node {
    let projects = this.state.projects
    let columnCount = 3
    switch(size) {
      case 'small':
        columnCount = 1
        break
      case 'medium':
        columnCount = 2
        break
      case 'large':
        columnCount = 3
        break
      default:
        columnCount = 3
    }
    let rows = [
      [
        (<Item key='add-project' align='center' justify='center'
          onClick={()=>this.handleNewProject()}>
            <Add color='light-4' size='large' />
        </Item>),
        (<Item key='import-project' align='center' justify='center'
          onClick={()=>this.handleImportProject()}>
          <Download color='light-4' size='large' />
        </Item>),
      ] // first row
    ]
    let count = 0
    for (let id in projects) {
      if (projects[id].deleted) {
        continue // filter the deleted ones
      }
      if ((count + 2) % columnCount === 0) {
        rows.push([])
      }
      rows[rows.length-1].push(
        <ProjectItem key={id}
          id={id}
          name={projects[id].name}
          path={projects[id].path}
          description={projects[id].description.split('\n')[0]}
          selected={this.state.selected != null && this.state.selected.id === id}
          onClick={()=>this.setState({selected: projects[id]})}
          onConfig={()=>this.setState({editing: true})}
          onEdit={()=>this.handleEditProject(id)}
          onDelete={()=>this.handleDeleteProject(id)}
          getProjectPath={id=>this.props.modelService.getProjectPath(id)}
        />
      )
      count++
    }
    // add placeholder for the last row if necessary
    for (let i = rows[rows.length-1].length; i < columnCount; i++) {
      rows[rows.length-1].push(
        <Box key={i} margin='12px' width='100%'/>
      )
    }
    return rows.map((row, index) => {
      return <Box key={index} direction='row' fill='horizontal' flex={false}>
        {row}
      </Box>
    })
  }

  render() {
    return (<Box direction='column' fill>
      <Box align='center' background='brand' direction='row' elevation='xsmall'
        height='48px' justify='between' pad='xsmall'>
        <Box align='center' direction='row'>
          <Image src='images/logo-white.png' fit='contain' style={{height: '26px'}} />
        </Box>
        <Box align='center' direction='row' >
          <DropButton alignSelf='center' dropContent={<UserMenu
              onCheckUpdate={this.props.onCheckUpdate}
              onLogout={this.props.onLogout} />}
            dropAlign={{ top: 'bottom' }} >
            <UserSettings size='24px'/>
          </DropButton>
        </Box>
      </Box>
      { this.state.loading ?
        <Loading />
        :
        <AutoOverflowStack fill>
          <Box fill
            overflow={{vertical: 'auto'}}
          >
          <ResponsiveContext.Consumer>
            { size => (
            <Box align='center' direction='column' justify='start' pad='medium'
              margin={{horizontal: 'xlarge'}} flex fill='vertical'
              onClick={()=>{this.setState({selected: null})}}
            >
              { this.renderProjects(size) }
            </Box>)
            }
          </ResponsiveContext.Consumer>
          </Box>
          {
          this.state.editing ? (
          <ProjectConfigurer project={this.state.selected}
            onSubmit={(project)=>{
              this.handleSubmitChange(project)
            }}
            onCancel={()=>this.setState({editing: false})}
          />
          ) : null
          }
        </AutoOverflowStack>
      }
    </Box>)
  }
}
