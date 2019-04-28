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
                  'path-heading',
                  {
                    key: 'file-input',
                    tabContainer: true,
                    props: { label: 'File Path', tabs: ['Drag & Drop', 'Text Input'] },
                    children: [
                      'drop-file',
                      'path-input'
                    ]
                  },
                  {
                    key: 'advanced-editor',
                    collapsible: true,
                    props: { label: 'Advanced' },
                    children: [
                      'description',
                      'sheet',
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
      default: 'CSV Source',
      props: { label: 'Title' }
    },
    'path-heading': {
      type: 'Heading',
      box: { flex: false },
      props: { text: 'File Path', level: 5 },
    },
    'drop-file': {
      bind: 'path',
      type: 'DropFile',
      default: '',
      props: {  }
    },
    'path-input': {
      bind: 'path',
      type: 'TextInput',
      default: '',
      props: { }
    },
    'sheet': {
      bind: 'sheetname',
      type: 'TextInput',
      default: '',
      props: { label: 'Sheet' }
    },
    'description': {
      bind: '__description__',
      type: 'TextArea',
      default: 'Describe this step here',
      props: { label: 'Description' },
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
