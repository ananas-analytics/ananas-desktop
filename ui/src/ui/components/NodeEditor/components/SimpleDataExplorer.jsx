import React, { PureComponent } from 'react'
import PropTypes from 'prop-types'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { DataTable } from 'grommet/components/DataTable'

import { StatusGood, StatusWarning } from 'grommet-icons'

class SimpleDataExplorer extends PureComponent {
  static propTypes = {
    page     : PropTypes.number,
    pageSize : PropTypes.number,
		
		onChange : PropTypes.func,
  }

  static defaultProps = {
    page     : 0,
    pageSize : 100,

		onChange : ()=>{},
  }

  constructor(props) {
    super(props)

    this.state = {
      context : this.props.context,
      config  : this.props.config,
      page    : this.props.page,
      size    : this.props.pageSize,
      loading : false,
      errMsg  : null,
      columns : this.getColumns({}),
      data    : this.getData({}),
    }
  }

  componentDidMount() {
    this.props.ee.on('CONFIG_UPDATED', this.handleConfigUpdated, this)
    this.handleMore(true)
  }

  componentWillUnmount() {
    this.props.ee.removeListener('CONFIG_UPDATED', this.handleConfigUpdated, this)
  }

  handleConfigUpdated(config) {
    this.setState({ config: {...config}, page: 0, dataframe: null})
    this.handleMore(true)
  }

  handleMoreResponse(data) {
    if (data.code !== 200) {
      this.setState({ loading: false, errMsg: data.message })
      return
    }

    let columns  = this.getColumns(data.data)
    let newData  = this.state.data
    let nextData = this.getData(data.data)

    nextData.forEach(d => {
      newData.push(d)
    })

    if (this.state.columns.length > 0) {
      this.setState( { loading: false, errMsg: null, columns, data: newData } )
    } else {
      this.setState( { loading: false, errMsg: null, columns, data: newData } )
    }

		this.props.onChange(data.data)
  }

  getColumns(dataframe) {
    if (!dataframe || !dataframe.schema || !dataframe.data) {
      return []
    }
    let columns = dataframe.schema.fields.map(field => {
      return {
        property: field.name,
        header: field.name,
        render: datum => this.renderCellByType(field.type, datum[field.name])
      }
    })
    columns.unshift({ property: '__id__', header: '#', render: datum => datum['__id__'] })
    return columns
  }

  getData(dataframe) {
    if (!dataframe || !dataframe.schema || !dataframe.data) {
      return []
    }
    return dataframe.data.map((row, i) => {
      let rowObj = { __id__: this.state.data.length + i }
      dataframe.schema.fields.forEach((field, index) => {
        rowObj[field.name] = row[index]
      })
      return rowObj
    })
  }

  handleMore(reload) {
    if (reload) {
      this.setState({ page: 0, data: [], schema: {}, loading: true })
    } else {
      this.setState({ loading: true })
    }

    let { variableService, executionService } = this.props.context.services

    // FIXME: remove this line, no need to get upstreams for data source
    const upstreams = executionService.getUpstreamSteps(this.props.context.project, [this.props.context.step.id])

    let step = { ... this.state.context.step }
    step.config = this.props.config
    variableService.loadVariableDict(this.props.context.variables)
      .then(dict => {
        return executionService.exploreDataSource(
          this.props.context.project.id, 
          step,
          dict, 
          this.state.page,
          this.state.size,
        )
      })
      .then(res => {
        this.handleMoreResponse(res)
      })
      .catch(err => {
        this.handleMoreResponse({
          code: 500,
          message: err.message,
        })
      })
  }

  renderCellByType(type, value) {
    let v = value
    if (v === null) {
      v = 'null'
    }
    switch(typeof value) {
      case 'boolean':
        v = new Boolean(value).toString()
        break
      case 'undefined':
        v = 'null'
        break
    }
    return v
  }

  render() {
    let minWidth = this.state.columns.length * 100
    if (this.state.data.length === 0) {
      return (<div>No data yet</div>)
    }

    return (<Box direction='column'
      border='horizontal'
      margin={{vertical: 'small'}} flex fill>
      <Box fill overflow='auto' >
        <DataTable columns={this.state.columns} data={this.state.data}
        size='100%'
        style={{ minWidth: `${minWidth}px` }}
        onMore={()=>this.handleMore(false)}
      />
      </Box>
      <Box align='center'
        direction='row' height='32px' width='100%' pad='xsmall' gap='small'>
        { this.state.loading ?
          <StatusWarning size='18px' color='status-warning'/> :
          <StatusGood size='18px' color='status-ok'/>
        }
        { this.state.loading ?
          <Text size='small'>loading more data...</Text> :
          <Text size='small'>new data loaded </Text>
        }
      </Box>
    </Box>)
  }
}

export default SimpleDataExplorer
