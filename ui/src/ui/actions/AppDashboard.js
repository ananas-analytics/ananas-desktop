// @flow

import actions from './types'
import Message from './Message'

import type { ID, PlainProject, Dispatch, GetState, ThunkActionArgs } from '../../common/model/flowtypes.js'


function newProject(project: PlainProject) {
  return (dispatch: Dispatch, getState: GetState, {modelService}: ThunkActionArgs) => {
    modelService.saveProject(project)
      .then(() => {
        dispatch({
          type: actions.NEW_PROJECT,
          project,
        })
      })
      .catch(err => {
        // TODO:
      })
  }
}

function updateProject(project: PlainProject) {
  return async (dispatch: Dispatch, getState: GetState, {modelService}: ThunkActionArgs) => {
    try {
      await modelService.saveProject(project, true) // shallow save
    } catch (err) {
      // TODO:
    }
  }
}

function deleteProject(projectId: ID) {
  return (dispatch: Dispatch, getState: GetState, {modelService}: ThunkActionArgs) => {
    modelService.deleteProject(projectId)
      .then(() => {
        dispatch({
          type: actions.DELETE_PROJECT,
          projectId,
        })
      })
      .catch(err => {
        // TODO:
      })
  }
}

function changeCurrentProject(id: string) {
  return async (dispatch: Dispatch, getState: GetState, {modelService, variableService}: ThunkActionArgs) => {
    if (!id) {
      dispatch({
        type: actions.CHANGE_PROJECT,
        id: null,
      })
    }

    variableService.clearCache()
    // first load the current project
    try {
      let project = await modelService.loadProject(id)
      // clear variable cache
      dispatch(projectLoaded(project))
      dispatch({
        type: actions.CHANGE_PROJECT,
        id,
        project,
      })
    } catch(err){
      Message.dispatchDisplayMessage(dispatch, id, err.message, 'danger', {})
    }
  }
}

function projectLoaded(project: PlainProject) {
  return {
    type: actions.PROJECT_LOADED,
    project,
  }
}

function logout() {
  return {
    type: actions.LOGOUT,
  }
}

function checkUpdate() {
  return (dispatch: Dispatch, getState: GetState, {proxy}: ThunkActionArgs) => {
    proxy.checkUpdate(true)
      .then(() => {
        dispatch({
          type: actions.CHECK_UPDATE
        })
      })
  }
}

export default {
  changeCurrentProject,
  newProject,
  updateProject,
  deleteProject,
  projectLoaded,
  logout,
  checkUpdate,
}
