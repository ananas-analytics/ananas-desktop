import React from 'react'
import styled from 'styled-components'

import CodeMirror from 'react-codemirror'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'

import '../../../../../node_modules/codemirror/lib/codemirror.css'

import 'codemirror/mode/javascript/javascript'

const StyledCodeMirror = styled(CodeMirror)`
  .cm-keyword {
    color: ${props => props.theme.global.colors['brandDark']} !important;
  }
`

export default ({config={}, label=null, readOnly=false, tabSize=2, devMode=false, onChange}) => {
  let metaConfs = {}
  let strValue = '{}'
  if (!devMode) {
    let tmpConfig = {}    
    for (let k in config) {
      if (!k.startsWith('__') || !k.endsWith('__')) {
        tmpConfig[k] = config[k]
      } else {
        metaConfs[k] = config[k]
      }
    }
    strValue = JSON.stringify(tmpConfig, null, 2)
  } else {
    strValue = JSON.stringify(config, null, 2)
  }
  return (
    <Box flex={false} direction='column'>
      <Box margin={{bottom: 'small'}}>
      { label ? <Text size='small'>{label}</Text> : null}
      </Box>
      <Box style={{fontSize: '1.2rem', fontWeight: '600'}}>
        <StyledCodeMirror autoFocus={true} value={strValue} options={{
            lineNumbers: true,
            readOnly,
            mode: 'javascript',
            tabSize,
          }} 
          onChange={text=> {
            try {
              let newConfig = JSON.parse(text)
              if (!devMode) {
                newConfig = { ... newConfig, ... metaConfs } 
              } 
              onChange(newConfig)
            } catch (e) {
              // TODO: show error, turn this component to a stateful one
            }
          }} 
        />
      </Box>
    </Box>
  )
}
