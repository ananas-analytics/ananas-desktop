import React from 'react'
import { connect } from 'react-redux'

// style related imports
import './App.scss'
import { getTheme } from './themes'
const theme = getTheme('ananas')

// component related imports
import { Box } from 'grommet/components/Box'
import { Grommet } from 'grommet/components/Grommet'

import AppDashboard from './components/AppDashboard'
import AppSideBar from './components/AppSideBar'
import AppToolBar from './components/AppToolBar'
import AppContainer from './containers/AppContainer'
import Messages from './components/Message'

import ServiceContext from './contexts/ServiceContext'

import actions from './actions'

const App = ({
  dispatch, // inject dispatch
  user,
  /* AppDashboard */
  projectId, onChangeCurrentProject, onNewProject, onUpdateProject, onDeleteProject,
  onLogout, onCheckUpdate,
  /* AppSideBar */
  activeMenu, expand, onClickAppMenu, onToggleAppSideBar,
  /* AppToolBar */
  contextSideBarExpanded, path, onToggleContextSideBar, onChangePath,
  /* Notification */
  showNotif, messages, onDisplayMessage, onDisposeMessage,
}) => {
  // register application level uncaught error handler, all uncaught error will be handled here
  if (window.onerror == null) {
    window.onerror = (errorMsg) => {
      onDisplayMessage('Uncaughted Error', 'warning', {
        body: errorMsg
      })
      return false
    }
  }

  // show project selection page if user not yet select project
  if (!projectId) {
    return (<Grommet theme={theme} full>
      <ServiceContext.Consumer>
        { ({modelService}) => (
        <AppDashboard dispatch={dispatch}
          modelService={modelService}
          onChangeCurrentProject={onChangeCurrentProject}
          onNewProject={onNewProject}
          onUpdateProject={onUpdateProject}
          onDeleteProject={onDeleteProject}
          onLogout={onLogout}
          onCheckUpdate={onCheckUpdate}
          onDisplayMessage={onDisplayMessage}
        />)
        }
      </ServiceContext.Consumer>
      { showNotif && (<Messages id='notification' messages={messages} onDispose={onDisposeMessage} />) }
    </Grommet>)
  }

  // show the main app
  return (
    <Grommet theme={theme} full>
      <Box direction='row' flex fill>
        <AppSideBar
          user={user}
          activeMenu={activeMenu}
          expand={expand}
          onClickMenu={onClickAppMenu}
          onToggleExpand={onToggleAppSideBar}
        />

        <Box direction='column' flex >
          <AppToolBar user={user} path={path}
            contextSideBarExpanded={contextSideBarExpanded}
            onToggleContextSideBar={onToggleContextSideBar}
            onChangePath={onChangePath}
          />
          <AppContainer />
        </Box>
        { showNotif && (<Messages id='notification' messages={messages} onDispose={onDisposeMessage} />) }
      </Box>
    </Grommet>
  )
}

const mapStateToProps = state => {
  let projectId = state.model.currentProjectId
  let messages = state.Message.messages
  return {
    user: state.model.user,
    projectId,

    /* AppSideBar */
    activeMenu: state.AppSideBar.activeMenu,
    expand: state.AppSideBar.expand,

    /* AppToolBar */
    path: state.AppToolBar.path,
    contextSideBarExpanded: state.AppToolBar.contextSideBarExpanded,

    /* Notification */
    showNotif: state.Message.open && messages.length > 0,
    messages,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    dispatch,
    /* AppDashboard */
    onLogout: () => {
      dispatch(actions.AppDashboard.logout())
    },
    onNewProject: project => {
      dispatch(actions.AppDashboard.newProject(project))
    },
    onUpdateProject: project => {
      dispatch(actions.AppDashboard.updateProject(project))
    },
    onDeleteProject: id => {
      dispatch(actions.AppDashboard.deleteProject(id))
    },
    onChangeCurrentProject: project => {
      dispatch(actions.AppDashboard.changeCurrentProject(project))
    },
    onCheckUpdate: () => {
      dispatch(actions.AppDashboard.checkUpdate())
    },
    /* AppSideBar */
    onClickAppMenu: index => {
      dispatch(actions.AppSideBar.clickAppMenu(index))
    },
    onToggleAppSideBar: () => {
      dispatch(actions.AppSideBar.toggleAppSideBar())
    },

    /* AppToolBar */
    onToggleContextSideBar: () => {
      dispatch(actions.AppToolBar.toggleContextSideBar())
    },
    onChangePath: path => {
      dispatch(actions.AppToolBar.changePath(path))
    },

    /* Notification */
    onDisplayMessage: (title, level, options) => {
      dispatch(actions.Message.displayMessage(title, level, options))
    },
    onDisposeMessage: (id) => {
      dispatch(actions.Message.disposeMessage(id))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(App)

