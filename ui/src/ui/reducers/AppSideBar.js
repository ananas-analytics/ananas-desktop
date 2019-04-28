import actionTypes from '../actions/types'

export default function(state, action) {
  switch(action.type) {
    case actionTypes.CLICK_APP_MENU:
      return handleClickAppMenu(state, action)
    case actionTypes.TOGGLE_APP_SIDEBAR:
      return handleToggleAppSideBar(state, action)
    default:
      return defaultHandler(state, action)
  }
}

function defaultHandler(state, action) {
  if (!state) return {
    activeMenu: 0,
    expand: true,
  }
  return state
}

function handleClickAppMenu(state, action) {
  let newState = { ... state }
  newState.activeMenu = action.index
  return newState
}

function handleToggleAppSideBar(state, action) {
  let newState = { ... state }
  newState.expand = !newState.expand
  return newState
}
