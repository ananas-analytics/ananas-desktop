// @flow

import * as React from 'react'

import { Box } from 'grommet/components/Box'
import { MaskedInput } from 'grommet/components/MaskedInput'


type Props = {
  value: string,

  onChange: (string)=>void,
}

type State = {
  value: string
}

export default class TimeInput extends React.Component<Props, State> {
  static defaultProps = {
    value: '',

    onChange: ()=>{},
  }

  state = { 
    value: this.props.value 
  }

  hours = []
  minutes = []
  seconds = []

  constructor(props: Props) {
    super(props)

    for (let i = 0; i < 24; i++) {
      this.hours.push(i.toLocaleString('en-US', {minimumIntegerDigits: 2, useGrouping:false}))
    }

    for (let i = 0; i < 60; i++) {
      this.minutes.push(i.toLocaleString('en-US', {minimumIntegerDigits: 2, useGrouping:false}))
      this.seconds.push(i.toLocaleString('en-US', {minimumIntegerDigits: 2, useGrouping:false}))
    }

  }

  // eslint-disable-next-line no-undef
  onChange = (event: SyntheticInputEvent<>) => {
    this.setState({ value: event.target.value })
    this.props.onChange(event.target.value)
  };

  render() {
    const { value } = this.state
    return (
      <Box align='center' justify='start' width='120px'>
        <Box width='medium'>
          <MaskedInput
            plain
            mask={[
              {
                length: 2,
                options: this.hours, 
                regexp: /^1[1-2]$|^[0-9]$/,
                placeholder: 'HH'
              },
              { fixed: ':' },
              {
                length: 2,
                options: this.minutes,
                regexp: /^[0-5][0-9]$|^[0-9]$/,
                placeholder: 'mm'
              },
              { fixed: ':' },
              {
                length: 2,
                options: this.seconds,
                regexp: /^[0-5][0-9]$|^[0-9]$/,
                placeholder: 'ss'
              }
            ]}
            value={value}
            size='small'
            style={{padding: '10px'}}
            onChange={this.onChange}
          />
        </Box>
      </Box>
    )
  }
}
