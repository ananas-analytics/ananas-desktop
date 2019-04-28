// @flow

import React, { Component } from 'react'

import { Box } from 'grommet/components/Box'
import { Select } from 'grommet/components/Select'
import { Text } from 'grommet/components/Text'

import type { ID, NodeEditorContext } from '../../../../common/model/flowtypes.js'

type Props = {
  context: NodeEditorContext, 
  label: string,
  value: ID,
  onChange: (string)=>void,
}

type Option = {
  label: string,
  value: any,
}

export default class InputStepSelector extends Component<Props> {
  options: Array<Option> = []

  constructor(props: Props) {
    super(props)
    let step = this.props.context.step
    let steps = this.props.context.project.steps
    let connections = this.props.context.project.dag.connections
    this.options = connections.filter(c => {
      return c.target === step.id && steps.hasOwnProperty(c.source)
    })
    .map(c => {
      return steps[c.source]
    })
    .map(step => {
      return {
        label: step.name,
        value: step.id,
      }
    })
  }

  render() {  
    let valueObj = this.options.find(option => option.value === this.props.value)
    if (!valueObj) {
      valueObj = undefined
    }

    return (<Box margin={{vertical: 'small'}} flex={false}>
      { typeof this.props.label === 'string' ? <Text size='small' margin={{bottom: 'xsmall'}}>{this.props.label}</Text> : null }
      <Select
        value={valueObj}
        options={this.options}
        valueKey='value'
        labelKey='label'
        onChange={e=>this.props.onChange(e.value.value)}
      />
    </Box>)   
  }
}
