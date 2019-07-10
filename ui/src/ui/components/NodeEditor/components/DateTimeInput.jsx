// @flow

import React, { Component } from 'react' 
import moment from 'moment'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'

import DateInput from './DateInput'
import TimeInput from './TimeInput'

type Props = {
  label?: string,
  date: string,
  time: string,

  showTime: boolean,

  onChange: (moment)=>void
}

export default class DateTimeInput extends Component<Props> {
  static defaultProps = {
    date: moment().format('YYYY-MM-DD'),
    time: '00:00:00',
    showTime: true,

    onChange: ()=>{}
  }

  handleChangeDate = (date: moment) => {
    let newDate = date.format('YYYY-MM-DD')
    this.props.onChange(moment(`${newDate} ${this.props.time}`).valueOf())
  }

  handleChangeTime = (time: string) => {
    this.props.onChange(moment(`${this.props.date} ${time}`).valueOf())
  }

  render() {
    return (
      <Box direction='column' >
        { this.props.label ? <Text size='small' margin={{bottom: 'xsmall'}}>{this.props.label}</Text> : null }
        <Box align='center' direction='row' border='all' round='4px' >
          <DateInput value={this.props.date} onChange={v=>this.handleChangeDate(v)} /> 
          {
            this.props.showTime ? <TimeInput value={this.props.time} onChange={v=>this.handleChangeTime(v)} /> : null
          }
        </Box>
      </Box>
    )
  }
}
