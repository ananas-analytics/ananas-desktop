import React from 'react'
import { connect } from 'react-redux'

import proxy from '../proxy'

import { Box } from 'grommet/components/Box'
import { Collapsible } from 'grommet/components/Collapsible'

import AnalysisBoard from './AnalysisBoard'
import Variables from './Variables'
import ExecutionEngine from './ExecutionEngine'
import Extensions from './Extensions'
// import Scheduler from './Scheduler'
import { DAGEditorSideBar, NodeEditorSideBar } from '../components/DAGEditorSideBar'

import NodeType from '../components/DAGEditorSideBar/models/NodeType'

const renderActiveApp = activeApp => {
  switch(activeApp) {
    case 0:
      return <AnalysisBoard />
    case 1:
      return <ExecutionEngine />
    case 2:
      return <Variables />
    case 3:
      // return <Scheduler />
      return <Extensions />
    default:
      return null // <Box>AppIndex: {activeApp}</Box>
  }
}

const renderActiveContextSideBar = (activeApp, options) => {
    switch(activeApp) {
    case 0:
      if (!options.showNodeEditor) {
        let items = options.nodeMetadata.map(item => NodeType.fromObject(item))
          .filter(item => {
            if (proxy.getSharedVariable('devMode')) {
              return true
            }
            return !item.id.startsWith('org.ananas.dev')
          })
        return <DAGEditorSideBar items={items} />
      } else {
        return <NodeEditorSideBar />
      }
    default:
      return null // <Box>AppIndex: {activeApp}</Box>
  }
}

const AppContainer = ({ activeApp, contextSideBarExpanded, showNodeEditor, nodeMetadata }) => {
  return (
    <Box direction='row' overflow={{horizontal: 'hidden'}}flex >
      <Box flex >
        { renderActiveApp(activeApp) }
      </Box>
      <Collapsible direction='horizontal' open={contextSideBarExpanded} fill>
        <Box border='left' flex>
          { renderActiveContextSideBar(activeApp, { nodeMetadata, showNodeEditor }) }
        </Box>
      </Collapsible>
    </Box>
  )
}

const mapStateToProps = state => {
  let currentProjectId = state.model.currentProjectId
  let currentProject = state.model.projects[currentProjectId]
  if (!currentProject) {
    currentProject = {}
  }
  let nodes = []
  if (currentProject.metadata) {
    nodes = currentProject.metadata.node || []
  }

  return {
    activeApp: state.AppSideBar.activeMenu,
    contextSideBarExpanded: state.AppToolBar.contextSideBarExpanded,
    showNodeEditor: state.AnalysisBoard.showEditor,
    nodeMetadata: [ ... nodes, ...state.model.metadata.node ],
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

export default connect(
  mapStateToProps,
  mapDispatchToProps,
)(AppContainer)
