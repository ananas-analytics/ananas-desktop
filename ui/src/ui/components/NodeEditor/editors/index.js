// @flow

export type Component = {
  bind?: string,
  type: string,
  box?: {[string]:any},
  default?: any,
  props: {[string]:any}
}

export type Container = {
  key: string,
  props: {[string]:any},
  children: Array<Container|string>
}

export type ViewData = {
  layout: Container,
  components: {[string]:Component}
}

/*
import excelSourceView from './excel_source.js'
import jsonSourceView from './json_source'
import mysqlSourceView from './mysql_source'
import postgresqlSourceView from './postgresql_source'
import mongoSourceView from './mongo_source'

import sqlTransformView from './sql_transform'
import joinTransformView  from './join_transform'
import concatTransformView  from './concat_transform'

import mysqlDestinationView from './mysql_destination'
import postgresqlDestinationView from './postgresql_destination'
import mongoDestinationView from './mongo_destination'


export function getViewData(type: string, config: {[string]:any}) :ViewData {
  switch(type) {
    case 'connector':
      switch(config.subtype) {
        case 'file': 
          if (config.format === 'csv') return csvSourceView 
          if (config.format === 'json') return jsonSourceView 
          if (config.format === 'excel') return excelSourceView
          break
        case 'jdbc':
          if (config.database === 'mysql') return mysqlSourceView
          if (config.database === 'postgres') return postgresqlSourceView
          break
        case 'mongo':
          return mongoSourceView
      }
      break
    case 'transformer':
      switch(config.subtype) {
        case 'sql': 
          return sqlTransformView 
        case 'join':
          return joinTransformView
        case 'concat':
          return concatTransformView
      }
      break
    case 'loader':
      switch(config.subtype) {
        case 'file':
          if (config.format === 'csv') return csvDestinationView  
          break
        case 'jdbc':
          if (config.database === 'mysql') return mysqlDestinationView
          if (config.database === 'postgres') return postgresqlDestinationView
          break
        case 'mongo':
          return mongoDestinationView
      }
      break
    case 'viewer':
      switch(config.subtype) {
        case 'bar chart':
          return barChartViewerView
        case 'line chart':
          return lineChartViewerView 
        case 'big number':
          return bignumberViewerView
      }
      break
  }
  return {
    layout: {
      key: 'main',
      props: {},  
      children: [],
    },
    components: {},
  }
}
*/
