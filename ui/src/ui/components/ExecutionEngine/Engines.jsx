// @flow

import type { PlainEngine, EngineType, EngineTemplate } from '../../../common/model/flowtypes'

import React, { Component } from 'react'

import styled from 'styled-components'

import { Box } from 'grommet/components/Box'
import { Stack } from 'grommet/components/Stack'

import { Add } from 'grommet-icons'

import Search from '../Common/Search'
import EngineItem from './EngineItem'
import EngineEditor from './EngineEditor'

const Engine = styled(Box)`
  &:hover{
    background: ${props => props.theme.global.colors['light-2']}; 
  }
  background: ${props => props.theme.global.colors['light-1']}; 

  height: 160px;
  width: 200px;
`

type Props = {
  filter: string,
  engines: Array<PlainEngine>,
  templates: { [EngineType]: Array<EngineTemplate> },

  onSelectEngine: (PlainEngine) => void,
  onChange: (Array<PlainEngine>) => void
}

type State = {
  filter: string,
  selected: ?PlainEngine,
  editing: boolean,
}

class Engines extends Component<Props, State> {

  static defaultProps = {
    filter: '',
    engines: [],
    templates: {},

    onSelectEngine: ()=>{},
    onChange: ()=>{},
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
      let engines = this.props.engines.filter(v=>{
        return v.name.toUpperCase() !== selected.name.toUpperCase() || v.scope === 'runtime'
      })
      this.setState({ selected: null })
      this.props.onChange(engines)
    }
  }

  handleSubmitEdit(v :PlainEngine) {
    let engines = this.props.engines
    let selected = this.state.selected
    if (selected != null) { // not null, not undefined
      engines = engines.map(engine=>{
      if (engine.name.toUpperCase() === selected.name.toUpperCase()) {
          return { ...v }
        } 
        return engine
      })
    } else {
      engines.unshift(v)  
    }
    this.setState({editing: false})
    this.props.onChange(engines)
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
          <Box direction='column' flex fill margin={{top: 'medium'}} >
            <Box direction='row'
              overflow={{vertical: 'auto'}} wrap={true} >
              <Engine align='center' margin='small' direction='row' justify='center'
                onClick={()=>this.handleClickAddNew()}
              >
                <Add size='large' color='brandLight' /> 
              </Engine>
              { this.props.engines.filter(v => {
                  if (this.state.filter === '') {
                    return true
                  }
                  return v.name.toUpperCase().includes(this.state.filter.toUpperCase())
                }).map(v => {
                  return (<Engine key={v.name} margin='small' 
                    onClick={(e)=>{
                      e.stopPropagation()
                      this.setState({selected: v})
                      this.props.onSelectEngine({ ... v })
                    }}
                  > 
                  <EngineItem name={v.name}
                    type={v.type}
                    description={v.description}
                    selected={
                      this.state.selected != null ? this.state.selected.name === v.name : false
                    }
                    scope={v.scope || 'project'}
                    properties={v.properties}

                    onEdit={()=>this.handleEditSelected()}
                    onDuplicate={()=>{}}
                    onDelete={()=>this.handleDeleteSelected()}
                  /></Engine>)
                }) }
            </Box>
          </Box>
        </Box>
        { 
        this.state.editing ? (
        <Box align='center' direction='row' justify='center' 
          background={{opacity:'strong', color:'light-1'}} fill >
          <Box background='light-1' direction='column' 
            elevation='xsmall' pad='medium' 
            style={{minWidth: '500px', maxHeight: '80%'}}>
            <EngineEditor 
              name={this.state.selected != null ? this.state.selected.name : ''}
              type={this.state.selected != null ? this.state.selected.type : 'Flink'}
              description={this.state.selected != null ? this.state.selected.description : ''}
              scope={this.state.selected != null ? this.state.selected.scope : 'workspace'}
              properties={this.state.selected != null ? this.state.selected.properties : null}
              doc={''}
              engines={this.props.engines}
              templates={this.props.templates}

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

export default Engines
