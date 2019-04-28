import React from 'react'

import CodeMirror from 'react-codemirror'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'

import '../../../../../node_modules/codemirror/lib/codemirror.css'
import './CodeEditor.scss'

import 'codemirror/mode/javascript/javascript'
import 'codemirror/mode/sql/sql'

export default ({label=null, value='', mode='sql', readOnly=false, tabSize=2, onChange}) => {
  return (
    <Box flex={false} direction='column'>
      <Box margin={{bottom: 'small'}}>
      { label ? <Text size='small'>{label}</Text> : null}
      </Box>
      <Box style={{fontSize: '1.2rem', fontWeight: '600'}}>
        <CodeMirror autoFocus={true} value={value} options={{
            lineNumbers: true,
            readOnly,
            mode,
            tabSize,
          }} 
          onChange={text=>onChange(text)} 
        />
      </Box>
    </Box>
  )
}
