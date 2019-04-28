import React from 'react'

import { Box } from 'grommet/components/Box'
import { Select } from 'grommet/components/Select'
import { Text } from 'grommet/components/Text'

export default ({ label=null, value, options=[], onChange }) => {
  let valueObj = options.find(option => option.value === value)
  if (!valueObj) {
    valueObj = undefined
  }
  return (
    <Box margin={{vertical: 'small'}}>
      { typeof label === 'string' ? <Text size='small' margin={{bottom: 'xsmall'}}>{label}</Text> : null }
      <Select
        value={valueObj}
        options={options}
        valueKey='value'
        labelKey='label'
        onChange={e=>onChange(e.value.value)}
      />
    </Box>
  )
}
