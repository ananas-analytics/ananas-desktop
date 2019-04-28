import React from 'react'

import { Box } from 'grommet/components/Box'
import { CheckBox } from 'grommet/components/CheckBox'

export default ({ label, value, onChange }) => {
  let checkBoxValue = value ? true : false
  return (<Box margin={{vertical: 'small'}}>
    <CheckBox 
      label={label}
      checked={checkBoxValue}
      onChange={(e)=>{onChange(e.target.checked)}} 
    />
  </Box>)
}
