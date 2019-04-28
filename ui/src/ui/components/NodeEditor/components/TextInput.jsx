import React from 'react'

import { Box } from 'grommet/components/Box'
import { TextInput } from 'grommet/components/TextInput'

import { Text } from 'grommet/components/Text'

export default ({label=null, type='text', value='', onChange}) => {
  let inputValue = value || ''
  return (
    <Box margin={{vertical: 'small'}}>
      { typeof label === 'string' ? <Text size='small' margin={{bottom: 'xsmall'}}>{label}</Text> : null }
      <TextInput 
        type={type}
        value={inputValue} 
        onChange={e => onChange(e.target.value)} />
    </Box>
  )
}

