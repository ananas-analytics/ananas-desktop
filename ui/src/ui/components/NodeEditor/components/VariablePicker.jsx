// @flow

import React, { Component } from 'react'
import moment from 'moment'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { Button } from 'grommet/components/Button'

import TextInput from './TextInput'
import DateTimeInput from './DateTimeInput'

import { FormNext, FormDown } from 'grommet-icons'

import { ExploreOption, TestOption } from '../../../model/NodeEditor'

import type { Node } from 'react'
import type { PlainVariable, NodeEditorContext } from '../../../../common/model/flowtypes.js'

import type { EventEmitter3 } from 'eventemitter3'


type Props = {
  context: NodeEditorContext, 
  ee: EventEmitter3, 
  label: string,
  value: {[string]: any},

  exploreButton: boolean,
  testButton: boolean,
  runButton: boolean,

  onChange: ({})=>void,
  onError: (title:string, level:string, error:Error)=>void
}

type State = {
  values: {[string]:any},
  open: boolean,
  running: boolean,
}

export default class VariablePicker extends Component<Props, State> {
  static defaultProps = {
    label: 'Data Criteria',
    value: {},
    exploreButton: false,
    testButton: false,
    runButton: false,
    onChange: ()=>{}
  }

  state = {
    values: this.props.value,
    open: false,
    running: false,
  }

  componentDidMount() {
    this.toggleOpen(true)
    this.props.ee.on('START_GET_DATAFRAME', this.handleStartExecution, this)
    this.props.ee.on('END_GET_DATAFRAME', this.handleEndExecution, this)
  }

  componentWillUnmount() {
    this.props.ee.removeListener('START_GET_DATAFRAME', this.handleStartExecution)
    this.props.ee.removeListener('END_GET_DATAFRAME', this.handleEndExecution)
  }

  handleStartExecution() {
    this.setState({running: true})
  }

  handleEndExecution() {
    this.setState({running: false})
  }

  handleChange(name: string, value: any) {
    let values = this.state.values
    values[name] = value
    this.setState({values})

    this.props.onChange(values)
  }

  handleExplore() {
    let { variableService } = this.props.context.services
    this.props.ee.emit('START_GET_DATAFRAME')
    variableService.saveVariableDict(this.props.context.project.id, this.state.values)
      .then(()=>{
        this.props.ee.emit('GET_DATAFRAME', new ExploreOption() )
      })
      .catch(err=>{
        this.props.onError('Error', 'danger', err)
        this.props.ee.emit('END_GET_DATAFRAME')
      })
  }

  handleTest() {
    let { variableService } = this.props.context.services
    this.props.ee.emit('START_GET_DATAFRAME')
    variableService.saveVariableDict(this.props.context.project.id, this.state.values)
      .then(()=>{
        this.props.ee.emit('GET_DATAFRAME', new TestOption())
      })
      .catch(err=>{
        this.props.onError('Error', 'danger', err)
        this.props.ee.emit('END_GET_DATAFRAME')
      })
  }

  handleRun() {
    let { executionService, jobService, variableService, notificationService } = this.props.context.services
    variableService.saveVariableDict(this.props.context.project.id, this.state.values)
      .then(()=>{
        return variableService.loadVariableDict(this.props.context.project.id, this.props.context.variables)
      })
      .then(dict => {
        return executionService.runStep(
          this.props.context.user,
          this.props.context.project.id,
          this.props.context.project.dag.connections,
          this.props.context.project.steps,
          dict,
          this.props.context.step.id)
      })
      .then(res => {
        if (res.code !== 200) {
          throw new Error(res.message)
        }
        jobService.newJob(res.data.jobid, this.props.context.step.id, this.props.context.user.name, this.props.context.user.token)
        notificationService.notify('Job Created', {
          body: `Job ${res.data.jobid} is created ...`
        })
      })
      .catch(err=>{
        this.props.onError('Failed to run current step', 'danger', err)
      })

  }

  toggleOpen(open: boolean) {
    let { variableService } = this.props.context.services
    if (open) {
      variableService.loadVariableDict(this.props.context.project.id, this.props.context.variables)
        .then(values => {
          this.setState({values, open})
        })
        .catch(err=>{
          // use empty dict
          this.props.onError('Failed to load variable dictionary', 'warning', err)
          this.setState({values: {}, open})
        })
    } else {
      this.setState({open})
    }
    
  }

  renderVariables() {
    return this.props.context.variables.map<Node>(v => {
      if (v.scope === 'runtime') {
        return null
      }
      switch(v.type) {
        case 'number':
          return (
            <Box key={v.name} pad='small'>
              <TextInput label={v.name.toUpperCase()} value={this.state.values[v.name]} 
                onChange={value=>this.handleChange(v.name, value)}
              />
            </Box>
          )
        case 'date': 
          return (
            <Box key={v.name} align='center' justify='center' pad='small'>
              {this.renderDateVariable(v)}
            </Box>
          )
        case 'string':
        default:
        return (
            <Box key={v.name} pad='small'>
              <TextInput label={v.name.toUpperCase()} value={this.state.values[v.name]} 
                onChange={value=>this.handleChange(v.name, value)}
              />
            </Box>
          )
      }
    })  
  }

  renderDateVariable(variable: PlainVariable) {
    let v = this.state.values[variable.name] || moment()    
    if (typeof v === 'string' || typeof v === 'number') {
      v = moment(v)
    }
    return (
      <DateTimeInput key={variable.name} label={variable.name.toUpperCase()} 
        date={v.format('YYYY-MM-DD')} 
        time={v.format('HH:mm:ss')} showTime={true}
        onChange={(value)=>{this.handleChange(variable.name, value.valueOf())}}
      />
    )
  }

  render() {
    let nonRuntimeVariables = this.props.context.variables.filter(v=>v.scope!=='runtime')
    return (
      <Box direction='column'>
        <Box direction='row' justify='start' gap='small' 
          margin={{ bottom: 'medium' }}
          onClick={()=>this.toggleOpen(!this.state.open)} >
          <Text size='medium'>{this.props.label}</Text>
          {
            this.state.open ? <FormDown size='medium'/> : <FormNext size='medium' /> 
          }
        </Box>
        {
        this.state.open ? (<Box border={ nonRuntimeVariables.length === 0 ? {size: '0px'}: 'all' } round='4px'>
            <Box direction='row' wrap>
            { this.renderVariables() }
            </Box>
          </Box>) : null
        }
        <Box align='center' direction='row' pad='small' justify='start' flex fill gap='small'>
          { this.props.testButton ? <Button label='Test' disabled={this.state.running} onClick={()=>this.handleTest()}/> : null }
          { this.props.testButton && this.props.runButton ? <Box><FormNext size='small' /></Box> : null }
          { this.props.runButton ? <Button label='Run' primary disabled={this.state.running} onClick={()=>this.handleRun()}/> : null }
          { this.props.testButton && this.props.runButton && this.props.exploreButton ? <Box><FormNext size='small' /></Box> : null }
          { this.props.exploreButton ? <Button label='Explore' disabled={this.state.running} onClick={()=>this.handleExplore()}/> : null }
        </Box>
      </Box>
    )
  }
}

