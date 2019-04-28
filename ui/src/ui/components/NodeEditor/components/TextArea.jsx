import React from 'react'

import { Box } from 'grommet/components/Box'
import { TextArea } from 'grommet/components/TextArea'

import { Text } from 'grommet/components/Text'

export default ({label=null, value='', height=undefined, onChange}) => {
  let inputValue = value || ''
  return (
    <Box margin={{vertical: 'small'}} height={height} >
      { typeof label === 'string' ? <Text size='small'
        margin={{bottom: 'xsmall'}}>{label}</Text> : null }
      <TextArea fill
        value={inputValue}
        onChange={e => onChange(e.target.value)}/>
    </Box>
  )
}

