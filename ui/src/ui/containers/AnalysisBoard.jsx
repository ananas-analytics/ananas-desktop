import React from 'react'
import { connect } from 'react-redux'

import styled from 'styled-components'

import { Box } from 'grommet/components/Box'

import { DAGEditor, DataTableView } from '../components/DAGEditor'
import { NodeEditor } from '../components/NodeEditor'
import { Close } from 'grommet-icons'

import ServiceContext from '../contexts/ServiceContext'

import actions from '../actions'

const Board = styled(Box)`
  position: relative;
`

const NodeEditorContainer = styled(Box)`
  bottom: 0;
  left: 0;
  opacity: 1;
  position: absolute;
  right: 0;
  top: 0;
`

const CloseButton = styled(Box)`
  &:hover {
    background: ${props => props.theme.global.colors['light-2']}
    top: 11px;
    right: 11px;
  }

  cursor: pointer;
  position: absolute;
  top: 10px;
  right: 10px;
`

const AnalysisBoard = ({ dispatch,
  user, project, dag, step, variables, engines, editors, showEditor,

  onChangeDAG, onCloseNodeEditor, onOpenNodeEditor, onSelectNode, onNewNode,
  onDeleteNode, onSubmitNodeConfig,
}) => {
  if (!dag) dag = { nodes: [], connections: [] }

  return (
    <Board direction='column' fill>
      <DAGEditor
        nodes={dag.nodes}
        connections={dag.connections}
        onChange={onChangeDAG}
        onSelectionChange={onSelectNode}
        onConfigureNode={onOpenNodeEditor}
        onNewNode={onNewNode}
        onDeleteNode={onDeleteNode}
      />
      <DataTableView user={user} project={project} step={step}/>

      {
        showEditor ? (
          <NodeEditorContainer background='light-1' fill >
            <ServiceContext.Consumer>
            { 
              services => (
              <NodeEditor
                user={user}
                dispatch={dispatch}
                project={project}
                dag={dag}
                step={step}
                variables={variables}
                editors={editors}
                engines={engines}
                services={services}
                onSubmit={v=>onSubmitNodeConfig(step.id, v)}
              />)
            }
            </ServiceContext.Consumer>
            <CloseButton elevation='small' pad='small' round
              onClick={()=>onCloseNodeEditor(step.id)}>
              <Close color='brandLight'/>
            </CloseButton>
          </NodeEditorContainer>
        ) : null
      }
    </Board>
  )
}

const mapStateToProps = state => {
  let currentProjectId = state.model.currentProjectId
  let currentStepId = state.AnalysisBoard.currentStepId
  let currentProject = state.model.projects[currentProjectId]
  if (!currentProject) {
    currentProject = {}
  }
  let editors = {}
  if (currentProject.metadata) {
    editors = currentProject.metadata.editor || {}
  }

  return {
    user: state.model.user,
    project: currentProject,
    dag: currentProject.dag,
    step: currentProject.steps[currentStepId],
    variables: [ ...state.model.runtimeVariables, ...currentProject.variables ],
    engines: state.ExecutionEngine.engines,
    editors: { ... state.model.metadata.editor, ... editors },

    showEditor: state.AnalysisBoard.showEditor,
    nodes: currentProject.dag.nodes,
    connections: currentProject.dag.connections,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    dispatch: dispatch,
    onChangeDAG: (nodes, connections, operation) => {
      dispatch(actions.AnalysisBoard.changeDAG(nodes, connections, operation))
    },
    onCloseNodeEditor: id => {
      dispatch(actions.AnalysisBoard.closeNodeEditor(id))
    },
    onSelectNode: node => {
      dispatch(actions.AnalysisBoard.selectNode(node))
    },
    onOpenNodeEditor: id => {
      dispatch(actions.AnalysisBoard.openNodeEditor(id))
    },
    onNewNode: node => {
      dispatch(actions.AnalysisBoard.newNode(node))
    },
    onDeleteNode: node => {
      dispatch(actions.AnalysisBoard.deleteNode(node))
    },
    onSubmitNodeConfig: (stepId, config) => {
      dispatch(actions.AnalysisBoard.submitNodeConfig(stepId, config))
    },
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps,
)(AnalysisBoard)
