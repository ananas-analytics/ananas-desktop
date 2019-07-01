// @flow

import React, { Component, createElement } from 'react'
import EventEmitter from 'eventemitter3'

import { Anchor } from 'grommet/components/Anchor'
import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'

import registry from './components'

import MessageActions from '../../actions/Message.js'

import type { ViewData } from './editors'

import type { 
  MessageLevel, 
  PlainEngine,
  PlainUser, 
  PlainProject, 
  PlainDAG, 
  PlainStep, 
  PlainVariable,
  Dispatch,
} from '../../../common/model/flowtypes'

type Props = {
  dispatch: Dispatch,
  user: PlainUser,
  project: PlainProject,
  dag: PlainDAG,
  step: PlainStep,
  editors: {[string]:any},
  variables: Array<PlainVariable>,
  engines: Array<PlainEngine>,
  services: {[string]:any},
  onSubmit: (any)=>void,
}

type State = {
  context: {
    dispatch: Dispatch,
    user: PlainUser,
    project: PlainProject,
    dag: PlainDAG,
    step: PlainStep,
    variables: Array<PlainVariable>,
  },
  value: {[string]:any},
  viewData: ViewData,
}


type CollapsibleContainerState = {
  open: boolean
}
class CollapsibleContainer extends Component<any, CollapsibleContainerState> {
  state = {
    open: this.props.open || false
  }
  render() {
    return (<Box direction='column' margin={{vertical: 'medium'}} {... this.props}>
      { typeof this.props.label === 'string' ? <Anchor label={this.props.label} size='small' margin={{bottom: 'xsmall'}} onClick={()=>this.setState({open: !this.state.open})} /> : null }
      { this.state.open ? this.props.children : null}
    </Box>)
  } 
}


type TabContainerState = {
  tabIndex: number,
}
class TabContainer extends Component<any, TabContainerState> {
  state = {
    tabIndex: 0
  }

  renderTabs() {
    return this.props.tabs
    .map((tab, index) => {
      return (
        <Text key={tab} size='small'
          color={this.state.tabIndex === index ? 'brand' : 'dark-4'} 
          onClick={ () => this.setState({tabIndex: index}) }
          style={{
            cursor: 'pointer',
            fontWeight: this.state.tabIndex === index ? '600' : '300'
          }}
        >
          {tab}
        </Text>)
    }) 
  }

  renderContent() {
    if (this.props.children.length < this.state.tabIndex) {
      return null
    }
    return this.props.children[this.state.tabIndex]
  }

  render() {
    return (<Box direction='column' {...this.props} >
      <Box flex={false} direction='row' gap='small' justify='start' pad='small'
        border={
          {
            side: 'bottom',
            size: '1px',
          }
        }
      >
        { this.renderTabs() } 
      </Box>

      <Box flex={true} fill='vertical'>
        { this.renderContent() }
      </Box>
    </Box>)
  }
}

export default class NodeEditor extends Component<Props, State> {
  static defaultProps = {
    dag: {},
    step: {},
    variables: [],
    engines: [],
    onSubmit: ()=>{}
  }

  ee = new EventEmitter()
  state = {
    context: Object.freeze({ // XXX: context is read only!!
      dispatch: this.props.dispatch,
      user: this.props.user,
      project: this.props.project, // current project
      dag: this.props.dag, // current dag 
      step: this.props.step, // current step 
      variables: this.props.variables, // all variable definitions
      engines: this.props.engines,
      services: this.props.services,    
    }),
    viewData: this.getViewData(),
    value: this.getInitConfig(),
  }
  configHandlers = {}

  onErrorHandler = this.handleError.bind(this)
  onMessageHandler = this.handleMessage.bind(this)
  onMultipleChangesHandler = this.handleChanges.bind(this)


  getViewData() :ViewData {
    // TODO: use default page layout instead of a blank page
    if (!this.props.editors.hasOwnProperty(this.props.step.metadataId)) {
      return {
        layout: {
          key: 'root',
          props: {},
          children: [],
        },
        components: {},
      }
    }
    return this.props.editors[this.props.step.metadataId]
  }

  componentDidMount() {
    this.ee.on('SUBMIT_CONFIG', this.handleSubmit, this) 
    this.ee.on('START_EXECUTION', this.handleStartExecution, this)
    this.ee.on('END_EXECUTION', this.handleEndExecution, this)
  }

