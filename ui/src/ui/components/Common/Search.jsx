import React from 'react'
import PropTypes from 'prop-types'

import { Box } from 'grommet/components/Box'
import { TextInput } from 'grommet/components/TextInput'

import { Search } from 'grommet-icons'

const SearchInput = ({ text, onChange }) => {
  return (<Box>
    <Box
      align='center'
      border={{
        side: 'all',
        color: 'border'
      }}
      direction='row'
      pad={{ horizontal: 'xsmall', vertical: 'xsmall' }}
      round='small'
    >
      <Box margin={{ left: '2px', right: '5px' }}>
        <Search color="brand" size='15px'/>
      </Box>
      <TextInput type='search' plain size='small' value={text}
        style={{padding: '2px'}}
        onChange={e => onChange(e.target.value)}
      />
    </Box>
  </Box>)
}

SearchInput.propTypes = {
  text: PropTypes.string,
  onChange: PropTypes.func,
}

SearchInput.defaultProps = {
  text: '',
  onChange: ()=>{}
}

export default SearchInput
