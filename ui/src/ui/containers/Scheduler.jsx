import React from 'react'

import { connect } from 'react-redux'

import { Box } from 'grommet/components/Box'
import { SchedulerApp } from '../components/Scheduler'

import actions from '../actions'

const Scheduler = ({ projectId, engines, triggers, onChangeTriggers }) => {
  return (
    <SchedulerApp
      projectId={projectId}
      engines={engines}
      triggers={triggers}
      onChange={onChangeTriggers}
    />
  )
}

const mapStateToProps = state => {
  let projectId = state.model.currentProjectId
  let currentProject = state.model.projects[projectId]
  return {
    projectId,
    engines: state.ExecutionEngine.engines,
    triggers: currentProject.triggers,
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
)(Scheduler)
