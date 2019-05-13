import actionTypes from '../actions/types'

export default function(state, action) {
  console.log('Execution Engine reducer', action)
  switch(action.type) {
		case actionTypes.CHANGE_EXECUTION_ENGINES:
			return handleChangeExecutionEngines(state, action)
    default:
      return defaultHandler(state, action)
  }
}

function defaultHandler(state, action) {
  if (!state) return {
    engines: [],
    templates: {}
  }
  return state
}

function handleChangeExecutionEngines(state, action) {
	let newState = { ... state }
  newState.engines = [ ... action.engines ]
  console.log('change execution engines', newState.engines, action)
	return newState
}

