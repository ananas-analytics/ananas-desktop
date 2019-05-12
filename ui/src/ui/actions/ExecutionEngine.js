import actions from './types'

function changeExecutionEngines(engines) {
  return (dispatch, getState, args) => {
    args.modelService.saveExecutionEngines(engines)
      .then(() => {
        dispatch({
          type: actions.CHANGE_EXECUTION_ENGINES,
          engines,
        })
      })
      .catch(err => {
        // TODO: handle error here
      })
  }
}

export default {
  changeExecutionEngines,
}
