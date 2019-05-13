import actionTypes from '../actions/types'

export default function(state, action) {
  //console.log('AppToolBar Reducer', state, action)
  switch(action.type) {
    case actionTypes.CHANGE_PROJECT:
      return handleChangeProject(state, action)
    case actionTypes.CLICK_APP_MENU:
      return handleClickAppMenu(state, action)
    case actionTypes.TOGGLE_CONTEXT_SIDEBAR:
      return handleToggleContextSideBar(state, action)
    case actionTypes.CHANGE_PATH:
      return handleChangePath(state, action)
    case actionTypes.SELECT_NODE:
      return handleSelectNode(state, action)
    default:
      return defaultHandler(state, action)
  }
}

function defaultHandler(state, action) {
  if (!state) return {
    contextSideBarExpanded: true,
    path: {
      project: {},
      app: {},
    }
  }
  return state
}

function handleChangeProject(state, action) {
  let newState = { ... state }
  if (!action.id) {
    newState.path = {}
  } else {
    newState.path = {
      project: { id: action.project.id, name: action.project.name },
      app: {
        id: 1,
        name: 'Analysis Board',
      }
    }
  }
  return newState
}

function handleToggleContextSideBar(state, action) {
  let newState = { ... state }
  newState.contextSideBarExpanded = !newState.contextSideBarExpanded
  return newState
}

function handleClickAppMenu(state, action) {
  let newState = { ... state }
  // TODO: get shared app names
	// const appNames = ['Analysis Board', 'Report', 'Automation', 'Monitoring', 'Variables']
  const appNames = ['Analysis Board', 'Execution Engine', 'Variables']
  newState.path.app = {
    id: action.index,
    name: appNames[action.index]
  }
  return newState
}

function handleChangePath(state, action) {
  let newState = { ... state }
  newState.path = { ... action.path }
  return newState
}

function handleSelectNode(state, action) {
  let newState = { ... state }
  newState.path = { ... newState.path }
  if (action.node) {
    if (!newState.path.app) {
      newState.path.app = {}
    }
    newState.path.app.view = {
      id: action.node.id,
      name: action.node.label,
    }
  } else {
    newState.path.app.view = null
  }
  return newState
}
