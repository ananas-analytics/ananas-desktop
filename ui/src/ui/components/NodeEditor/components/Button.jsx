import React from 'react'

import { Button } from 'grommet/components/Button'

export default ({ label, primary=true, event='CLICK BUTTON', ee }) => {
  return (<Button 
    label={label}
    primary={primary}
    onClick={()=>{ee.emit(event)}} 
  />)
}
