// @flow

import type { PlainEngine, PlainTrigger } from '../../../common/model/flowtypes'

import React, { Component } from 'react'
import styled from 'styled-components'
import moment from 'moment'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'
import { Stack } from 'grommet/components/Stack'
import { Text } from 'grommet/components/Text'

import { Add } from 'grommet-icons'

import Search from '../Common/Search'
import TriggerItem from './TriggerItem'
import TriggerEditor from './TriggerEditor'

const Trigger = styled(Box)`
  &:hover{
    background: ${props => props.theme.global.colors['light-2']}; 
  }
  background: ${props => props.theme.global.colors['light-1']}; 

  height: 160px;
  width: 200px;
`

type Props = {
  projectId: string,
  filter: string,
  engines: Array<PlainEngine>,
  triggers: Array<PlainTrigger>,

  onSelectTrigger: (PlainTrigger)=>void,
  onChange: (Array<PlainTrigger>)=>void,
}

type State = {
  filter: string,
  selected: ?PlainTrigger,
  editing: boolean,
}

export default class Triggers extends Component<Props, State> {
  static defaultProps = {
    filter: '',
    engines: [],
    triggers: [],

    onSelectTrigger: () => {},
    onChange: () => {},
  }

  state = {
    filter: '',
    selected: null,
    editing: false,
  }

  handleClickAddNew() {
    this.setState({ selected: null, editing: true })
  }

  handleEditSelected() {
    this.setState({editing: true})
  }

  handleDeleteSelected() {
    let selected = this.state.selected
    if (selected != null) { // flow, not null, not undefined
      let triggers = this.props.triggers.filter(v=>{
        return v.name.toUpperCase() !== selected.name.toUpperCase() 
      })
      this.setState({ selected: null })
      this.props.onChange(triggers)
    }
  }

  handleSubmitEdit(v: any) {
    let triggers = this.props.triggers
    let selected = this.state.selected
    if (selected != null) { // not null, not undefined
      triggers = triggers.map(trigger=>{
      if (trigger.name.toUpperCase() === selected.name.toUpperCase()) {
          return { ...v }
        } 
        return trigger
      })
    } else {
      triggers.unshift(v)  
    }
    this.setState({editing: false})
    this.props.onChange(triggers)

  }

  handleCancelEdit() {
    this.setState({ editing: false })
  }


  render() {
    return (
      <Stack fill>
        <Box direction='column' pad='small' fill onClick={()=>{this.setState({selected: null})}}>
          <Box flex={false} height='50px'>
            <Search text={this.state.filter} onChange={(filter)=> this.setState({filter})}/>
          </Box>

          <Box align='center' direction='row' flex={false} gap='small' pad='small' >
            <Button primary label='Deploy'/>
            <Button secondary label='Start/Stop'/>
            <Text size='medium'>Current active deployment at {moment().format('YYYY/MM/DD')}</Text>
          </Box>

          <Box direction='column' flex fill margin={{top: 'medium'}} >
            <Box direction='row'
              overflow={{vertical: 'auto'}} wrap={true} >
              <Trigger align='center' margin='small' direction='row' justify='center'
                onClick={()=>this.handleClickAddNew()}
              >
                <Add size='large' color='brandLight' /> 
              </Trigger>
              { this.props.triggers.filter(v => {
                  if (this.state.filter === '') {
                    return true }
                  return v.name.toUpperCase().includes(this.state.filter.toUpperCase())
                }).map(v => {
                  return (<Trigger key={v.name} margin='small' 
                    onClick={(e)=>{
                      e.stopPropagation()
                      this.setState({selected: v})
                      this.props.onSelectTrigger({ ... v })
                    }}
                  > 
                  <TriggerItem name={v.name}
                    type={v.type}
                    description={v.description}
                    startTimestamp={v.startTimestamp}
                    enabled={v.enabled}
                    selected={
                      this.state.selected != null ? this.state.selected.name === v.name : false
                    }

                    onEdit={()=>this.handleEditSelected()}
                    onDuplicate={()=>{}}
                    onDelete={()=>this.handleDeleteSelected()}
                  /></Trigger>)
                }) }
            </Box>
          </Box>
        </Box>
        { 
        this.state.editing ? (
        <Box align='center' direction='row' justify='center' 
          background={{opacity:'strong', color:'light-1'}} fill >
          <Box background='light-1' direction='column' fill 
            elevation='xsmall' pad='medium' > 
            <TriggerEditor 
              projectId={this.props.projectId}
              trigger={this.state.selected}
              triggers={this.props.triggers}
              engines={[]}

              onSubmit={(v)=>this.handleSubmitEdit(v)}
              onCancel={()=>this.handleCancelEdit()}
            />
          </Box>
        </Box>) : null
        }
      </Stack>
    )
  }
  
}
