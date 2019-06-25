// @flow

import React, { Component } from 'react'
import uuidv1 from 'uuid/v1'
import moment from 'moment'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'
import { Heading } from 'grommet/components/Heading'
import { Text } from 'grommet/components/Text'

import SelectInput from '../NodeEditor/components/SelectInput'
import TextArea from '../NodeEditor/components/TextArea'
import TextInput from '../NodeEditor/components/TextInput'
import DateTimeInput from '../NodeEditor/components/DateTimeInput'


import type { PlainEngine, PlainTrigger, TriggerType  } from '../../../common/model/flowtypes'

type Props = {
  projectId: string,
  trigger: ?PlainTrigger,
  engines: Array<PlainEngine>,
  triggers: Array<PlainTrigger>,

  onSubmit: (State) => void,
  onCancel: () => void,
}

type State = {
  id: string,
  projectId: string,
  name: string,
  type: TriggerType,
  description: string,
  
  interval: number,
  hour: number,
  minute: number,
  dayOfWeek: number,
  dayOfMonth: number,
  startTimestamp: number,

  engine: ?PlainEngine,

  disabled: boolean,
  errMsg: ?string,
}


class TriggerEditor extends Component<Props, State> {
  static TriggerOptions = [
    { label: 'ONCE', value: 'once' },
    { label: 'REPEAT', value: 'repeat' },
    { label: 'HOURLY', value: 'hourly' },
    { label: 'DAILY', value: 'daily' },
    { label: 'WEEKLY', value: 'weekly' },
    { label: 'MONTHLY', value: 'monthly' },
  ]

  static Hours = [...Array(24).keys()].map<any>(h => {
    return {
      label: h.toString(), value: h,
    }
  })

  static Minutes = [...Array(60).keys()].map<any>(h => {
    return {
      label: h.toString(), value: h,
    }
  })

  static DayOfWeeks = [...Array(7).keys()].map<any>(h => {
    let o = {
      label: '',
      value: h,
    }

    switch(h) {
      case 0: 
        o.label = 'Monday'
        break
      case 1: 
        o.label = 'Tuesday'
        break
      case 2: 
        o.label = 'Wednesday'
        break
      case 3: 
        o.label = 'Thursday'
        break
      case 4: 
        o.label = 'Friday'
        break
      case 5: 
        o.label = 'Saturday'
        break
      case 6: 
        o.label = 'Sunday'
        break
    }
    return o
  })

  static DayOfMonths = [...Array(31).keys()].map<any>(h => {
    return {
      label: (h+1).toString(), value: h,
    }
  })


  static defaultProps = {
    trigger: {
      id: uuidv1(),
      projectId: '',
      name: '',
      description: '',
      type: 'once',
      startTimestamp: new Date().getTime() + 1000 * 60, // 1 minute later

      interval: 0,
      hour: 0,
      minute: 0,
      dayOfWeek: 0,
      dayOfMonth: 0,


      enabled: false,
    },

    triggers: [],

    engine: null,

    onSubmit: ()=>{},
    onCancel: ()=>{},
  }

  state = {
    id: this.props.trigger ? this.props.trigger.id : uuidv1(),        
    projectId: this.props.trigger ? this.props.trigger.projectId : this.props.projectId,
    name: this.props.trigger ? this.props.trigger.name : '',
    type: this.props.trigger ? this.props.trigger.type : 'once',
    description: this.props.trigger ? this.props.trigger.description : '',

    interval: (this.props.trigger != null && this.props.trigger.interval != null) ? this.props.trigger.interval : 0,
    hour: (this.props.trigger != null && this.props.trigger.hour != null) ? this.props.trigger.hour : 0,
    minute: (this.props.trigger != null && this.props.trigger.minute != null) ? this.props.trigger.minute : 0,
    dayOfWeek: (this.props.trigger != null && this.props.trigger.dayOfWeek != null) ? this.props.trigger.dayOfWeek : 0,
    dayOfMonth: (this.props.trigger != null && this.props.trigger.dayOfMonth != null) ? this.props.trigger.dayOfMonth : 0,
    startTimestamp: this.props.trigger != null ? this.props.trigger.startTimestamp : new Date().getTime() + 1000 * 60,

    engine: null,

    disabled: false,
    errMsg: null,
  }

  handleChangeName(name: string) {
    let errMsg = null
    let disabled = false
    if (this.props.triggers.find(v=>v.name.toUpperCase()===name.toUpperCase())) {
      errMsg = `${name} already exists!` 
      disabled = true
    }
    this.setState({name, errMsg, disabled})
  }

