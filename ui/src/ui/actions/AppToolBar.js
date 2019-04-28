import actions from './types'

function toggleContextSideBar() {
  return {
    type: actions.TOGGLE_CONTEXT_SIDEBAR,
  }
}

function changePath(path) {
  return (dispatch) => {
    dispatch({
      type: actions.CHANGE_PATH,
      path,
    })

    if (path.app && path.app.name) {
      dispatch({
        type: actions.CLICK_APP_MENU,
        index: path.app.id,
      })
    } else {
      // remove current project, and back to dashboard
      dispatch({
        type: actions.CHANGE_PROJECT,
        id: null,
      })
    }
  }
}


export default {
  toggleContextSideBar,
  changePath,
}
