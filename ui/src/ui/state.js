// get last modified project and configures
const state = {
  AppSideBar: {
    activeMenu: 0,
    expand: false,
  },
  AppToolBar: {
    contextSideBarExpanded: true,
    path: {
      //project: { id: 'test-project-id', name: 'Test Project' },
      /*
      project: { id: 'test-project-id', name: 'Test Project' },
      app: {
        id: 1,
        name: 'Analysis Board',
        // view: { id: '', name: 'CSV source' }
      },
      */
    }
  },

  /* each tab will have its own sub state object */
  AnalysisBoard: {
    currentStepId: null, // current working step
    showEditor: false, // if showing the editor
  },

  /*
  Report: {
  },
  Automation: {
  },
  Monitoring: {
  },
  Variables: {
  },
  */

  Message: {
    open: true,
    messages: [], 
  },

  /* data model */
  model: {
    user: {
      id: 'default user',
      name: 'Welcome',
      email: 'user@ananas.com',
      subscriptionId: 'subscription',
      token: '123',
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
      },
      // other projects here
    },
    runtimeVariables: [], 
  },

  /* settings */
  settings: {
    runnerEndpoint: 'http://localhost:3003/v1',
  }
}

export default state
