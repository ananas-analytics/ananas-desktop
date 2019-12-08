import actionTypes from '../actions/types'
import ObjectID from 'bson-objectid'

export default function(state, action) {
  // console.log('model:', state, action)
  switch(action.type) {
    case actionTypes.LOGIN:
      return handleLogin(state, action)
    case actionTypes.LOGOUT:
      return handleLogout(state, action)
    case actionTypes.NEW_PROJECT:
      return handleNewProject(state, action)
    case actionTypes.UPDATE_PROJECT:
      return handleUpdateProject(state, action)
    case actionTypes.CHANGE_PROJECT:
      return handleChangeProject(state, action)
    case actionTypes.PROJECT_LOADED:
      return handleProjectLoaded(state, action)
    case actionTypes.CHANGE_DAG:
      return handleChangeDAG(state, action)
    case actionTypes.SUBMIT_NODE_CONFIG:
      return handleSubmitNodeConfig(state, action)
    case actionTypes.CHANGE_VARIABLES:
      return handleChangeVariables(state, action)
    default:
      return defaultHandler(state, action)
  }
}

function defaultHandler(state, action) {
  if (!state) return {
    currentProject: null,
    projects: {},
  }
  return state
}

function handleLogin(state, action) {
  let newState = { ... state }
  newState.user = action.user
  return newState
}

function handleLogout(state, action) {
  let newState = { ... state }
  newState.user = null
  return newState
}

function handleNewProject(state, action) {
  let newState = { ... state }
  newState.projects[action.project.id] = action.project
  return newState
}

function handleUpdateProject(state, action) {
  let newState = { ... state }
  newState.projects[action.project.id] = action.project
  return newState
}

function handleChangeProject(state, action) {
  if (!action.id) {
    let newState = { ... state }
    newState.currentProjectId = null
    return newState
  }
  if (!state.projects.hasOwnProperty(action.id)) {
    return state
  }
  let newState = { ... state }
  newState.currentProjectId = action.id
  return newState
}

function handleProjectLoaded(state, action) {
  let newState = { ... state }
  let project = action.project
  newState.projects[project.id] = project
  return newState
}

function handleChangeDAG(state, action) {
  let newState = { ... state }
  let currentProjectId = newState.currentProjectId
  let currentProject = { ... newState.projects[currentProjectId] }

  currentProject.dag = {
    nodes: [ ... action.nodes ],
    connections: [ ... action.connections ]
  }
  newState.projects[currentProjectId] = currentProject

  switch(action.operation.type) {
    case 'NEW_NODE':
      newState = handleNewNode(newState, action.operation.node)
      break
    case 'DUPLICATE_NODE':
      newState = handleDuplicateNode(newState, action.operation.original, action.operation.newNode)
      break
    case 'DELETE_NODE':
      newState = handleDeleteNode(newState, action.operation.node)
      break
  }

  return newState
}

function handleNewNode(newState, node) {
  let currentProjectId = newState.currentProjectId
  let currentProject = { ... newState.projects[currentProjectId] }

  if (currentProject.steps.hasOwnProperty(node.id)) {
    return newState
  }

  let meta = node.metadata
  let newStep = {
    id: node.id,
    name: meta.name,
    description: meta.description,
    metadataId: meta.id,
    type: meta.step.type,
    config: { ... meta.step.config }, 
    variables: [],
    dirty: true,
  }

  currentProject.steps = { ... currentProject.steps }
  currentProject.steps[node.id] = newStep

  newState.projects[currentProjectId] = currentProject
  return newState
}

function handleDuplicateNode(newState, original, newNode) {
  let currentProjectId = newState.currentProjectId
  let currentProject = { ... newState.projects[currentProjectId] }

  if (currentProject.steps.hasOwnProperty(newNode.id)) {
    return newState
  }

  let meta = newNode.metadata
  let newStep = {
    id: newNode.id,
    metadataId: meta.id,
    name: meta.name,
    description: meta.description,
    type: meta.step.type,
    config: { ... meta.step.config }, 
    variables: [],
    dirty: true,
  }

  let originalStep = currentProject.steps[original.id]
  if (originalStep) {
    newStep.config = { ... originalStep.config }
  } 

  currentProject.steps = { ... currentProject.steps }
  currentProject.steps[newNode.id] = newStep

  newState.projects[currentProjectId] = currentProject
  return newState
}

function handleDeleteNode(newState, node) {
  let currentProjectId = newState.currentProjectId
  let currentProject = { ... newState.projects[currentProjectId] }

  if (!currentProject.steps.hasOwnProperty(node.id)) {
    return newState
  }
  currentProject.steps[node.id].deleted = true

  newState.projects[currentProjectId] = currentProject
  return newState
}

function handleSubmitNodeConfig(state, action) {
  let newState = { ... state }
  let currentProjectId = newState.currentProjectId
  let currentProject = { ... newState.projects[currentProjectId] }

  let step = currentProject.steps[action.stepId]
  if (!step) {
    // TODO: handle error
    return state
  }

  let config = action.config
  let newStep = { ... step }
  newStep.config = { ...config }
  // remove injected property
  newStep.name = config['__name__']
  newStep.description = config['__description__']
  newStep.dataframe = config['__dataframe__']
  newStep.metadataId = config['__metadataId__']
  
  delete newStep.config['__name__']
  delete newStep.config['__description__']
  delete newStep.config['__dataframe__']
  delete newStep.config['__metadataId__']

  let nodes = [ ... currentProject.dag.nodes ]
  nodes.forEach(node => {
    if (node.id === newStep.id) {
      node.label = newStep.name
    }
  })
  currentProject.dag.nodes = nodes

  newStep.expressions = action.expressions
  // update variables in the config
  
  currentProject.steps[action.stepId] = newStep
  newState.projects[currentProjectId] = currentProject
  return newState
}

function handleChangeVariables(state, action) {
  let newState = { ... state }
  let projectId = state.currentProjectId
  let project = state.projects[projectId]
  project.variables = action.variables
  return newState
}
