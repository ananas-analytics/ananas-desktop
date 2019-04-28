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
                  'host',
                  'port',
                  'database',
                  'collection',
                  'is-text',
                  {
                    key: 'advanced-editor',
                    collapsible: true,
                    props: { label: 'Advanced' },
                    children: [
                      'description',
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
      default: 'MySQL Source',
      props: { label: 'Title' }
    },
    'host': {
      bind: 'host',
      type: 'TextInput',
      default: 'localhost',
      props: { label: 'Mongo Host' }
    },
    'port': {
      bind: 'port',
      type: 'TextInput',
      default: '27017',
      props: { label: 'Mongo Port' }
    },
    'database': {
      bind: 'database',
      type: 'TextInput',
      default: '',
      props: { label: 'Database' }
    },
    'is-text': {
      bind: 'text',
      type: 'CheckBox',
      default: false,
      props: { label: 'extract each row as raw text?' }
    },
    'collection': {
      bind: 'collection',
      type: 'TextInput',
      default: '',
      props: { label: 'Collection' }
    },
    'description': {
      bind: '__description__',
      type: 'TextArea',
      default: 'Describe this step here',
      props: { label: 'Description' },
    },
    'sql-editor': {
      bind: 'sql',
      type: 'CodeEditor',
      default: 'SELECT * FROM [YOUR TABLE]',
      props: { label: 'Limit your data with SQL query', options: { mode: 'sql' } }
    },

    'update-btn': {
      type: 'Button',
      props: { label: 'Update', event: 'SUBMIT_CONFIG' }
    },
    'variable-editor': {
      type: 'VariablePicker',
      box: { flex: false, margin: { bottom: 'medium', top: 'small' }},
      props: { exploreButton: true, testButton: false, runButton: false },
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
