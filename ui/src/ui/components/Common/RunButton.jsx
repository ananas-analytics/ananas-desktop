// @flow
import React, { Component } from 'react'
import styled from 'styled-components'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'
import { DropButton } from 'grommet/components/DropButton'
import { Text } from 'grommet/components/Text'

import type { PlainEngine } from '../../../common/model/flowtypes'

type Props = {
  label: string,
  engines: Array<PlainEngine>,

  disabled: boolean,

  onClick: PlainEngine=>void,
}

type State = {
  open?: boolean
}

const DropDownButton = styled(Box)`
  &:hover{
    background: ${props => props.theme.global.colors['light-2']}; 
  }
  background: ${props => props.theme.global.colors['light-1']}; 
  cursor: pointer;
`

export default class RunButton extends Component<Props, State> {
  static defaultProps = {
    label: 'RUN',  
    engines: [],
    disabled: false,
  }

  state = {
  }

  renderDropContent() {
    return (<Box direction='column' >{
      this.props.engines.map(engine => {
        return (
          <Button key={engine.name} hoverIndicator="light-1" onClick={() => {
            this.props.onClick(engine) 
            this.setState({ open: false })
            setTimeout(() => this.setState({ open: undefined }), 1)
          }} >
            <Box pad="small" direction="row" align="center" gap="small">
              <Text>{ engine.name }</Text>
            </Box>
          </Button>
        )
      })
    }</Box>)
  }
  
  render() {
    if (this.props.engines.length === 1) {
      return (<Button label={this.props.label} primary disabled={this.props.disabled} onClick={()=>{
        this.props.onClick(this.props.engines[0])
      }}/>)
    }
    return (<DropButton
      alignSelf='center'
      primary
      label={this.props.label}
      open={this.state.open}
      onClose={() => this.setState({ open: undefined })}
      dropContent={this.renderDropContent()}
      dropAlign={{top: 'bottom', left: 'left'}}
    />)
  }
}
