import React from 'react'

import { connect } from 'react-redux'

import { ExtensionsApp } from '../components/Extensions'

import actions from '../actions'

const Extensions = ({ projectId, extensions }) => {
  return (
    <ExtensionsApp
      projectId={projectId}
      extensions={extensions}
    />
  )
}

const mapStateToProps = state => {
  let projectId = state.model.currentProjectId
  let currentProject = state.model.projects[projectId]
  let extensions = currentProject.extensions || {}
  return {
    projectId,
    extensions,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    onChangeTriggers: (triggers) => {
      dispatch(actions.Scheduler.changeTrigger(triggers))
    }
  }
}

export default connect(
  mapStateToProps,
  mapDispatchToProps,
)(Extensions)
