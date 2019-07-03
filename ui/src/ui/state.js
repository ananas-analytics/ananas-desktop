const state = {
  AppSideBar: {
    activeMenu: 0,
    expand: false,
  },
  AppToolBar: {
    contextSideBarExpanded: true,
    path: {}
  },

  /* each tab will have its own sub state object */
  AnalysisBoard: {
    currentStepId: null, // current working step
    showEditor: false, // if showing the editor
  },

  ExecutionEngine: {
    engines: [
      {
        name: 'LOCAL',
        type: 'Flink',
        scope: 'runtime',
        description: 'default local Flink engine',
        properties: {
          parallelism: 10,
          maxBundleSize: 1000000,
          objectReuse: true,
          database_type: 'derby',
          database_url: null,
          database_user: null,
          database_pass: null,
        }
      }
    ],
    templates: {
      Dataflow: [
        { name: 'projectId', label: 'ProjectId', type: 'string', description: '', default: '', advanced: false },
        { name: 'tempLocation', label: 'Google Cloud Storage Temp Location', type: 'string', description: '', default: 'gs://<bucket>/tmp', advanced: true },
        { name: 'streaming', label: 'Use Streaming', type: 'boolean', description: '', default: 'false', advanced: true },
      ],
      Flink: [
        { name: 'flinkMaster', label: 'Master URL', type: 'string', description: '', default: '[auto]', advanced: false },
        { name: 'tempLocation', label: 'Temp Location', type: 'string', description: '', default: '/tmp/', advanced: true },
        { name: 'parallelism', label: 'Parallelism', type: 'number', description: '', default: '10', advanced: false },
        { name: 'maxBundleSize', label: 'Max Bundle Size', type: 'number', description: '', default: '1000000', advanced: true },
        { name: 'objectReuse', label: 'Enable Object Reuse', type: 'boolean', description: '', default: 'true', advanced: true },
      ],
      Spark: [
        { name: 'sparkMaster', label: 'Master URL', type: 'string', description: '', default: '', advanced: false },
        { name: 'tempLocation', label: 'Temp Location', type: 'string', description: '', default: '/tmp/', advanced: true },
        { name: 'streaming', label: 'Use Streaming', type: 'boolean', description: '', default: 'false', advanced: true },
        { name: 'enableMetricSinks', label: 'Enable Metric Sinks', type: 'boolean', description: '', default: 'true', advanced: true },
      ]
    }
  },

  Scheduler: {
    triggers: [],
  },

  Message: {
    open: true,
    messages: [], 
  },

  /* data model */
  model: {
    user: {
      id: 'local-user',
      name: '',
      email: 'opensource@ananasanalytics.com',
      subscriptionId: 'not-used',
      token: 'not-used',
    },
    currentProjectId: null,
    projects: { // all project related data here
      'test-project-id': {
        id: 'test-project-id',
        name: 'Test Project',
        dag: {
          nodes: [],
          connections: [],
        },
        steps: {},
        variables: [],
        settings: {}, // project based settings
        triggers: [],
      },
      // other projects here
    },
    runtimeVariables: [], 
    metadata: {
      node: [],
      editor: {},
    }, // metadata    
  },

  /* global settings */
  Settings: {
    global: {
      runnerEndpoint: 'http://localhost:3003/v1',
    }
  }
}

export default state
