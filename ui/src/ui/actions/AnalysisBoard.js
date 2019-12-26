import actions from './types'

function changeDAG(nodes, connections, operation) {
  return (dispatch, getState, args) => {
    dispatch({
      type: actions.CHANGE_DAG,
      nodes,
      connections,
      operation: operation,
    })

    let state = getState()
    let projectId = state.model.currentProjectId
    let project = state.model.projects[projectId]

    args.modelService.saveProject(project)
      .then(()=>{
        // do nothing for now
      })
      .catch(err=>{
        // do nothing for now
      })

  }
}


function closeNodeEditor(id) {
  return {
    type: actions.CLOSE_NODE_EDITOR,
    id,
  }
}

function openNodeEditor(id) {
  return {
    type: actions.OPEN_NODE_EDITOR,
    id,
  }
}

function selectNode(node) {
  return {
    type: actions.SELECT_NODE,
    node,
  }
}

function submitNodeConfig(stepId, config) {
  return (dispatch, getState, {modelService, variableService}) => {
    // get expressions from config
    let expressions = variableService.parseStepConfig(config)
    dispatch({
      type: actions.SUBMIT_NODE_CONFIG,
      stepId,
      config,
      expressions,
    })

    let state = getState()
    let projectId = state.model.currentProjectId
    let project = { ... state.model.projects[projectId] }
    delete project['metadata']

    modelService.saveProject(project)
      .then(()=>{
        // do nothing for now
        console.log('save project done!')
      })
      .catch(err=>{
        // do nothing for now
        console.error(err)
      })
  }
}

export default {
  changeDAG,
  closeNodeEditor,
  openNodeEditor,
  selectNode,
  submitNodeConfig,
}
