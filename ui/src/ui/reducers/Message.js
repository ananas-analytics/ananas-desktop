import actionTypes from '../actions/types'

export default function(state, action) {
  switch(action.type) {
		case actionTypes.DISPLAY_MESSAGE:
			return handleDisplayMessage(state, action)
		case actionTypes.DISPOSE_MESSAGE:
			return handleDisposeMessage(state, action)
    default:
      return defaultHandler(state, action)
  }
}

function defaultHandler(state, action) {
  if (!state) return {
    open: false,
		messages: []
  }
  return state
}

function handleDisplayMessage(state, action) {
	let newState = { ... state }
	newState.messages.push({
		id: action.id,
		title: action.title,
		level: action.level,
		options: action.options,
	})
	return newState
}

function handleDisposeMessage(state, action) {
	let newState = { ... state }
	newState.messages = state.messages.filter(msg=> msg.id !== action.id)
	return newState
}
