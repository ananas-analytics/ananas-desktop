// @flow

import React, { PureComponent } from 'react'

import { Box } from 'grommet/components/Box'

import Chart from 'react-google-charts'

import type { PlainDataframe, NodeEditorContext } from '../../../../common/model/flowtypes.js'
import type { EventEmitter3 } from 'eventemitter3'
import type { GetDataEventOption } from '../../../model/NodeEditor'

type Props = {
  context: NodeEditorContext,
  ee: EventEmitter3,

  type: 'bar' | 'line',

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
export default class BasicChartView extends PureComponent<Props, State> {
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
    dataframe: this.props.value || BasicChartView.defaultProps.value,
    loading: false,

    mode: 'STATIC'
  }


  componentDidMount() {
    this.props.ee.on('GET_DATAFRAME', this.getDataframe, this)
  }

  componentWillUnmount() {
    this.props.ee.removeListener('GET_DATAFRAME', this.getDataframe, this)
  }

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
    this.setState({dataframe: BasicChartView.defaultProps.value, loading: true})
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
        this.setState({dataframe: BasicChartView.defaultProps.value, loading: false})
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
        this.setState({dataframe: BasicChartView.defaultProps.value, loading: false})
        this.props.onError('Failed to get data', 'danger', err)
        this.props.ee.emit('END_GET_DATAFRAME')
      })
  }

  prepareRender() {
    let config = this.props.context.step.config
    let dataframe = this.state.dataframe || { schema: { fields: [] }, data: [] }

    let selectedDimension = null
    if (config.dimension && Array.isArray(config.dimension) && config.dimension.length > 0) {
      selectedDimension = config.dimension[0].toUpperCase()
    }

    let configMeasures = Array.isArray(config.measures) ? config.measures.map(measure => measure.toUpperCase()) : []

    let dimension = null
    let measures = []

    dataframe.schema.fields.map(field => {
      if (field.name.toUpperCase() === selectedDimension) {
        dimension = { name: field.name.toUpperCase(), type: field.type }
      }
      if (configMeasures && configMeasures.indexOf(field.name.toUpperCase()) >= 0) {
        measures.push({ name: field.name.toUpperCase(), type: field.type })
      }
    })

    let data = dataframe.data.map<any>(row => {
      let rowdata = {}
      dataframe.schema.fields.forEach((field, index)=> {
        if (index >= row.length) {
          rowdata[field.name.toUpperCase()] = null
        } else {
          rowdata[field.name.toUpperCase()] = row[index]
        }
      })
      return rowdata
    })

    return {
      dimension,
      measures,
      data,
    }
  }

  prepareRenderV2() {
    let config = this.props.context.step.config
    let dataframe = this.state.dataframe || { schema: { fields: [] }, data: [] }

    let selectedDimension = null
    if (config.dimension && Array.isArray(config.dimension) && config.dimension.length > 0) {
      selectedDimension = config.dimension[0].toUpperCase()
    }

    let configMeasures = Array.isArray(config.measures) ? config.measures.map(measure => measure.toUpperCase()) : []

    let dimension = null
    let measures = []

    dataframe.schema.fields.map((field, index) => {
      if (field.name.toUpperCase() === selectedDimension) {
        dimension = { name: field.name.toUpperCase(), type: field.type, index }
      }
      let measureIndex = configMeasures.indexOf(field.name.toUpperCase())
      if (configMeasures && measureIndex >= 0) {
        measures.push({ name: field.name.toUpperCase(), type: field.type, index: measureIndex })
      }
    })

    if (dimension == null) {
      return {
        dimension,
        measures,
        data: [],
      }
    }


    let header = [dimension.name, ... measures.map(m => m.name)]

    let data = dataframe.data.map<any>(row => {
      let output = []
      // $FlowFixMe
      output.push(row[dimension.index])
      measures.forEach(measure => {
        output.push(row[measure.index])
      })
      return output
    })

    data.unshift(header)
    
    return {
      dimension,
      measures,
      data,
    }
  }

  render() {
    let config = this.props.context.step.config
    let { dimension, measures, data } = this.prepareRenderV2()

    if (this.state.loading) {
      return (<Box margin={{ bottom: 'large' }} fill>
        Loading ...
      </Box>)
    }

    if (this.props.type === 'bar') {
      return (<Box margin={{bottom: 'large'}}  fill>
        <Chart
            width={'100%'}
            height={'100%'}
            chartType='ColumnChart'
            loader={<div>Loading Chart</div>}
            data={data}
            options={{
              title: config.title || '',
              chart: {
                title: config.title || '',
              },
              hAxis: {
                title: config.xlabel || 'X',
              },
              vAxis: {
                title: config.ylabel || 'Y',
              },
            }}
            rootProps={{ 'data-testid': '2' }}
          />
      </Box>)
    } else {
      return (<Box margin={{bottom: 'large'}}  fill>
        <Chart
            width={'100%'}
            height={'100%'}
            chartType='LineChart'
            loader={<div>Loading Chart</div>}
            data={data}
            options={{
              title: config.title || '',
              chart: {
                title: config.title || '',
              },
              hAxis: {
                title: config.xlabel || 'X',
              },
              vAxis: {
                title: config.ylabel || 'Y',
              },
            }}
            rootProps={{ 'data-testid': '2' }}
          />
      </Box>)
    }
    
  }
}

