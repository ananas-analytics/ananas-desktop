import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'
import { Heading } from 'grommet/components/Heading'
import { Text } from 'grommet/components/Text'

import SelectInput from '../NodeEditor/components/SelectInput'
import TextArea from '../NodeEditor/components/TextArea'
import TextInput from '../NodeEditor/components/TextInput'


class VariableEditor extends Component {
  static propTypes = {
    name: PropTypes.string,
    type: PropTypes.string,
    description: PropTypes.string,
    scope: PropTypes.string,
    doc: PropTypes.string,

    variables: PropTypes.array,

    onSubmit: PropTypes.func,
    onCancel: PropTypes.func,
  } 

  static defaultProps = {
    name: '',
    type: 'string',
    description: '',
    scope: 'project',
    doc: '',

    variables: [],

    onSubmit: ()=>{},
    onCancel: ()=>{},
  }

  constructor(props) {
    super(props)
    this.state = {
      name: this.props.name,
      type: this.props.type,
      description: this.props.description,
      scope: this.props.scope,
      doc: this.props.doc,

      disabled: false,
      errMsg: null,
    }
  }

  handleChangeName(name) {
    let errMsg = null
    let disabled = false
    if (this.props.variables.find(v=>v.name===name)) {
      errMsg = `${name} already exists!` 
      disabled = true
    }
    this.setState({name, errMsg, disabled})
  }

  render() {
    return (
      <Box flex fill>
        <Box flex fill>
          <Heading level={4} color='brand'>Edit Variable</Heading>
          <TextInput label='Variable Name' value={this.state.name} 
            onChange={v=>{this.handleChangeName(v)}}
          />
          { this.state.errMsg ? <Text color='status-error' size='small'>{this.state.errMsg}</Text> : null }
          <SelectInput label='Variable Type' value={this.state.type} 
            options={[
              {label: 'STRING', value: 'string'}, 
              {label: 'NUMBER', value: 'number'},
              {label: 'DATE', value: 'date'},
            ]}
            onChange={v=>{
              this.setState({type: v})  
            }}
          />
          <TextArea label='Variable Description' value={this.state.description} 
            onChange={v=>this.setState({description: v})} />
        </Box>
        <Box direction='row' height='40px' justify='end' flex={false} fill='horizontal' gap='medium' margin={{top: 'medium'}}>
          <Button label='Save' primary 
            disabled={this.state.disabled}
            onClick={()=>this.props.onSubmit(this.state)}/>
          <Button label='Cancel' onClick={()=>this.props.onCancel()}/>
        </Box>
      </Box>
    )
  }
}

export default VariableEditor
