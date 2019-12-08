import React from 'react'

import { Text } from 'grommet/components/Text'

export default ({ value='', level, template='', color='text', truncate=false }) => {
  let size = 'small'
  let weight = 500
  switch(level) {
    case 1:
      size = 'xxlarge'
      weight = 900 
      break
    case 2: 
      size = 'xlarge'
      weight = 800
      break
    case 3:
      size = 'large'
      weight = 700
      break
    case 4:
      size = 'medium'
      weight = 500
      break
    case 5: 
      size = 'small'
      weight = 400
      break
    case 6: 
      size = 'xsmall'
      weight = 300
      break
    default:
      size = 'medium'
      weight = 500
  }
  return (
    <Text 
      color={color}
      margin={{vertical: 'small'}}
      size={size} 
      truncate={truncate}
      weight={weight} >{template.replace(/{value}/gi, value)}</Text>
  )
}
