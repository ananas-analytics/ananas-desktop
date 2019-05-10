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

import proxy from './proxy'

// import services
import ServiceContext from './contexts/ServiceContext'
import { 
  ExecutionService, 
  JobService, 
  ModelService, 
  VariableService,
  NotificationService,
  NodeMetadataService,
} from './service'

// #if process.env.NODE_ENV !== 'production'
const logger              = createLogger({})
// #endif
const notificationService = new NotificationService()
const modelService        = new ModelService()
const jobService          = new JobService(state.settings.runnerEndpoint, notificationService)
const variableService     = new VariableService()
const executionService    = new ExecutionService(variableService)
const nodeMetadataService = new NodeMetadataService()

// first get local user name
proxy.getLocalUserName()
  .then(username => {
    state.model.user.name = username 
    // load metadata, then init the app
    return nodeMetadataService.load()
  })
  .then(metadata => {
    let services = { 
      executionService, 
      jobService, 
      modelService, 
      variableService, 
      notificationService,
      nodeMetadataService,
    }
    state.model.metadata = metadata
    state.model.runtimeVariables = variableService.getRuntimeVariables()
    const store = createStore(
      reducers,
      state,
      applyMiddleware(
        // #if process.env.NODE_ENV !== 'production'
        logger, 
        // #endif
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
  })
  .catch(err => {
    console.error(err.message, err.stack)
  })




