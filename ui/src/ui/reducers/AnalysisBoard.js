import actionTypes from '../actions/types'

export default function(state, action) {
  switch(action.type) {
    case actionTypes.CLOSE_NODE_EDITOR:
      return handleCloseNodeEditor(state, action)
    case actionTypes.OPEN_NODE_EDITOR:
      return handleOpenNodeEditor(state, action)
    case actionTypes.SELECT_NODE:
      return handleSelectNode(state, action)
    default:
      return defaultHandler(state, action)
  }
}

function defaultHandler(state, action) {
  if (!state) return {
    currentStepId: null,
    showEditor: true,
  }
  return state
}

function handleCloseNodeEditor(state, action) {
  let newState = { ... state }
  newState.currentStepId = action.id
  newState.showEditor = false
  return newState
}

function handleOpenNodeEditor(state, action) {
  let newState = { ... state }
  newState.currentStepId = action.id
  newState.showEditor = true
  return newState
}

function handleSelectNode(state, action) {
  let newState = { ... state }
  if (action.node) {
    newState.currentStepId = action.node.id
  } else {
    newState.currentStepId = null
  }
  return newState
}
