// @flow

import React, { PureComponent } from 'react'

import ReactTable, { ReactTableDefaults } from 'react-table'

import 'react-table/react-table.css'
// $FlowFixMe
import './DataTable.scss'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'

import { GetDataEventOption } from '../../../model/NodeEditor'

// https://github.com/tannerlinsley/react-table/issues/730
const columnDefaults = { ...ReactTableDefaults.column, headerClassName: 'datatable-header' }

import type { PlainDataframe, NodeEditorContext } from '../../../../common/model/flowtypes.js'
import type { EventEmitter3 } from 'eventemitter3'

type Props = {
  uncontrolled: boolean,
  pagination: boolean,
  pageSize: number,
  context: NodeEditorContext,
  ee: EventEmitter3,

  caption: string,
  value: PlainDataframe,

  onChange: (any)=>void,
  onError: (title:string, level:string, error: Error)=>void,
  onMessage: (title:string, level:string, message:string, timeout?:number)=>void,
}

type State = {
  description: string,
  dataframe: PlainDataframe,
  page: number, // for explorer data
  pageSize: number, // for explorer data
  loading: boolean,

  mode: 'EXPLORE' | 'TEST' | 'JOB_RESULT' | 'STATIC' // static: data will not change, dynamic: load data by page 
}

/**
 * DataTable works in two different mode:
 *
 * 1. static display mode, with default data from value property
 * During initiation, all data (a dataframe) are passed through the value property. Pagination is managed by DataTable itself 
 *
 * 2. dynamic display mode
 * When a data retrieving event is sent from outside to inform DataTable to retrieve data externally, pagination is managed 
 * by underlying API.
 *
 * Usually, after initiation, DataTable works in static display mode, once it receives the data retrieving related event, it 
 * changes the mode to dynamic display mode 
 *
 * Data retrieving event: GET_DATAFRAME
 *
 * event options: {
 *  type: 'EXPLORE' | 'TEST' | 'JOB_RESULT'
 *
 *  jobid: 'the job id' //only available when type is JOB_RESULT
 * }
 *
 * 
 */
