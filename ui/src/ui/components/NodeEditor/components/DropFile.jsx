// @flow
import React, { Component } from 'react'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'


type Props = {
  label: ?string,
  value: string,

  onChange: (string)=>void,
}

type State = {
}

export default class DropFile extends Component<Props, State> {
  render() {

    return (<Box margin={{vertical: 'small'}} border='all' onDragOver={
        (event) => {
          // Ugly: fix react-dnd conflict with react-dropzone
          // see: https://github.com/react-dnd/react-dnd/issues/457
          event.preventDefault()
          event.stopPropagation()
        }
      }
      onDrop={ (event) => event.preventDefault() }
      >
      { typeof this.props.label === 'string' ? <Text size='small' margin={{bottom: 'xsmall'}}>{this.props.label}</Text> : null }
      <Box pad='small' onDrop={
        (ev) => {
          let path = ev.dataTransfer.files[0].path
          this.props.onChange(path)
          ev.preventDefault()
        }
      }>
        <Text size='1rem' weight='bold' wordBreak='break-all'>
          { this.props.value ? this.props.value : 'Drop your file here ...' }
        </Text>
      </Box>
    </Box>)
  }
}
