// @flow

export type ID = string

export type VariableDictionary = {[string]:string}

export type NodeType = "Source" | "Transform" | "Destination" | "Visualization"

export type StepType = "connector" | "transformer" | "loader" | "viewer"

// see https://flow.org/en/docs/react/redux//
export type State = any
export type Action = {
  +type: string
}

export type Dispatch = (action: Action | ThunkAction | PromiseAction) => any
export type GetState = () => State
export type ThunkActionArgs = any
export type ThunkAction = (dispatch: Dispatch, getState: GetState, args:
                            ThunkActionArgs) => any
export type PromiseAction = Promise<Action>

/**
 * User
 */
export type PlainUser = {
  id           : string,
  name         : string,
  email        : string,
  subscription : PlainSubscription,
  token        : string,
}

export type PlainSubscription = {
  id          : string,
  plan        : string,
  maxProjects : number,
  expires     : number,
}

/**
 * Dataframe
 */
export type PlainField = {
  name: string,
  type: string,
}

export type PlainDataframe = {
  schema: {
    fields: Array<PlainField>,
  },
  data: Array<Array<any>>,
}

/**
 * DAG
 */
export type PlainNodeMetadata = {
  id          : string,
  name        : string,
  icon        : string,
  description : string,
  type        : StepType,
  step        : PlainStep,
  options     : {
    maxIncoming : number,
    maxOutgoing : number,
  },
} 

export type PlainNode = {
  id       : ID,
  x        : number,
  y        : number,
  label    : string,
  type     : NodeType,
  metadata : PlainNodeMetadata,
}

export type PlainConnection = {
  source: ID,
  target: ID,
}

export type PlainDAG = {
  nodes       : Array<PlainNode>,
  connections : Array<PlainConnection>
}

/**
 * Project
 */
export type ProjectMeta = {
  id   : string,
  path : string,
}

export type PlainProject = {
  id          : ID,
  path?       : string,
  name        : string,
  description : string,
  dag         : PlainDAG,
  steps       : {[string] : PlainStep},
  variables   : Array<PlainVariable>,
  settings    : Setting,
  deleted?    : boolean,
}

/**
 * Step
 */
export type PlainStep = {
  id          : ID,
  name        : string,
  description : string,
  metadataId  : string,
  type        : StepType,
  config      : {[string] : any},
  dataframe   : PlainDataframe,
  dict        : {},
}

/**
 * Variable
 */
export type VariableType  = "string" | "number" | "date"
export type VariableScope = "runtime" | "organization" | "project"

export type PlainVariable = {
  name        : string,
  type        : VariableType,
  description : string,
  scope       : VariableScope,
}

/**
 * Engine
 */
export type EngineType  = "Flink" | "Spark" 
export type EngineScope = "runtime" | "workspace"

export type PlainEngine = {
  name        : string,
  type        : EngineType,
  description : string,
  scope       : EngineScope,
  properties  : { [string]: string }
}

export type EngineTemplate = {
  name: string,
  label: string,
  type: string, // string, number, boolean
  description: string,
  default: string,
  advance: boolean
}

/**
 * Job
 */
export type Env = {
  name: string,
  type: string,
}

export type PlainJob = {
  id         : ID,
  userId     : ID,
  userName   : string,
  goals      : Array<ID>,
  env        : Env,
  state      : string,
  message    : ?string,
  updateTime : number,
  createTime : number,
}

/**
 * Message
 */
export type MessageLevel = 'info' | 'success' | 'warning' | 'danger'
export type MessageOptions = {
  body?    : string,
  timeout? : number
}

/**
 * Node Editor
 */
export type NodeEditorContext = {
  user      : PlainUser,
  project   : PlainProject,
  dag       : PlainDAG,
  step      : PlainStep,
  variables : Array<PlainVariable>,
  engines   : Array<PlainEngine>,
  services  : {[string] : any},
}


/**
 * Settings
 */
export type Setting = {
  [string]: any
}

/**
 * Utils
 */
export type APIResponse<T> = {
  code     : number,
  message? : string,
  data?    : T,
}

