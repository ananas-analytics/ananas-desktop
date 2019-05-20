import React, { Component } from 'react'
import { connect } from 'react-redux'

import { Accordion } from 'grommet/components/Accordion'
import { AccordionPanel } from 'grommet/components/AccordionPanel'
import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { Table } from 'grommet/components/Table'
import { TableBody } from 'grommet/components/TableBody'
import { TableHeader } from 'grommet/components/TableHeader'
import { TableRow } from 'grommet/components/TableRow'
import { TableCell } from 'grommet/components/TableCell'

class NodeEditorSideBar extends Component {
  constructor(props) {
    super(props)

    this.state = {
      type: 'Schema'
    }
  }

  renderTabs() {
    return ['Schema', 'Variable']
    .map(type => {
      return (
        <Text key={type} size='xsmall'
          color={this.state.type === type ? 'brand' : 'dark-4'} 
          onClick={ () => this.setState({type}) }
          style={{
            cursor: 'pointer',
            fontWeight: this.state.type === type ? '600' : '300'
          }}
        >
          {type}
        </Text>)
    })  
  }

  renderSchemaContent() {
    return (<Accordion animate={false} multiple={true}>
      {this.props.upstreams.map(step => {
        return (<AccordionPanel key={step.id} 
          label={<Text color='brand' size='small' margin='small'>{`Input Schema - ${step.name}`}</Text>}
        >
          { this.renderDataframe(step.dataframe) }
        </AccordionPanel>)
      })}
      <AccordionPanel 
        label={<Text color='brand' size='small' margin='small'>Output Schema</Text>}
      >
        { this.renderDataframe(this.props.step.dataframe) }
      </AccordionPanel> 
    </Accordion>) 
  }

  renderDataframe(dataframe) {
    let fields = []
    if (dataframe && typeof dataframe.schema === 'object' && Array.isArray(dataframe.schema.fields)) {
      fields = dataframe.schema.fields
    }
    return (
      <Box direction='column' fill>
        { [... fields].sort((a, b)=>{
            return a.name.localeCompare(b.name) 
          })
          .map(field => (<Box key={field.name} direction='column' 
            pad={{ vertical: 'xsmall', horizontal: 'medium' }} border='top'>
            <Text size='small' color='brand'>{field.name}</Text>
            <Text size='xsmall'>{`${field.type}`}</Text>
          </Box>)) }
      </Box>
    )
  }

  renderVariableContent() {
    return (
      <Box direction='column' fill>
        { this.props.variables.sort((a, b)=>{
            return a.name.localeCompare(b.name) 
          })
          .map(datum => (<Box key={datum.name} direction='column' 
            pad={{ vertical: 'xsmall', horizontal: 'small' }} border='bottom'>
            <Text size='small' color='brand'>{datum.name.toUpperCase()}</Text>
            <Text size='xsmall'>{`${datum.type}`}</Text>
          </Box>)) }
      </Box>
    )
    /*
    return (<Table caption="Default Table">
      <TableHeader>
        <TableRow>
          <TableCell scope='col' >
            <Text size='small' weight='bold'>Name</Text>
          </TableCell>
          <TableCell scope='col' >
            <Text size='small' weight='bold'>Type</Text>
          </TableCell>
        </TableRow>
      </TableHeader>
      <TableBody>
        {this.props.variables.sort((a, b)=>{
          return a.name.localeCompare(b.name) 
        }).map(datum => (
          <TableRow key={datum.name}>
            <TableCell scope='row' >
              <Text size='small'>{datum.name.toUpperCase()}</Text>
            </TableCell>
            <TableCell scope='row' >
              <Text size='small'>{datum.type}</Text>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>)
    */
  }

  renderContent() {
    switch(this.state.type) {
      case 'Variable':
        return this.renderVariableContent()
      case 'Schema':  
      default:
        return this.renderSchemaContent()
    }
  }

  render() {
    return (
      <Box direction='column' fill='vertical' width='300px'>
        <Box direction='row' gap='small' justify='start' pad='small'
          border={
            {
              side: 'bottom',
              size: '1px',
            }
          }
        >
          { this.renderTabs() } 
        </Box>

        <Box fill overflow={{vertical: 'auto'}} >
          { this.renderContent() }
        </Box>
      </Box>
    )
  }
}

const mapStateToProps = state => {
  let currentProjectId = state.model.currentProjectId
  let currentStepId = state.AnalysisBoard.currentStepId
  let currentProject = state.model.projects[currentProjectId]
  if (!currentProject) {
    currentProject = {}
  }

  let upstreamSteps = currentProject.dag.connections.filter(connection => connection.target === currentStepId)
    .map(connection => {
      return currentProject.steps[connection.source]
    })

  return {
    step: currentProject.steps[currentStepId],
    upstreams: upstreamSteps,
    variables: [
      ... state.model.runtimeVariables,
      ... currentProject.variables
    ]
  }
} 

const mapDispatchToProps = dispatch => {
  return {}
}

export default connect(
  mapStateToProps,
  mapDispatchToProps,
)(NodeEditorSideBar)

