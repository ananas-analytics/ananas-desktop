// @flow

const app = require('electron').remote.app

import moment from 'moment'

import proxy from '../proxy'

import type { ID, PlainVariable } from '../../common/model/flowtypes.js'

class Expression {
  // the expression string
  expression: string
  // the variable name
  variable: string
  // the modifiers
  modifiers: Array<(v:any) => any>

  constructor(expression: string, variable: string, modifiers: Array<string>) {
    this.expression = expression
    this.variable = variable
    this.parseModifiers(modifiers)
  }

  parseModifiers(modifiers) {
    this.modifiers = modifiers.map(modifier=>{
      if (modifier.startsWith('dateFormat(')) {
        let format = modifier.substring(11, modifier.length-1)  
        return v=>{
          if (moment.isMoment(v)) {
            return v.format(format)
          }
          if (typeof v === 'string') {
            return moment(v).format(format)
          }
          return v
        }
      } else {
        // if unknown modifier return value itself
        return v=>v
      }
    })
  }

  eval(value: any) :any {
    let ret = value
    this.modifiers.forEach(modifier => {
      ret = modifier(ret)
    })
    return ret
  }
}

export default class VariableService {
  store: any
  dict: {[string]: PlainVariable}

  getServiceName() {
    return 'VariableService'
  }

  setStore(store:any) {
    this.store = store
  }
  
  updateDictionary(variables: Array<PlainVariable>) {
    this.dict = {}
    variables.forEach(v => {
      this.dict[v.name.toUpperCase()] = v
    })
  }

  findExpressions(str :string) :Array<Expression> {
    // match {variable_name|modifier1()|...|modifier2()}
    const expressionMatcher = /\{\s*([0-9a-zA-Z\-_]+)(?:\s*\|\s*((?:[0-9a-zA-Z\-_]+\([^\)]*\)(?:\s*\|\s*)?)+))?\s*\}/gi
    let expressions = []
    let result = expressionMatcher.exec(str)
    while(result) {
      let match = result[0]
      let name = result[1]
      let modifiers = result[2]
      if (typeof modifiers === 'string') {
        modifiers = modifiers.split('|')
      } else {
        modifiers = []
      }
      expressions.push(new Expression(match, name.toUpperCase(), modifiers))
      
      // find next
      result = expressionMatcher.exec(str)
    }
    return expressions  
  }

  parseStepConfig(config: {[string]: any}) :{[string]:Expression} {
    // TODO: do we need to do nested search? Will there be an object as a config item?
    let exps = {}
    let founds = null
    for (let k in config) {
      switch(typeof config[k]) {
        case 'string':
          founds = this.findExpressions(config[k])  
          founds.forEach(found=>{
            exps[found.expression] = found
          })
          break
        default:
          break
      }
    }
    return exps
  }

  injectExpressions(config: {[string]: any}, dict: {[string]:string}): {[string]: any} {
    // TODO: nested inject
    let ret = {}
    for (let k in config) {
      ret[k] = config[k]
      if (typeof config[k] === 'string') {
        for (let d in dict) {
          ret[k] = ret[k].replace(d, dict[d])
        } 
      }
    }
    return ret
  }

  getRuntimeVariables() :Array<PlainVariable> {
    return [
      {
        name: 'EXECUTE_DATE',
        type: 'date',
        description: 'Job execution date',
        scope: 'runtime',
      },
      {
        name: 'PROJECT_PATH',
        type: 'string',
        description: 'Project full path',
        scope: 'runtime',
      }
    ]
  }

  saveVariableDict(projectID: ID, dict: {[string]: any}) :Promise<'OK'> {
    return proxy.saveProjectVariableDict(projectID, dict)
  }

  loadVariableDict(projectID: ID, variables: Array<PlainVariable>) :Promise<{[string]: any}> {
    return proxy.getProjectVariableDict(projectID)
      .then(dict => {
        if (Object.keys(dict).length === 0) {
          let defaultDict = {}
          variables.forEach(v => {
            if (v.type === 'date') {
              defaultDict[v.name] = { name: v.name, type: 'date', value: moment().toISOString() }  
            } else if (v.type === 'string') {
              defaultDict[v.name] = { name: v.name, type: 'string', value: '' }
            } else if (v.type === 'number') {
              defaultDict[v.name] = { name: v.name, type: 'number', value: '0' }
            }
          })
          return defaultDict
        }

        for (let k in dict) {
          let variable = variables.find(v => v.name === k)
          dict[k] = {
            name: k,
            type: variable ? variable.type : 'string',
            value: dict[k],
          }
        } 

        let newDict = { ... dict }  
        // override runtime variables, and remove unexpected variables
        newDict['EXECUTE_DATE'] = {name: 'EXECUTE_DATE', type: 'date', value: moment().format()} 
        newDict['PROJECT_PATH'] = {name: 'PROJECT_PATH', type: 'string', value: this.getProjectPath(projectID)}
        return newDict
      })
  }

  getProjectPath(projectID: ID) :string{
    const state = this.store.getState()
    let project = state.model.projects[projectID]
    if (project && project.path) {
      return project.path
    }
    return `${app.getPath('userData')}/${projectID}`
  }
}
