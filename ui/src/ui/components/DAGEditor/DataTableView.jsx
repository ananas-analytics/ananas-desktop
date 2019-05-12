// @flow

import React, { Component } from 'react'
import styled from 'styled-components'
import EventEmitter from 'eventemitter3' 

import { Anchor } from 'grommet/components/Anchor'
import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { Collapsible } from 'grommet/components/Collapsible'
import { Markdown } from 'grommet/components/Markdown'

import DataTable from '../NodeEditor/components/DataTable'

const PanelButton = styled(Box)`
  &:hover {
    cursor: pointer;
  }
`

import { LinkTop, LinkBottom } from 'grommet-icons'

import type { PlainUser, PlainProject, PlainStep } from '../../../common/model/flowtypes.js'

type Props = {
  user: PlainUser,
  project: PlainProject,
  step: PlainStep,
  open: boolean
}

type State = {
  open: boolean
}


class DataTableView extends Component<Props, State> {
  static defaultProps = {
    open: false,
    step: undefined
  }

  state = {
    open: this.props.open
  }

  renderPanelButtons() {
    if (this.state.open) {
      return (
        <PanelButton background='brand' pad='3px' round='3px'>
          <LinkBottom size='small' onClick={() => this.setState({open: !this.state.open})} />
        </PanelButton>
      )
    }

    return (
      <PanelButton background='brand' pad='3px' round='3px'>
        <LinkTop size='small' onClick={() => this.setState({open: !this.state.open})} />
      </PanelButton>
    )

  }

  render() {
    let dataframe = this.props.step ? this.props.step.dataframe : undefined
    if (dataframe) {
      dataframe = { ... dataframe }
      dataframe.data = dataframe.data.slice(0, 6)
    }
    return (
      <Box border={{
          side: 'top',
          size: 'small'
        }} overflow='hidden' >

        <Box align='center' border={{side:'bottom', size: 'xsmall'}}
          direction='row' justify='between'
          pad={{ horizontal: 'small', vertical: 'xsmall' }} >

          <Text size='xsmall'> Preview </Text>

          { this.renderPanelButtons() }
        </Box>

        <Collapsible direction='vertical' open={this.state.open} fill>
          <Box direction='row' height='300px' >
            <Box direction='column' width='50%' pad='small' overflow='auto'>
              <Box flex={false} border='bottom' margin={{bottom: 'small'}}>
                <Text size='small'>Description</Text>
              </Box>
              <Markdown components={{
                'a': {
                  component: Anchor,
                  props: { target: '_blank' }
                }
              }}>
                { this.props.step ? this.props.step.description : '' }
              </Markdown>
            </Box>
            <Box flex fill pad='small'>
              <Box flex={false} border='bottom' margin={{bottom: 'small'}}>
                <Text size='small'>Output Example</Text>
              </Box>
              <Box>
                <DataTable value={dataframe} ee={new EventEmitter()} context={{
                  user: this.props.user,
                  project: this.props.project,
                  dag: this.props.project.dag,
                  step: this.props.step,
                  services: {},
                  variables: [],
                  engines: [],
                  }} 
                  uncontrolled 
                  pagination={false}
                  pageSize={6}
                  onError={()=>{}}
                  onMessage={()=>{}}
                />
              </Box>
            </Box>
          </Box>
        </Collapsible>

      </Box>
    )
  }
}

export default DataTableView

