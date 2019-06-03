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
  MetadataService,
  SettingService,
} from './service'

// #if process.env.NODE_ENV !== 'production'
const logger              = createLogger({})
// #endif
const notificationService = new NotificationService()
const modelService        = new ModelService()
const jobService          = new JobService(state.Settings.global.runnerEndpoint, notificationService)
const variableService     = new VariableService()
const executionService    = new ExecutionService(variableService)
const metadataService     = new MetadataService()
const settingService      = new SettingService()

// start initializing application
proxy.getLocalUserName()
  .then(username => {
    state.model.user.name = username 
    return settingService.loadGlobalSettings()
  })
  .then(globalSettings => {
    state.Settings.global = Object.assign({ // default settings
      runnerEndpoint: 'http://localhost:3003/v1'
    }, globalSettings) 
    return modelService.loadExecutionEngines()
  })
  .then(engines => {
    if (engines.length > 0) {
      state.ExecutionEngine.engines = engines
    } 
    // load metadata, then init the app
    return Promise.all([
      metadataService.loadNodeMetadata(),
      metadataService.loadEditorMetadata()
    ])
  })
  .then(metadatas => {
    let services = { 
      executionService, 
      jobService, 
      modelService, 
      variableService, 
      notificationService,
      metadataService,
      settingService,
    }
    state.model.metadata = {
      node: metadatas[0],
      editor: metadatas[1],
    }
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
    settingService.setStore(store)

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
    // TODO: report error to main process and display 
    console.error(err.message, err.stack)
  })




