import React from 'react'

import { connect } from 'react-redux'

import { Box } from 'grommet/components/Box'
import { EnginesApp } from '../components/ExecutionEngine'

import actions from '../actions'

const ExecutionEngine = ({ engines, templates, onChangeEngines }) => {
  return (
    <EnginesApp
      engines={engines}
      templates={templates}
      onChange={onChangeEngines}
    />
  )
}

const mapStateToProps = state => {
  return {
    engines: state.ExecutionEngine.engines,
    templates: state.ExecutionEngine.templates,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    onChangeEngines: (engines) => {
      dispatch(actions.ExecutionEngine.changeExecutionEngines(engines))
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps,
)(ExecutionEngine)
