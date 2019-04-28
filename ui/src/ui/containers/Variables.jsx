import React from 'react'

import { connect } from 'react-redux'

import { VariablesApp } from '../components/Variables'

import actions from '../actions'

const Variables = ({variables, onChangeVariables}) => {
  return (<VariablesApp 
    variables={variables}
    onChange={onChangeVariables}
  />)
}

const mapStateToProps = state => {
  let currentProjectId = state.model.currentProjectId
  let currentProject = state.model.projects[currentProjectId]

  let runtimeVariables = state.model.runtimeVariables || []
  let projectVariables = currentProject.variables || []

  let variables = [ ... projectVariables, ... runtimeVariables ]
  return {
    variables,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    onChangeVariables: variables => {
      dispatch(actions.Variables.changeVariables(variables))
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps,
)(Variables)
