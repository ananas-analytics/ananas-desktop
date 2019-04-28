// @flow

import React, { Component } from 'react'

import type { PlainUser } from '../../../common/model/flowtypes.js'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'
import { Heading } from 'grommet/components/Heading'
import { Keyboard } from 'grommet/components/Keyboard'
import { MaskedInput } from 'grommet/components/MaskedInput'
import { Text } from 'grommet/components/Text'
import { TextInput } from 'grommet/components/TextInput'

import proxy from '../../proxy'

type Props = {
  apiEndpoint: string,
  email: string,
  password: string,
  rememberMe: boolean,

  onSubmit: (PlainUser) => void,
}

type State = {
  email: string,
  password: string,
  rememberMe: boolean,

  errMsg: ?string,
}

export default class AppLogin extends Component<Props, State> {
  static defaultProps = {
    email: '',
    password: '',
    rememberMe: true,
  }

  state = {
    email: this.props.email,
    password: this.props.password,
    rememberMe: this.props.rememberMe,

    errMsg: null,
  }

  handleSubmit() {
    proxy.login(this.props.apiEndpoint, this.state.email, this.state.password)
      .then(user => {
        this.props.onSubmit(user)
      })
      .catch(() => {
        this.setState({errMsg: 'email and password not match!'})
      })
  }

  render() {
    return (<Keyboard target='document' onEnter={()=>this.handleSubmit()}>
      <Box align='center' direction='row' justify='center' fill>
        <Box align='center' direction='column' elevation='small' pad='medium' width='400px'>
          <Heading level={2} color='brand'>Please Sign In</Heading>
          <Box align='start' fill='horizontal' margin={{bottom: 'medium'}}>
            <Text color='label' margin={{bottom:'xsmall'}} >Email</Text>
            <MaskedInput mask={[
              {
                regexp: /^[\w\-_.]+$/,
                placeholder: 'example'
              },
              { fixed: '@' },
              {
                regexp: /^[\w]+$/,
                placeholder: 'yourcompany'
              },
              { fixed: '.' },
              {
                regexp: /^[\w]+$/,
                placeholder: 'com'
              }
              ]}
              value={this.state.email}
              onChange={v=>this.setState({email: v.target.value, errMsg: null})}
            />
          </Box>
          <Box align='start' fill='horizontal' margin={{bottom: 'medium'}}>
            <Text color='label' margin={{bottom: 'xsmall'}}>Password</Text>
            <TextInput type='password'
              value={this.state.password}
              onChange={v=>this.setState({password: v.target.value, errMsg: null})}
            />
          </Box>
          <Box margin={{bottom: 'medium'}}><Text color='status-error'> {this.state.errMsg}</Text></Box>
          <Box direction='column' flex fill >
              <Button label='Login' primary
                onClick={()=>this.handleSubmit()}
              />
          </Box>
        </Box>
    </Box>
  </Keyboard>)
  }
}

