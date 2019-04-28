export default {
  key: 'root',
  container: true,
  props: {
    direction: 'row',
    fill: true,
  },
  components: [
    { 
      key: 'left-bar',
      container: true,
      props: {
        elevation: 'small',
        fill: 'vertical',
        width: '350px',
        style: { minWidth: '300px' },
      },
      components: [
        {
          key: 'scrollable-editor',
          container: true,
          props: {
            flex: true,
            overflow: { vertical: 'auto' },
            pad: 'small',
          },
          components: [
            { // control
              type: 'Heading',
              props: {
                level: 4,
                value: 'Edit CSV Source',
                box: { pad: 'xsmall' }
              }
            },
            {
              type: 'Label',
              props: { text: 'Platform', size: 'small', box: {pad:'xsmall'} }
            },
            {
              bind: 'platform',
              type: 'SelectInput',
              group: 'group1',
              props: {
                default: 'local',
                options: [
                  { label: 'Local File System', value: 'local' },
                  { label: 'Amazon Web Service', value: 'aws' },
                  { label: 'Google Clous Storage', value: 'gcs' },
                ],
                box: { pad: 'xsmall', margin: { bottom: 'small' } }
              }
            },
            {
              type: 'Label',
              props: { text: 'File Path', size: 'small', box: {pad:'xsmall'} }
            },
            {
              bind: 'path',
              type: 'TextInput',
              group: 'group1',
              props: {
                box: { pad: 'xsmall', margin: { bottom: 'small' } }
              }
            },
            {
              bind: 'header',
              type: 'Checkbox',
              group: 'group1',
              props: {
                default: true,
                label: 'Include Header',
                box: { pad: 'xsmall', margin: { bottom: 'small' } }
              }
            },
          ]
        },
        { // bottom submit button
          key: 'submit-button',
          container: true,
          props: {
            border: { side: 'top', size: 'xsmall', color: 'light-4' },
            direction: 'column',
            height: '50px',
            justify: 'center',
          },
          components: [
            {
              type: 'Submit', 
              props: {
                label: 'Save & Update',
                primary: true,
                box: {
                  padyy: { horizontal: 'medium', vertical: 'xsmall' },
                }
              }
            } 
          ]
        }
      ]
    },
    { // right side
      key: 'table-view',
      container: true,
      props: {
        flex: true,
        fill: true,
        pad: 'small',
        overflow: { vertical: 'scroll', horizontal: 'auto' }
      },
      components: [
        {
          type: 'Heading',
          group: 'group1',
          props: {
            level: 4,
            value: 'Explorer Data',
            box: {}
          }
        },
        {
          type: 'SimpleTable', 
          props: {
            group: 'group1'
          }
        }
      ]
    }
  ]
}


