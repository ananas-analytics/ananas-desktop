// @flow
import React, { Component } from 'react'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'
import { Heading } from 'grommet/components/Heading'
import { Text } from 'grommet/components/Text'

import SelectInput from '../NodeEditor/components/SelectInput'
import TextArea from '../NodeEditor/components/TextArea'
import TextInput from '../NodeEditor/components/TextInput'


import type { PlainEngine, EngineType, EngineScope, EngineTemplate } from '../../../common/model/flowtypes'

type Props = {
  name: string,
  type: EngineType,
  description: string,
  scope: EngineScope,
  doc: string,

  properties: ?{ [string]: string },

  engines: Array<PlainEngine>,
  templates: {[EngineType]: Array<EngineTemplate>},

  onSubmit: (State) => void,
  onCancel: () => void,
}

type State = {
  name: string,
  type: EngineType,
  description: string,
  scope: EngineScope,
  doc:string,

  properties: { [string]: string },

  disabled: boolean,
  errMsg: ?string,
}


class EngineEditor extends Component<Props, State> {

   static defaultProps = {
    name: '',
    type: 'Flink',
    description: '',
    scope: 'workspace',
    doc: '',

    properties: {},

    engines: [],
    templates: {},

    onSubmit: ()=>{},
    onCancel: ()=>{},
  }

  state = {
    name: this.props.name,
    type: this.props.type,
    description: this.props.description,
    scope: this.props.scope,
    doc: this.props.doc,

    properties: this.props.properties != null ? this.props.properties : this.getDefaultPropertiesByType(this.props.type),

    disabled: false,
    errMsg: null,
  }

  handleChangeName(name: string) {
    let errMsg = null
    let disabled = false
    if (this.props.engines.find(v=>v.name.toUpperCase()===name.toUpperCase())) {
      errMsg = `${name} already exists!` 
      disabled = true
    }
    this.setState({name, errMsg, disabled})
  }

  getDefaultPropertiesByType(type: EngineType) {
    let template = this.props.templates[type]  
    if (!template) {
      return {}
    }

    let properties = {}
    template.map(row => {
      properties[row.name] = row.default 
    })
    return properties
  }

  render() {
    let typeOptions = Object.keys(this.props.templates).map<any>(template => {
      return { label: template.toUpperCase(), value: template }
    })
    return (
      <Box flex fill>
        <Box flex fill>
          <Heading level={4} color='brand'>Edit Execution Engine</Heading>
          <TextInput label='Engine Name' value={this.state.name} 
            onChange={v=>{this.handleChangeName(v)}}
          />
          { this.state.errMsg ? <Text color='status-error' size='small'>{this.state.errMsg}</Text> : null }
          <SelectInput label='Engine Type' value={this.state.type} 
            options={typeOptions}
            onChange={v=>{
              this.setState({type: v, properties: this.getDefaultPropertiesByType(v)})  
            }}
          />
          <TextArea label='Engine Description' value={this.state.description} 
            onChange={v=>this.setState({description: v})} />
        </Box>
        {
          this.props.templates[this.state.type].map(row => {
          return (<Box key={row.name}>
            <TextInput label={row.label} value={this.state.properties[row.name] ? this.state.properties[row.name] : row.default} 
              onChange={v=>{
                let properties = { ... this.state.properties }
                properties[row.name] = v
                this.setState({ properties })
              }}
            />
            </Box>)
          })
        }
        <Box direction='row' height='40px' justify='end' flex={false} fill='horizontal' gap='medium' margin={{top: 'medium'}}>
          <Button label='Save' primary 
            disabled={this.state.disabled}
            onClick={()=>{
              let engine = { ... this.state }
              delete engine['doc']
              delete engine['disabled']
              delete engine['errMsg']
              this.props.onSubmit(engine)
            }}/>
          <Button label='Cancel' onClick={()=>this.props.onCancel()}/>
        </Box>
      </Box>
    )
  }
}

export default EngineEditor
