// @flow

import React, { Component } from 'react'
import moment from 'moment'

import { Box } from 'grommet/components/Box'
import { DropButton } from 'grommet/components/DropButton'
import { Calendar } from 'grommet/components/Calendar'
import { Text } from 'grommet/components/Text'

import { FormDown } from 'grommet-icons'

type Props = {
  value?: string,
  onChange: (moment) => void,
}

type State = {
  open: boolean,
  value?: string,
}

export default class DateInput extends Component<Props, State> {
  static defaultProps = {
    open: false,
    value: undefined,

    onChange: ()=>{},
  }

  state = {
    open: false,
    value: this.props.value,
  }

  onClose = () => {
    this.setState({ open: false })
    setTimeout(() => this.setState({ open: undefined }), 1)
  };

  onSelect = (date: Date) => {
    let normalizedDate = moment(date)
    normalizedDate.hour(0).minute(0).second(0).millisecond(0)

    this.setState({ value: normalizedDate.format('YYYY-MM-DD'), open: false })
    this.props.onChange(normalizedDate)
  }

  render() {
    return (
      <Box align='center' direction='row'>
        <DropButton
          open={this.state.open}
          onClose={() => this.setState({ open: false })}
          onOpen={() => this.setState({ open: true })}
          dropContent={<Calendar date={this.state.value} size='small' onSelect={this.onSelect} />}
        >
          <Box direction='row' gap='medium' align='center' pad='10px'>
            <Text size='small'>
              {this.state.value ? new Date(this.state.value).toLocaleDateString() : 'Select date'}
            </Text>
            <FormDown color='brand' size='18px'/>
          </Box>
        </DropButton>
        
      </Box>
    )
  }
}


