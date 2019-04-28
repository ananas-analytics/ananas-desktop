// @flow

import React, { PureComponent } from 'react'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'

import type { PlainDataframe, NodeEditorContext } from '../../../../common/model/flowtypes.js'
import type { EventEmitter3 } from 'eventemitter3'
import type { GetDataEventOption } from '../../../model/NodeEditor'

type Props = {
  context: NodeEditorContext,
  ee: EventEmitter3,

  /*
  fields: Array<string>, // max 1 fieldname in the array
  label: string,
  size: 'small' | 'medium' | 'large' | 'xlarge' | 'xxlarge',
  color: string,
  */

  value: PlainDataframe,

  onChange: (any)=>void,
  onError: (title:string, level:string, error: Error)=>void,
  onMessage: (title:string, level:string, message:string, timeout?:number)=>void,
}

type State = {
  dataframe: PlainDataframe,
  loading: boolean,

  mode: 'EXPLORE' | 'TEST' | 'STATIC' // static: data will not change, dynamic: load data by page 
}

export default class BigNumberView extends PureComponent<Props, State> {
  static defaultProps = {
    value: {
      schema: {
        fields: [ 
          {name: 'table columns will show here', type: 'STRING'}, 
        ],
      },
      data: [],
    },
    onChange: ()=>{},
  }
    
  state = {
    dataframe: this.props.value || BigNumberView.defaultProps.value,
    loading: false,

    mode: 'STATIC'
  }


  componentDidMount() {
    this.props.ee.on('GET_DATAFRAME', this.getDataframe, this)
    // this.props.ee.on('CONFIG_CHANGED', this.configChanged, this)
  }

  componentWillUnmount() {
    this.props.ee.removeListener('GET_DATAFRAME', this.getDataframe, this)
  }

  /*
  configChanged(config) {
  }
  */

  getDataframe(options: GetDataEventOption) :void {
    switch(options.getType()) {
      case 'EXPLORE':
        this.setState({ 
          mode: 'EXPLORE', loading: true
        })
        break
      case 'TEST':
        this.setState({ 
          mode: 'TEST', loading: true 
        })
        break
      case 'JOB_RESULT':
        this.setState({ 
          loading: true 
        })
        break
      default:
        this.setState({ loading: true })
    }
    this.fetchData()
  }

  fetchData() :void {
    this.setState({dataframe: BigNumberView.defaultProps.value, loading: true})
    switch(this.state.mode) {
      case 'EXPLORE':
        this.explore()
        break
      case 'TEST':
        this.testStep()
        break
    }
    this.props.ee.emit('END_GET_DATAFRAME')
  }

  testStep() {
    let { variableService, executionService } = this.props.context.services

    variableService.loadVariableDict(this.props.context.project.id, this.props.context.variables)
      .then(dict => {
        return executionService.testStep(
          this.props.context.project.id,
          this.props.context.project.dag.connections,
          this.props.context.project.steps,
          dict,
          this.props.context.step.id)
      })
      .then(res => {
        if (res.code !== 200) {
          // TODO: handle error
          throw new Error(res.message)
        }
        let dataframe = res.data[this.props.context.step.id]
        if (!dataframe) {
          dataframe = { schema: { fields: [] }, data: [] }
        }
        this.setState({dataframe, loading: false})
        this.props.ee.emit('END_GET_DATAFRAME')
        this.props.onChange(dataframe)
        this.props.onMessage('Test Succeed!', 'success', 'Your configuaration looks good! You can RUN it to get the full result', 5000)
        // submit the config with new dataframe
        this.props.ee.emit('SUBMIT_CONFIG')
      })
      .catch(err => {
        this.setState({dataframe: BigNumberView.defaultProps.value, loading: false})
        this.props.ee.emit('END_GET_DATAFRAME')
        this.props.onError('Test Failed', 'danger', err)
      })
  }

  explore(){
    let { variableService, executionService } = this.props.context.services
    if (!variableService || !executionService) {
      return
    }
    variableService.loadVariableDict(this.props.context.project.id, this.props.context.variables)
      .then(dict => {
        return executionService.exploreDataSource(
          this.props.context.project.id,
          this.props.context.step,
          dict,
          0, // for viewer, need to start to 0 always
          99999, // for viewer, need to return all
        )
      })
      .then(res => {
        if (res.code !== 200) {
          throw new Error(res.message)
        }
        let dataframe = res.data
        if (!dataframe) {
          dataframe = { schema: { fields: [] }, data: [] }
        }
        this.setState({dataframe, loading: false})
        this.props.ee.emit('END_GET_DATAFRAME')
        this.props.onChange(dataframe)
        // submit the config with new dataframe
        this.props.ee.emit('SUBMIT_CONFIG')
      })
      .catch(err => {
        this.setState({dataframe: BigNumberView.defaultProps.value, loading: false})
        this.props.onError('Failed to get data', 'danger', err)
        this.props.ee.emit('END_GET_DATAFRAME')
      })
  }

  render() {
    let config = this.props.context.step.config
    let dataframe = this.state.dataframe || { schema: { fields: [] }, data: [] }
    let fieldname = (config.fields && config.fields.length > 0) ? config.fields[0] : ''

    if (this.state.loading) {
      return (<Box margin={{ bottom: 'large' }} fill>
        Loading ...
      </Box>)
    }

    if (dataframe.data.length === 0) {
      return (<Box>No data yet</Box>)
    } 

    let data = dataframe.data[0]
    let index = dataframe.schema.fields.findIndex(field => field.name.toUpperCase() === fieldname.toUpperCase())
    if (data.length <= index) {
      return (<Box>No data yet</Box>)
    }

    return (<Box align='center' justify='center' direction='column' fill>
      <Box align='center' direction='column' gap='medium'>
        <Text>{ config.title || '' }</Text> 
        <Text size={config.size} color={config.color}>{ data[index] }</Text>
      </Box>
    </Box>)
    
  }
}

