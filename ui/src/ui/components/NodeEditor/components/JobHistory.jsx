// @flow

import React, { Component } from 'react'
import moment from 'moment'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'
import { Text } from 'grommet/components/Text'
import { Table } from 'grommet/components/Table'
import { TableBody } from 'grommet/components/TableBody'
import { TableHeader } from 'grommet/components/TableHeader'
import { TableRow } from 'grommet/components/TableRow'
import { TableCell } from 'grommet/components/TableCell'

import { View } from 'grommet-icons'


import type { NodeEditorContext } from '../../../../common/model/flowtypes.js'

import { JobResultOption } from '../../../model/NodeEditor'

import type { EventEmitter3 } from 'eventemitter3'


type Props = {
  context: NodeEditorContext, 
  ee: EventEmitter3, 
  label: string,

  onError: (title:string, level:string, error: Error)=>void,
  onMessage: (title:string, level:string, message:string, timeout?:number)=>void,
}

type State = {
  jobs: Array<any>, // TODO: use PlainJob here, need to refactor code 
  loading: boolean
}

export default class JobHistory extends Component<Props, State> {
  state = {
    jobs: [],
    loading: false,
  }
  interval: IntervalID // eslint-disable-line no-undef

  componentDidMount() {
    // load latest jobs, and set interval
    let { jobService } = this.props.context.services
    jobService.getJobsByStepId(this.props.context.step.id)
      .then(jobs => {
        this.setState({ jobs }) 
      })

    this.interval = setInterval(() => {
      jobService.getJobsByStepId(this.props.context.step.id)
        .then(jobs => {
          this.setState({ jobs }) 
        })
    }, 3000)
  }

  componentWillUnmount() {
    // clear interval
    clearInterval(this.interval)
  }

  handleViewJobResult(jobId: string) {
    this.props.ee.emit('START_GET_DATAFRAME')
    this.props.ee.emit('GET_DATAFRAME', new JobResultOption(jobId, `Result of job ${jobId}`) )
    setTimeout(() => {
      this.props.ee.emit('END_GET_DATAFRAME')
    }, 500)
  }

  renderJobs() {
    return (<Table caption="Jobs Table">
      <TableHeader>
        <TableRow>
          {/*
          <TableCell scope='col' >
            <Text size='small' weight='bold'>Action</Text>
          </TableCell>
          */}
          {/*
          <TableCell scope='col' >
            <Text size='small' weight='bold'>User</Text>
          </TableCell>
          */}
          <TableCell scope='col' >
            <Text size='small' weight='bold'>Created</Text>
          </TableCell>
          <TableCell scope='col' >
            <Text size='small' weight='bold'>Status</Text>
          </TableCell>
          <TableCell scope='col' >
            <Text size='small' weight='bold'>Result</Text>
          </TableCell>
        </TableRow>
      </TableHeader>
      <TableBody>
        {this.state.jobs.map((datum, index) => (
          <TableRow key={datum.id}>
            {/*
            <TableCell scope='row' >
              <Text size='small'>{datum.userName}</Text>
            </TableCell>
            */}
            <TableCell scope='row' >
              <Text size='small'>{moment(datum.createAt).format('YYYYMMDD HH:mm:ss')}</Text>
            </TableCell>
            <TableCell scope='row' >
              <Text size='small'>{datum.state.toUpperCase()}</Text>
            </TableCell>
            <TableCell scope='row' >
              { datum.message != null && datum.message !== undefined && datum.message !== '' ?
              (<Box>
                <Button icon={<View size='small'/>} 
                  hoverIndicator 
                  onClick={() => {this.props.onMessage(
                    `Message of Job ${datum.id}`,
                    datum.state.toUpperCase() === 'FAILED' ? 'danger' : 'warning',
                    datum.message ? datum.message : '',
                    10000,
                  )}}
                />
              </Box>)
              : 
              (datum.state.toUpperCase() == 'DONE' ? (<Box>
                <Button icon={<View size='small'/>} 
                  hoverIndicator 
                  onClick={() => {
                    this.handleViewJobResult(datum.id) 
                  }}
                /> 
              </Box>) : '-')
              }
            </TableCell>
           </TableRow>
        ))}
      </TableBody>
    </Table>)
  }

  render() {
    return (
      <Box>
        { this.renderJobs() }
      </Box>
    )
  }
} 
