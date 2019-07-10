// @flow

import actions from './types'

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
  return (dispatch: Dispatch, getState: GetState, {modelService}: ThunkActionArgs) => {
    modelService.saveProject(project)
      .then(() => {
        dispatch({
          type: actions.UPDATE_PROJECT,
          project,
        })
      })
      .catch(err => {
        // TODO:
      })
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
  return (dispatch: Dispatch, getState: GetState, {modelService, variableService}: ThunkActionArgs) => {
    if (!id) {
      dispatch({
        type: actions.CHANGE_PROJECT,
        id: null,
      })
    }

    variableService.clearCache()
    // first load the current project
    modelService.loadProject(id)
      .then((project)=>{
        // clear variable cache
        dispatch(projectLoaded(project))

        dispatch({
          type: actions.CHANGE_PROJECT,
          id,
          project,
        })
      })
      .catch(err=>{
        console.error(err)
      })
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

export default {
  changeCurrentProject,
  newProject,
  updateProject,
  deleteProject,
  projectLoaded,
  logout,
}
