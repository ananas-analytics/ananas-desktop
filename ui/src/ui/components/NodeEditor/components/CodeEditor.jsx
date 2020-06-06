import React, { useState } from 'react'
import styled from 'styled-components'

import CodeMirror from 'react-codemirror'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'
import { Text } from 'grommet/components/Text'
import { Layer } from 'grommet/components/Layer'

import { Expand, Contract } from 'grommet-icons'

import '../../../../../node_modules/codemirror/lib/codemirror.css'
// import './CodeEditor.scss'

import 'codemirror/mode/javascript/javascript'
import 'codemirror/mode/sql/sql'

const StyledCodeMirror = styled(CodeMirror)`
  .cm-keyword {
    color: ${props => props.theme.global.colors['brandDark']} !important;
  }
`
const FullScreenStyledCodeMirror = styled(StyledCodeMirror)`
  .ReactCodeMirror, .CodeMirror {
    border: 1px solid #eee;
    /* Firefox */
    height: -moz-calc(100vh - 50px);
    /* WebKit */
    height: -webkit-calc(100vh - 50px);
    /* Opera */
    height: -o-calc(100vh - 50px);
    /* Standard */
    height: calc(100vh - 50px);
  }
`

const ExpandButton = styled(Button)`
  &:hover {
    background: ${props => props.theme.global.colors['light-2']}
  }

  cursor: pointer;
  position: absolute;
  top: -36px;
  right: 0px;
`

export default ({label=null, value='', mode='sql', readOnly=false, tabSize=2, onChange}) => {
  const [ code, setCode ] = useState(value)
  const [ fullScreen, setFullScreen ] = useState(false)

  function handleCodeChange(text) {
    setCode(text)
    onChange(text)
  }

  return (
    <Box flex={false} direction='column'>
      <Box margin={{bottom: 'small'}}>
      { label ? <Text size='small'>{label}</Text> : null}
      </Box>
      { !fullScreen ?
          (<Box style={{fontSize: '1.2rem', fontWeight: '600', position: 'relative'}}>
            <StyledCodeMirror autoFocus={true} value={code} options={{
                lineNumbers: true,
                readOnly,
                mode,
                tabSize,
              }}
              onChange={text=>handleCodeChange(text)}
            />
            <ExpandButton icon={<Expand size='small'/>}
              hoverIndicator
              onClick={() => {setFullScreen(true)}}
            />
          </Box>)
          :
          (<Layer position="left"
            full="vertical"
            animation="fadeIn">
            <Box width='xxlarge' flex direction='column' margin={{top: 'small'}}>
              <Box margin={{left: 'small', bottom: 'small'}}>
                { label ? <Text size='small'>{label}</Text> : null}
              </Box>
              <Box fill style={{fontSize: '1.2rem', fontWeight: '600', position: 'relative'}}>
                <Box fill flex={false} style={{position: 'relative'}}>
                  <FullScreenStyledCodeMirror autoFocus={true} value={code} options={{
                      lineNumbers: true,
                      readOnly,
                      mode,
                      tabSize,
                      fullScreen: true
                    }}
                    onChange={text=>handleCodeChange(text)}
                  />
                </Box>
                <ExpandButton icon={<Contract size='small'/>}
                  hoverIndicator
                  onClick={() => {setFullScreen(false)}}
                />
              </Box>
            </Box>
          </Layer>)
      }
    </Box>
  )
}
