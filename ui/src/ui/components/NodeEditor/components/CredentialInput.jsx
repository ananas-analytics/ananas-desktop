import React, { Component } from 'react'

import { Box } from 'grommet/components/Box'
import { TextInput } from 'grommet/components/TextInput'

import { Text } from 'grommet/components/Text'

import { View } from 'grommet-icons'

type Props = {
  label: string,
  value: string,

  onChange: () => void, 
}

type State = {
  label: string,
  value: string,
  showCredential: boolean
}

export default class CredentialInput extends Component<Props, State> {
  static defaultProps = {}

  state = {
    label: this.props.label || '',
    value: this.props.value || '',
    showCredential: false,
  }

  render() {
    return (<Box margin={{vertical: 'small'}}>
      { typeof this.state.label === 'string' || this.state.label !== '' ? <Text size='small' margin={{bottom: 'xsmall'}}>{this.state.label}</Text> : null }
      <Box align='center' border={{side: 'all', color: 'border'}} direction='row' >
        <TextInput plain 
          type={this.state.showCredential ? 'text' : 'password'}
          value={this.state.value || ''} 
          onChange={e => {
          this.setState({value: e.target.value})
            this.props.onChange(e.target.value)
          }} />
        <Box margin='small' onClick={()=> this.setState({showCredential: !this.state.showCredential})}>
          <View size='small' color={this.state.showCredential ? 'brand' : 'status-disabled'}/>
        </Box>
      </Box>
    </Box>)
  }
}
