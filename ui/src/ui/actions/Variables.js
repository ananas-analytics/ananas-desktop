import actions from './types'

function selectVariable(variable) {
  return {
    type: actions.SELECT_VARIABLE,
    variable,
  }
}

function changeVariables(variables) {
  return (dispatch, getState, args) => {
    let model = getState().model
    let projectId = model.currentProjectId
    let project = model.projects[projectId]

    let tobesaved = { ...project }
    tobesaved.variables = variables
    args.modelService.saveProject(tobesaved)
      .then(() => {
        dispatch({
          type: actions.CHANGE_VARIABLES,
          variables,
        })
      })
      .catch(err=>{
        // TODO: handle error
        console.error(err)
      })
    }
}

export default {
  changeVariables,
}
