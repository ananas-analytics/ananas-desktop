// @flow

import actions from './types'
import ObjectID from 'bson-objectid'

import type { ID, MessageLevel, MessageOptions, Dispatch, ThunkAction } from '../../common/model/flowtypes'

function displayMessage(title :string, level :MessageLevel, options: MessageOptions = {}) :ThunkAction {
  return (dispatch: Dispatch) => {
    // TODO: send notification according to window status
    let id = ObjectID.generate()
    dispatch({
      type: actions.DISPLAY_MESSAGE,
      id,
      title,
      level,
      options,
    })
    let timeout = 5000
    if (options.timeout && options.timeout > 0) {
      timeout = options.timeout
    }
    setTimeout(()=>{
      dispatch(disposeMessage(id))
    }, timeout)
  }
}

function dispatchDisplayMessage(dispatch: Dispatch, id: ID, title :string, level :MessageLevel, options: MessageOptions = {}) {
  dispatch({
    type: actions.DISPLAY_MESSAGE,
    id,
    title,
    level,
    options,
  })
  let timeout = 5000
  if (options.timeout && options.timeout > 0) {
    timeout = options.timeout
  }
  setTimeout(()=>{
    dispatch(disposeMessage(id))
  }, timeout)
}

function disposeMessage(id: ID) {
  return {
    type: actions.DISPOSE_MESSAGE,
    id,
  }
}

export default {
  dispatchDisplayMessage,
  displayMessage,
  disposeMessage,
}
