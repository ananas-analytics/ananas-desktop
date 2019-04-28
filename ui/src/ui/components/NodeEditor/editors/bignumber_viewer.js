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
                      'chart-title',
                      'fields',
                      'size',
                      'color',
                    ]
                  },
                  {
                    key: 'advanced-editor',
                    collapsible: true,
                    props: { label: 'Advanced' },
                    children: [
                      'description',
                      'sql-editor', 
                    ]
                  },
                  {
                    key: 'history-container',
                    collapsible: true,
                    props: { label: 'Job History', open: true },
                    children: [
                      'job-history'
                    ]
                  }
                ]

              }
            ],
          },
          {
            key: 'update-container',
            props: { 
              border: 'top',
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
          {
            key: 'result-tabs',
            tabContainer: true,
            props: { tabs: ['Big Number view', 'Table view'], flex: true, fill: 'vertical' },
            children: [
              'big-number-view',
              'data-table',
            ]
          },
        ]
      }   
    ],
  },

  components: {
    'title': {
      bind: '__name__',
      type: 'TextInput',
      default: 'SQL Transformer',
      props: { label: 'Title' },
    },
    'chart-title': {
      bind: 'title',
      type: 'TextInput',
      default: '',
      props: { label: 'Title' },
    },
    'fields': {
      bind: 'fields',
      type: 'MultipleFieldsSelector',
      props: { 
        label: 'fields', 
        maxSelections: 1,
        typeFilter: [
          'INTEGER', 'INT16', 'INT32', 'INT64', 
          'TINYINT', 'SMALLINT', 'BIGINT', 
          'FLOAT', 'DOUBLE', 'DECIMAL'
        ] 
      }
    },
    'size': {
      bind: 'size',
      type: 'SelectInput',
      default: '',
      props: { 
        label: 'Size',
        options: [
          { label: 'XSMALL', value: 'xsmall' },
          { label: 'SMALL', value: 'small' },
          { label: 'MEDIUM', value: 'medium' },
          { label: 'LARGE', value: 'large' },
          { label: 'XLARGE', value: 'xlarge' },
          { label: 'XXLARGE', value: 'xxlarge' },
          { label: 'XXXLARGE', value: '42px' },
          { label: 'XXXXLARGE', value: '52px' },
        ]
      },
    },
    'color': {
      bind: 'color',
      type: 'ColorPicker',
      default: '#000000',
      props: { label: 'Color' }
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
      default: 'SELECT * FROM PCOLLECTION',
      props: { label: 'Query data to dipslay', options: { model: 'sql' } }
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
    'data-table': {
      bind: '__dataframe__',
      type: 'DataTable',
      box: { flex: true, fill: true },
      props: { pageSize: 25 },
    },
    'big-number-view': {
      bind: '__dataframe__',
      type: 'BigNumber',
      box: { flex: true, fill: true },
      props: {},
    }

  }
}
