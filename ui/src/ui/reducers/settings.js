import actionTypes from '../actions/types'

export default function(state, action) {
	switch(action.type) {
		default: 
			return defaultHandler(state, action)
	}
}

function defaultHandler(state, action) {
	if (!state) {
		return {
			apiEndpoint: 'http://ananasanalytics.com/api/v1',
			runnerEndpoint: 'http://localhost:3003'
		}
	}
	return state
}