  componentWillUnmount() {
    this.ee.removeListener('SUBMIT_CONFIG', this.handleSubmit, this)
    this.ee.removeListener('START_EXECUTION', this.handleStartExecution, this)
    this.ee.removeListener('END_EXECUTION', this.handleEndExecution, this)
  }

  getInitConfig() {
    let viewData = this.getViewData()
    let config = this.props.step.config
    // inject step level properties, name, description, and dataframe
    // config = { ... config }
    config['__name__'] = this.props.step.name
    config['__description__'] = this.props.step.description
    config['__dataframe__'] = this.props.step.dataframe

    // merge the default value
    for (let key in viewData.components) {
      let view = viewData.components[key]
      if (view.bind) {
        if (typeof config[view.bind] === 'undefined') {
          config[view.bind] = view.default
        }
      } 
    }
    return config
  }

  getConfigValue(bind: string): {[string]:any} {
    return this.state.value[bind]
  }

  handleChange(bind: string, value: any) {
    let v = this.state.value
    v[bind] = value
    this.setState({ value: v })
    this.ee.emit('CONFIG_CHANGED', { name: bind, value })
  }

  handleChanges(changes: {[string]: any}) {
    for (let bind in changes) {
      this.handleChange(bind, changes[bind])
    }
  }

  handleSubmit() {
    // NOTE: the injected __xx__ property MUST be handled in the onSubmit system
    this.props.onSubmit(this.state.value)
    this.ee.emit('CONFIG_UPDATED', this.state.value)
  }

  handleStartExecution() {
  }

  handleEndExecution() {
  }

  getConfigHandler(bind: string) :(any)=>void {
    if (!this.configHandlers.hasOwnProperty(bind)) {
      this.configHandlers[bind] = v => {
        this.handleChange(bind, v)
      }
    } 
    return this.configHandlers[bind]
  }

  handleError(title: string, level: MessageLevel, error: Error) {
    this.props.dispatch(MessageActions.displayMessage(title, level, {
      body: error.message,
      timeout: 10000,
    }))
  }

  handleMessage(title: string, level: MessageLevel, message: string, timeout?: number = 5000) {
    this.props.dispatch(MessageActions.displayMessage(title, level, {
      body: message,
      timeout,
    }))
  }

  acceptConditions(conditions: {[string]: Array<any>}) {
    let match = true
    for (let bind in conditions) {
      if (!this.state.value || !this.state.value.hasOwnProperty(bind) ||
        conditions[bind].indexOf(this.getConfigValue(bind)) < 0) {
        match = false
      }
    }
    return match
  }

  render() {
    return this.renderContainer(this.state.viewData.layout, this.state.viewData.components) 
  }

  renderContainer(container: any, components: any) {
    let props = container.props || {}

    let children = container.children.map(child => {
      if (typeof child === 'string') {
        // component
        return this.renderComponent(child, components)
      } else if (typeof child === 'object') {
        // container
        return this.renderContainer(child, components)
      }
    })  

    if (container.collapsible) {
      return (<CollapsibleContainer flex={false} key={container.key} {...props} >
        { children }
      </CollapsibleContainer>)
    }

    /* render tabs */
    if (container.tabContainer) {
      // render tabs
      return (<TabContainer flex={false} key={container.key} {...props}>
        { children }
      </TabContainer>)
    }

    return (<Box flex={false} key={container.key} {...props} >
      { children }
    </Box>)
  }

  renderComponent(key: string, components: {[string]:any}) {
    if (!components.hasOwnProperty(key)) {
      return null
    }
    let view = components[key]

    let conditions = view.conditions
    
    if (!this.acceptConditions(conditions)) {
      return null
    }    

    let props = view.props || {}
    // inject common props, these props are reserved
    props.ee = this.ee
    props.context = this.state.context  
    props.config = this.state.value
    if (view.bind) {
      props.value = this.getConfigValue(view.bind)
      // callback for changing bind config
      props.onChange = this.getConfigHandler(view.bind) 
    }
    props.onMessage = this.onMessageHandler
    props.onError = this.onErrorHandler
    // general config change callback, change multiple configs
    // props.onMultipleChanges = this.onMultipleChangesHandler

    // create component instance
    let comp = registry[view.type]
    if (!comp) {
      comp = registry['PlaceHolder']
    }
    let elem = createElement(comp, props)
    let boxStyle = view.box || {}
    return (<Box key={key} {...boxStyle}>{elem}</Box>)
  }
}

