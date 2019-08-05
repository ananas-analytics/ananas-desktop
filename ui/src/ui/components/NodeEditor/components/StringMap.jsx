// @flow

import React, { Component } from 'react'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { TextInput } from 'grommet/components/TextInput'

import { FormClose, FormAdd } from 'grommet-icons'

import type { NodeEditorContext } from '../../../../common/model/flowtypes.js'

type Props = {
  label: string,
  value: {[string]:string},
  keyLabel: string,
  valueLabel: string,
  context: NodeEditorContext,
  onChange: (any)=>void,
}

type State = {
  value: Array<Array<string>>,
  newKey: string,
  newValue: string,
}

export default class ColumnMap extends Component<Props, State> {
  static defaultProps = {
    keyLabel: 'Name',
    valueLabel: 'Value',
  }

  state = {
    value: [],
    newKey: '',
    newValue: '' 
  }

  constructor(props: Props) {
    super(props)
    let defaultValue = this.props.value || {}
    let value = Object.keys(defaultValue).map(key => {
      return [key, defaultValue[key]]
    })
    this.state = {
      value,
      newKey: '',
      newValue: '',
    } 
  }

  handleLeftChanged(v: string) {
    this.setState({newKey: v})
  }

  handleRightChanged(v: string) {
    this.setState({newValue: v})
  }

  handleAddNew() {
    let value = [ ...this.state.value ]
    value.push([this.state.newKey, this.state.newValue])
    this.setState({value, newKey:'', newValue: ''})
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

  render() {
    return (<Box flex={false} direction='column' margin={{vertical: 'small'}}>
      { typeof this.props.label === 'string' ? <Text size='small' margin={{bottom: 'xsmall'}}>{this.props.label}</Text> : null }
      <Box border='all' pad='small'>
      <table>
        <thead>
          <tr><td><Text size='small' color='brand'>{this.props.keyLabel}</Text></td><td><Text size='small' color='brand'>{this.props.valueLabel}</Text></td><td></td></tr>
        </thead>
        <tbody>
          {
            this.state.value.map((row, index) => {
            return (<tr key={index} style={{maxWidth: '300px'}}>
              <td style={{minWidth: '120px', overflow: 'hidden'}}><Text truncate={true} size='small'>{row[0]}</Text></td>
              <td style={{minWidth: '120px', maxWidth: '120px', overflow: 'hidden'}}><Text truncate={true} size='small'>{row[1]}</Text></td>
              <td><div onClick={()=>this.handleDeleteMapItem(index)}><FormClose /></div></td></tr>)
            })
          }
          <tr>
            <td><TextInput value={this.state.newKey} onChange={v=>this.handleLeftChanged(v.target.value)}/></td>
            <td><TextInput value={this.state.newValue} onChange={v=>this.handleRightChanged(v.target.value)}/></td>
            <td><div onClick={()=>this.handleAddNew()}><FormAdd /></div></td>
          </tr>
        </tbody>
      </table>  
      </Box>
    </Box>)
  }
}

  
