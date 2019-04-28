import React from 'react'
import { connect } from 'react-redux'

import { Box } from 'grommet/components/Box'
import { Collapsible } from 'grommet/components/Collapsible'

import AnalysisBoard from './AnalysisBoard'
import Variables from './Variables'
import { DAGEditorSideBar, NodeEditorSideBar } from '../components/DAGEditorSideBar'

import NodeType from '../components/DAGEditorSideBar/models/NodeType'

import { getNodeTypes } from '../api'

const renderActiveApp = activeApp => {
  switch(activeApp) {
    case 0:
      return <AnalysisBoard />
    case 1:
			return <Variables />
    default:
			return null // <Box>AppIndex: {activeApp}</Box>
  }
}

const renderActiveContextSideBar = (activeApp, options) => {
    switch(activeApp) {
    case 0:
      if (!options.showNodeEditor) {
        let items = getNodeTypes()
        items = items.map(item => NodeType.fromObject(item))
        return <DAGEditorSideBar items={items} />
      } else {
        return <NodeEditorSideBar />
      }
    default:
			return null // <Box>AppIndex: {activeApp}</Box>
  }
}

const AppContainer = ({ activeApp, contextSideBarExpanded, showNodeEditor }) => {
  return (
    <Box direction='row' overflow={{horizontal: 'hidden'}}flex >
      <Box flex >
        { renderActiveApp(activeApp) }
      </Box>
      <Collapsible direction='horizontal' open={contextSideBarExpanded} fill>
        <Box border='left' flex>
          { renderActiveContextSideBar(activeApp, { showNodeEditor }) }
        </Box>
      </Collapsible>
    </Box>
  )
}

const mapStateToProps = state => {
  return {
    activeApp: state.AppSideBar.activeMenu,
    contextSideBarExpanded: state.AppToolBar.contextSideBarExpanded,
    showNodeEditor: state.AnalysisBoard.showEditor,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

export default connect(
  mapStateToProps,
  mapDispatchToProps,
)(AppContainer)
