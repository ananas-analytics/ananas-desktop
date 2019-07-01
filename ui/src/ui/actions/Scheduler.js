import actions from './types'

function changeTrigger(triggers) {
  return (dispatch, getState, args) => {
    args.schedulerService.saveTriggers(triggers)
      .then(() => {
        dispatch({
          type: actions.CHANGE_TRIGGERS,
          triggers,
        })
      })
      .catch(err => {
        // TODO: handle error here
      })
  }
}

export default {
  changeTrigger,
}
