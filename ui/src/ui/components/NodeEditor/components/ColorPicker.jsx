// @flow

import React, { Component } from 'react'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { DropButton } from 'grommet/components/DropButton'

import { SketchPicker } from 'react-color'

type Props = {
  label: string,
  value: string, 
  height: string,
  onChange: (string)=>void
}

type State = {
  color: string
}

export default class ColorPicker extends Component<Props, State> {
  static defaultProps = {
    height: '40px'
  }

  state = {
    color: this.props.value || '#000000'
  }

  render() {
    return (<Box margin={{vertical: 'small'}}>
      { typeof this.props.label === 'string' ? <Text size='small' margin={{bottom: 'xsmall'}}>{this.props.label}</Text> : null }
      <DropButton
        dropAlign={{bottom: 'top'}}
        dropContent={this.renderColorPicker()}
      >
        <Box border='all' pad='xsmall'>
          <Box background={this.state.color} height={this.props.height} />
        </Box>
      </DropButton>
    </Box>)
  }

  renderColorPicker() {
    return (<SketchPicker
      color={ this.state.color }
      disableAlpha={true}
      onChangeComplete={color=>{
        this.setState({ color: color.hex })
        this.props.onChange(color.hex)
      }}
    />)
  }
  
}