export default class DataTable extends PureComponent<Props, State> {
  static defaultProps = {
    uncontrolled: false,
    pagination: true,
    pageSize: 10,
    caption: '',
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
    description: '', // a short text to describe what's in the table
    dataframe: this.props.value || DataTable.defaultProps.value,
    page: 0,
    pageSize: this.props.pageSize || 25, 

    loading: false,
    mode: 'STATIC',
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
          description: options.getDescription(), 
          mode: 'EXPLORE', loading: true, page: 0 
        })
        break
      case 'TEST':
        this.setState({ 
          description: options.getDescription(), 
          mode: 'TEST', loading: true, page: 0 
        })
        break
      case 'JOB_RESULT':
        this.setState({ 
          description: options.getDescription(), 
          mode: 'JOB_RESULT', loading: true, page: 0 
        })
        break
      default:
        this.setState({ description: '', mode: 'STATIC', loading: true, page: 0 })
    }
    this.fetchData(this.state.page, this.state.pageSize)
  }

  fetchData(page: number, pageSize: number) :void {
    this.setState({dataframe: DataTable.defaultProps.value, loading: true})
    switch(this.state.mode) {
      case 'EXPLORE':
        this.explore(page, pageSize, false)
        break
      case 'TEST':
        this.testStep()
        break
      case 'JOB_RESULT':
      default: // show static data from props value 
        this.setState({ dataframe: this.props.value || DataTable.defaultProps.value, loading: false, page: 0 })
        this.props.ee.emit('END_GET_DATAFRAME')
    }
  }

  explore(page: number, pageSize: number, controlled: boolean) {
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
          page,
          pageSize,
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
        this.setState({dataframe, page: controlled ? page : undefined, loading: false})
        this.props.ee.emit('END_GET_DATAFRAME')
        this.props.onChange(dataframe)
        // submit the config with new dataframe
        this.props.ee.emit('SUBMIT_CONFIG')
      })
      .catch(err => {
        this.setState({dataframe: DataTable.defaultProps.value, page: undefined, loading: false})
        this.props.onError('Failed to get data', 'danger', err)
        this.props.ee.emit('END_GET_DATAFRAME')
      })
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
        this.setState({dataframe, loading: false, page: undefined})
        this.props.ee.emit('END_GET_DATAFRAME')
        this.props.onChange(dataframe)
        this.props.onMessage('Test Succeed!', 'success', 'Your configuaration looks good! Connect a Destination or a Visualization, and RUN it to get the full result', 5000)
        // submit the config with new dataframe
        this.props.ee.emit('SUBMIT_CONFIG')
      })
      .catch(err => {
        this.setState({dataframe: DataTable.defaultProps.value, page: undefined, loading: false})
        this.props.ee.emit('END_GET_DATAFRAME')
        this.props.onError('Test Failed', 'danger', err)
      })
  }

  handleRunStep() {
    this.setState({dataframe: DataTable.defaultProps.value})
    let { variableService, executionService } = this.props.context.services

    variableService.loadVariableDict(this.props.context.project.id, this.props.context.variables)
      .then(dict => {
        console.log(this.props.context.project)
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
          // TODO: handle error
        }
        this.props.ee.emit('END_GET_DATAFRAME')
      })
      .catch(err => {
        console.error(err)
        this.props.ee.emit('END_GET_DATAFRAME')
      })

  }

  showPagination() {
    if (this.state.mode === 'STATIC') {
      if (this.props.pagination) {
        return true
      } else {
        return false
      } 
    } else {
      return true
    }
  }

  getColumns() {
    let dataframe = this.state.dataframe
    if (this.props.uncontrolled) {
      dataframe = this.props.value 
    }

    

    if (dataframe.schema.fields.length === 0) {
      return [
        {
          id: 'default_column',
          Header: <Text size='small' color='brand'>Your columns will be shown here</Text>,
          minWidth: 100,
          resizable: true,
        }
      ]
    }
    return dataframe.schema.fields.map<any>(field=>{
      return {
        id: field.name,
        Header: <Text size='small' color='brand'>{field.name}</Text>,
        accessor: row => this.dataAccessor(field.name, field.type, row),
        minWidth: 100,
        resizable: true,
      }
    })
  }

  getData() {
    let dataframe = this.state.dataframe
    if (this.props.uncontrolled) {
      dataframe = this.props.value
    }
    return dataframe.data.map<any>(row => {
      let rowdata = {}
      dataframe.schema.fields.forEach((field, index)=> {
        if (index >= row.length) {
          rowdata[field.name] = null
        } else {
          rowdata[field.name] = row[index]
        }
      })
      return rowdata
    })
  }

  isControlledTable() {
    return this.state.mode === 'STATIC' || this.state.mode === 'TEST'
  }

  getFetchDataFunc() {
    if (this.isControlledTable()) {
      return undefined
    } 
    return (state: any) => {
      this.fetchData(state.page, state.pageSize)
    }
  }

  dataAccessor(name:string, type:string, row:any) {
    let value = row[name]
    if (value === null || value === undefined) {
      return 'null'
    }
    if (typeof value === 'boolean') {
      // $FlowFixMe
      return String.valueOf(value)
    }
    switch(type) {
      case 'VARCHAR':
      default:
        return value
    }
  }

  render() {
    let columns = this.getColumns()
    let data = this.getData()
    return (
      <Box margin={{bottom: 'small'}} fill>
        <Box flex={false} margin={{vertical: 'small'}}>
          <Text size='small' color='neutral-1'>{this.state.description}</Text>
        </Box>
        <ReactTable 
          noDataText='No data yet!'
          columns={columns}
          data={data}

          sortable={false}
          multiSort={false}
          resizable={false}
          showPageSizeOptions={false}
          showPageJump={false}
          showPagination={this.showPagination()}
          defaultPageSize={this.props.pageSize}
          ofText=''

          column={columnDefaults}
            
          loading={this.state.loading}
          manual={!this.isControlledTable()}
          page={this.state.page}
          pages={!this.isControlledTable() ? 999 : undefined}
          onFetchData={this.getFetchDataFunc()}

          renderTotalPagesCount={()=>null}
        />
      </Box>
    )
  }
}
