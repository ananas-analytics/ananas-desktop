import React from 'react'
import ReactDOM from 'react-dom'

import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'

import { DragDropContextProvider } from 'react-dnd'
import HTML5Backend from 'react-dnd-html5-backend'

import reducers from './reducers'

import state from './state.js'

import App from './App'

// import services
import ServiceContext from './contexts/ServiceContext'
import { 
  ExecutionService, 
  JobService, 
  ModelService, 
  VariableService,
  NotificationService,
} from './service'

const logger              = createLogger({})
const notificationService = new NotificationService()
const modelService        = new ModelService()
const jobService          = new JobService(state.settings.apiEndpoint, notificationService)
const variableService     = new VariableService()
const executionService    = new ExecutionService(variableService)

let services = { 
  executionService, 
  jobService, 
  modelService, 
  variableService, 
  notificationService,
}

state.model.runtimeVariables = variableService.getRuntimeVariables()
const store = createStore(
  reducers,
  state,
  applyMiddleware(
    logger, // comment this line in PRODUCTION
    thunkMiddleware.withExtraArgument(services),
  )
)

modelService.setStore(store)
executionService.setStore(store)
variableService.setStore(store)

ReactDOM.render(
  <Provider store={store}>
    <DragDropContextProvider backend={HTML5Backend}>
      <ServiceContext.Provider value={services}>
        <App />
      </ServiceContext.Provider>
    </DragDropContextProvider>
  </Provider>,
  document.getElementById('app')
)

/*
window.onerror = function myErrorHandler(errorMsg, url, lineNumber) {
  alert('Error occured: ' + errorMsg)
  return false
}
*/
