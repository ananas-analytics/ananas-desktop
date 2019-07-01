import actionTypes from '../actions/types'

export default function(state, action) {
  switch(action.type) {
    case actionTypes.CHANGE_TRIGGERS:
      return handleChangeTriggers(state, action) 
    default:
      return defaultHandler(state, action)
  }
}

function defaultHandler(state, action) {
  if (!state) return {
    triggers: [],
  }
  return state
}

function handleChangeTriggers(state, action) {
  let newState = { ... state }
  newState.triggers = [ ... action.triggers ]
  
}
