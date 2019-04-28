import React from 'react'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'

export default (props) => {
  let cache = { ...props }
  delete cache['ee']
  delete cache['context']
  return (<Box
    height='50px'
    width='100%'
    style={{minHeight: '50px', border: '0px dashed red'}}
  >
    <Text size='xsmall'>{JSON.stringify(cache, null, 4)}</Text>
  </Box>)
} 