  render() {
    return (
      <Box flex fill>
        <Box flex fill>
          <Heading level={4} color='brand'>Edit Scheduling</Heading>
          <Box flex fill overflow={{vertical: 'auto'}}>
            <Box flex={false} fill>
              <TextInput label='Scheduler Name' value={this.state.name} 
                onChange={v=>{this.handleChangeName(v)}}
              />
              {<Heading level={6} color='brand' margin={{vertical: 'xsmall'}}>The Task</Heading>}

              {<Heading level={6} color='brand' margin={{vertical: 'xsmall'}}>The Engine</Heading>}
              {/*
              <SelectInput label='Engines' value={this.state.engine} 
                options={this.props.engines.map(engine => {
                  return {
                    label: engine.name,
                    value: engine
                  }
                })}
                onChange={v=>this.setState({engine: v})}
              />
              */}

              {<Heading level={6} color='brand' margin={{vertical: 'xsmall'}}>Trigger Settings</Heading>}
              
              { this.state.errMsg ? <Text color='status-error' size='small'>{this.state.errMsg}</Text> : null }
              <SelectInput label='Trigger Type' value={this.state.type} 
                options={TriggerEditor.TriggerOptions}
                onChange={v=>{
                  this.setState({type: v})  
                }}
              />
              {/*
                <TextArea label='Trigger Description' value={this.state.description} 
                onChange={v=>this.setState({description: v})} />
              */}
              {
                this.renderParameters()
              }
            </Box>
          </Box>
        </Box>
        <Box direction='row' height='40px' justify='end' flex={false} fill='horizontal' gap='medium' margin={{top: 'medium'}}>
          <Button label='Save' primary 
            disabled={this.state.disabled}
            onClick={()=>{
              let trigger = { ... this.state }
              delete trigger['disabled']
              delete trigger['errMsg']
              this.props.onSubmit(trigger)
            }}/>
          <Button label='Cancel' onClick={()=>this.props.onCancel()}/>
        </Box>
      </Box>
    )
  }

  renderParameters() {
    let parameters = []

    parameters.push((<DateTimeInput key='start timestamp'  label='Start Date Time'
        date={moment(this.state.startTimestamp).format('YYYY-MM-DD')} 
        time={moment(this.state.startTimestamp).format('HH:mm:ss')}
        onChange={v => this.setState({ startTimestamp: v.valueOf() })}
      />))


    if (this.state.type === 'repeat') {
      parameters.push((<TextInput key='interval' type='number' label='Interval In Seconds' value={this.state.interval} 
        onChange={v => this.setState({ interval: v })}
      />))
    }

    if (this.state.type === 'hourly') {
      parameters.push((<SelectInput key='minute' label='Minute' value={this.state.minute} 
        options={TriggerEditor.Minutes}
        onChange={v => this.setState({ minute: v })}
      />))
    }

    if (this.state.type === 'daily') {
      parameters.push((<SelectInput key='hour' label='Hour' value={this.state.hour} 
        options={TriggerEditor.Hours}
        onChange={v => this.setState({ hour: v })}
      />))
      parameters.push((<SelectInput key='minute' label='Minute' value={this.state.minute} 
        options={TriggerEditor.Minutes}
        onChange={v => this.setState({ minute: v })}
      />))
    }

    if (this.state.type === 'weekly') {
      parameters.push((<SelectInput key='weekday' label='Day Of Week' value={this.state.dayOfWeek} 
        options={TriggerEditor.DayOfWeeks}
        onChange={v => this.setState({ dayOfWeek : v })}
      />))
      parameters.push((<SelectInput key='hour' label='Hour' value={this.state.hour} 
        options={TriggerEditor.Hours}
        onChange={v => this.setState({ hour: v })}
      />))
      parameters.push((<SelectInput key='minute' label='Minute' value={this.state.minute} 
        options={TriggerEditor.Minutes}
        onChange={v => this.setState({ minute: v })}
      />))
    }

    if (this.state.type === 'monthly') {
      parameters.push((<SelectInput key='monthday' label='Day of Month' value={this.state.dayOfMonth} 
        options={TriggerEditor.DayOfMonths}
        onChange={v => this.setState({ dayOfMonth : v })}
      />))
      parameters.push((<SelectInput key='hour' label='Hour' value={this.state.hour} 
        options={TriggerEditor.Hours}
        onChange={v => this.setState({ hour: v })}
      />))
      parameters.push((<SelectInput key='minute' label='Minute' value={this.state.minute} 
        options={TriggerEditor.Minutes}
        onChange={v => this.setState({ minute: v })}
      />))
    }

    return parameters
  }
}


export default TriggerEditor
