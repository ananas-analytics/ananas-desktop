// @flow

import type { PlainVariable } from '../../../common/model/flowtypes.js'

import React, { Component } from 'react'

import styled from 'styled-components'

import { Box } from 'grommet/components/Box'
import { Stack } from 'grommet/components/Stack'

import { Add } from 'grommet-icons'

import Search from '../Common/Search'
import VariableItem from './VariableItem'
import VariableEditor from './VariableEditor'

const Variable = styled(Box)`
  &:hover{
    background: ${props => props.theme.global.colors['light-2']}; 
  }
  background: ${props => props.theme.global.colors['light-1']}; 

  height: 160px;
  width: 200px;
`

type Props = {
  filter: string,
  variables: Array<PlainVariable>,

  onSelectVariable: (PlainVariable) => void,
  onChange: (Array<PlainVariable>) => void
}

type State = {
  filter: string,
  selected: ?PlainVariable,
  editing: boolean,
}

class Variables extends Component<Props, State> {

  static defaultProps = {
    filter: '',
    variables: [],

    onSelectVariable: ()=>{},
    onChange: ()=>{},
  }

  state = {
    filter: '',
    variables: this.props.variables,
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
      let variables = this.props.variables.filter(v=>{
        return v.name !== selected.name && v.scope === 'project'
      })
      this.setState({ selected: null })
      this.props.onChange(variables)
    }
  }

  handleSubmitEdit(v /*:PlainVariable*/) {
    let variables = this.props.variables
    let selected = this.state.selected
    if (selected != null) { // not null, not undefined
      variables = variables.map(variable=>{
        if (variable.name === selected.name) {
          return { ...v }
        } 
        return variable
      })
    } else {
      variables.unshift(v)  
    }
    this.setState({editing: false})
    this.props.onChange(variables.filter(v=>v.scope === 'project'))
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
              <Variable align='center' margin='small' direction='row' justify='center'
                onClick={()=>this.handleClickAddNew()}
              >
                <Add size='large' color='brandLight' /> 
              </Variable>
              { this.props.variables.filter(v => {
                  if (this.state.filter === '') {
                    return true
                  }
                  return v.name.toUpperCase().includes(this.state.filter.toUpperCase())
                }).map(v => {
                  return (<Variable key={v.name} margin='small' 
                    onClick={(e)=>{
                      e.stopPropagation()
                      this.setState({selected: v})
                      this.props.onSelectVariable({ ... v })
                    }}
                  > 
                  <VariableItem name={v.name}
                    type={v.type}
                    description={v.description}
                    selected={
                      this.state.selected != null ? this.state.selected.name === v.name : false
                    }
                    scope={v.scope || 'project'}

                    onEdit={()=>this.handleEditSelected()}
                    onDelete={()=>this.handleDeleteSelected()}
                  /></Variable>)
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
            style={{minWidth: '500px'}}>
            <VariableEditor name={this.state.selected != null ? this.state.selected.name : ''}
              type={this.state.selected != null ? this.state.selected.type : 'string'}
              description={this.state.selected != null ? this.state.selected.description : ''}
              doc={''}
              variables={this.props.variables}

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

export default Variables
