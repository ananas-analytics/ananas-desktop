// @flow
import React, { Component } from 'react'
import ObjectID from 'bson-objectid'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'
import { Heading } from 'grommet/components/Heading'
import { Text } from 'grommet/components/Text'

import SelectInput from '../NodeEditor/components/SelectInput'
import TextArea from '../NodeEditor/components/TextArea'
import TextInput from '../NodeEditor/components/TextInput'

import type { ID, PlainProject } from '../../../common/model/flowtypes.js'

type Props = {
  project: ?PlainProject,
  onSubmit: (PlainProject)=>void,
  onCancel: ()=>void,
}

type State = {
  project: PlainProject,
}

export default class ProjectConfigurer extends Component<Props, State> {
  static defaultProps = {
    project: {},

    onSubmit: ()=>{},
    onCancel: ()=>{},
  }

  state = {
    project: this.props.project || {
      id: ObjectID.generate(),
      name: '',
      description: '',
      dag: { nodes: [], connections: [] },
      steps: {},
      variables: [],
    },
  }

  render() {
    let project = this.state.project

    return (<Box align='center' background={{opacity: 'strong', color: 'light-1'}}
      direction='row' justify='center' fill>
      <Box background='light-1' direction='column' elevation='xsmall'
        pad='medium' style={{minWidth: '500px'}}>
        <Box direction='column' justify='start' flex fill>
            <Heading level={4} color='brand'>{
              this.state.project != null ? 'Configurer Project' : 'New Project'
            }</Heading>
          <TextInput label='Project Name' value={project.name}
            onChange={(name)=>{
              project.name = name
              this.setState({project})
            }}
          />
          <TextArea label='Project Description' value={project.description}
            height='250px'
            onChange={(description)=>{
              project.description = description
              this.setState({project})
            }} />
        </Box>
        <Box direction='row' height='40px' justify='end' flex={false} fill='horizontal' gap='medium' margin={{top: 'medium'}}>
          <Button label='Save' primary
            onClick={()=>{
              if (this.state.project != null) {
                this.props.onSubmit(this.state.project)
              }
            }}/>
          <Button label='Cancel' onClick={()=>this.props.onCancel()}/>
        </Box>
      </Box>
    </Box>)
  }
}



