// @flow

import React, { Component } from 'react'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { Select } from 'grommet/components/Select'

import { FormClose, FormAdd } from 'grommet-icons'

import type { NodeEditorContext } from '../../../../common/model/flowtypes.js'

type Props = {
  label: string,
  value: {[string]:string},
  context: NodeEditorContext,
  leftConfigKey: string,
  rightConfigKey: string,
  onChange: (any)=>void,
}

type State = {
  value: Array<Array<string>>,
  newLeft: string,
  newRight: string,
}

export default class ColumnMap extends Component<Props, State> {
  state = {
    value: [],
    newLeft: '',
    newRight: '',
  }

  constructor(props: Props) {
    super(props)
    let defaultValue = this.props.value || {}
    let value = Object.keys(defaultValue).map(key => {
      return [key, defaultValue[key]]
    })
    this.state = {
      value,
      newLeft: '',
      newRight: '',
    } 
  }

  handleLeftChanged(v: string) {
    this.setState({newLeft: v})
  }

  handleRightChanged(v: string) {
    this.setState({newRight: v})
  }

  handleAddNew() {
    if (this.state.newLeft === '' || this.state.newRight === '') {
      return
    }
    let value = [ ...this.state.value ]
    value.push([this.state.newLeft, this.state.newRight])
    this.setState({value, newLeft:'', newRight: ''})
    this.submitChange(value)
  }

  handleDeleteMapItem(index: number) {
    let value = this.state.value.filter((v, i) => i !== index)
    this.setState({value})
    this.submitChange(value)
  }

  submitChange(value:Array<Array<string>>) {
    let map = {}
    value.map(v => {
      map[v[0]] = v[1]
    })
    this.props.onChange(map)
  }

  getDataframes() {
    let steps = this.props.context.project.steps
    let step = this.props.context.step
    let config = step.config
    let leftStepId = config[this.props.leftConfigKey]
    let rightStepId = config[this.props.rightConfigKey]

    let leftStep = steps[leftStepId] || { dataframe: { schema: {fields: []}, data: [] } }
    let rightStep = steps[rightStepId] || { dataframe: { schema: {fields: []}, data: [] } }

    return { left: leftStep.dataframe, right: rightStep.dataframe }
  } 

  render() {
    let { left, right } = this.getDataframes()  
    if (!left) left = { schema: { fields: [], data: [] } }
    if (!right) right = { schema: { fields: [], data: [] } }
    let leftOptions = left.schema.fields.map(f => f.name)
    let rightOptions = right.schema.fields.map(f => f.name)
    return (<Box flex={false} direction='column' margin={{vertical: 'small'}}>
      { typeof this.props.label === 'string' ? <Text size='small' margin={{bottom: 'xsmall'}}>{this.props.label}</Text> : null }
      <Box border='all' pad='small'>
      <table>
        <thead>
          <tr><td><Text size='small' color='brand'>Left Column</Text></td><td><Text size='small' color='brand'>Right Column</Text></td><td></td></tr>
        </thead>
        <tbody>
          {
            this.state.value.map((row, index) => {
            return (<tr key={index}>
              <td><Text size='small'>{row[0]}</Text></td>
              <td><Text size='small'>{row[1]}</Text></td>
              <td><div onClick={()=>this.handleDeleteMapItem(index)}><FormClose /></div></td></tr>)
            })
          }
          <tr>
            <td><Select plain value={this.state.newLeft} options={leftOptions} onChange={({option})=>this.handleLeftChanged(option)}/></td>
            <td><Select plain value={this.state.newRight} options={rightOptions} onChange={({option})=>this.handleRightChanged(option)}/></td>
            <td><div onClick={()=>this.handleAddNew()}><FormAdd /></div></td>
          </tr>
        </tbody>
      </table>  
      </Box>
    </Box>)
  }
}

  
