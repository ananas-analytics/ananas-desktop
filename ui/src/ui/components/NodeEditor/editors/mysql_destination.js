export default {
  layout: {
    key: 'root',
    props: { direction: 'row', fill: true },
    children: [
      {
        key: 'left-bar',
        props: { direction: 'column', elevation: 'small', fill: 'vertical', width: '400px', style: { minWidth: '300px'}},
        children: [
          {
            key: 'scrollable-editor',
            props: { flex: true, overflow: {vertical: 'auto'}, pad: 'small' },
            children: [
              {
                key: 'inner-scrollable-editor',
                props: { flex: false },
                children: [
                  'title',
                  {
                    key: 'basic-editor',
                    collapsible: true,
                    props: { label: 'Basic Settings', open: true },
                    children: [
                      'db-url',
                      'db-user',
                      'db-password',
                      'table-name',
                      'sql-editor'
                    ]
                  },
                  {
                    key: 'advanced-editor',
                    collapsible: true,
                    props: { label: 'Advanced' },
                    children: [
                      'description',
                      'overwrite',
                    ]
                  },
                  {
                    key: 'job-history-container',
                    collapsible: true,
                    props: { label: 'Job History', open: true },
                    children: [
                      'job-history',
                    ]
                  }
                ]

              }
            ],
          },
          {
            key: 'update-container',
            props: { 
              boder: {side: 'top', size: 'xsmall', color: 'light-4'},
              direction: 'column', height: '50px', justify: 'center',
              pad: { horizontal: 'medium', vertical: 'xsmall' }
            },
            children: [
              'update-btn'
            ]
          },
        ]
      },
      {
        key: 'main',
        props: { direction: 'column', flex: true, fill: true, pad: {top: 'small', left: 'small', right: 'small'} },
        children: [
          'variable-editor',
          'table-title',
          'explorer-view',
        ]
      }   
    ],
  },

  components: {
    'title': {
      bind: '__name__',
      type: 'TextInput',
      default: 'MySQL Destination',
      props: { label: 'Title' }
    },
    'db-url': {
      bind: 'url',
      type: 'TextInput',
      default: 'mysql://localhost:3306/[your database]',
      props: { label: 'Database URL' }
    },
    'table-name': {
      bind: 'tablename',
      type: 'TextInput',
      default: '',
      props: { label: 'Table Name' }
    },
    'db-user': {
      bind: 'user',
      type: 'TextInput',
      default: 'root',
      props: { label: 'Database User' }
    },
    'db-password': {
      bind: 'password',
      type: 'TextInput',
      default: '',
      props: { label: 'Database Password', type: 'password' }
    },
    'description': {
      bind: '__description__',
      type: 'TextArea',
      default: 'Describe this step here',
      props: { label: 'Description' },
    },
    'overwrite': {
      bind: 'overwrite',
      type: 'CheckBox',
      default: true,
      props: { label: 'Overwrite' } 
    },
    'sql-editor': {
      bind: 'sql',
      type: 'CodeEditor',
      default: 'SELECT * FROM [YOUR TABLE]',
      props: { label: 'Explore your data with SQL', options: { mode: 'sql' } }
    },
    'job-history': {
      type: 'JobHistory',
      box: {},
      props: {}
    },
    'update-btn': {
      type: 'Button',
      props: { label: 'Update', event: 'SUBMIT_CONFIG' }
    },
    'variable-editor': {
      type: 'VariablePicker',
      box: { flex: false, margin: { bottom: 'medium', top: 'small' }},
      props: { exploreButton: true, testButton: true, runButton: true },
    },
    'table-title': {
      type: 'Heading',
      box: { flex: false },
      props: { text: 'Result', level: 4 },
    },
    'explorer-view': {
      bind: '__dataframe__', 
      type: 'DataTable',
      box: { flex: true, fill: true },
      props: { pageSize: 25 },
    }
  }
}
