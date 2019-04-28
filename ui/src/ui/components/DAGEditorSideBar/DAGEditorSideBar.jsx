import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { TextInput } from 'grommet/components/TextInput'

import { Search } from 'grommet-icons'

import DAGItem from './DAGItem'

class DAGEditorSideBar extends Component {
  
  constructor(props) {
    super(props)

    this.state = {
      filter: '',
      type: 'All',
    }
  }

  renderTabs() {
    return ['All', 'Source', 'Transform', 'Destination', 'Visualization']
    .map(type => {
      return (
        <Text key={type} size='xsmall'
          color={this.state.type === type ? 'brand' : 'dark-4'} 
          onClick={ () => this.setState({type}) }
          style={{
            cursor: 'pointer',
            fontWeight: this.state.type === type ? '600' : '300'
          }}
        >
          {type}
        </Text>)
    })  
  }

  render() {
    return (
      <Box direction='column' fill='vertical' width='300px'>
        <Box pad='small'>
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
            <TextInput type='search' plain size='small' value={this.state.filter}
              style={{padding: '2px'}}
              onChange={e => this.setState({ filter: e.target.value })}
            />
          </Box>
        </Box>
        <Box direction='row' gap='xsmall' justify='between' pad='small'
          border={
            {
              side: 'bottom',
              size: '1px',
            }
          }
        >
          { this.renderTabs() } 
        </Box>
        <Box fill='vertical' overflow={{vertical: 'auto'}} >
          <Box direction='column'>
          {
            this.props.items
              .filter(item => {
                if (this.state.type !== 'All' && this.state.type !== '' && item.type !== this.state.type) {
                  return false
                }

                if (!this.state.filter || this.state.filter === '') {
                  return true
                }

                let name = item.name.toLowerCase()
                let description = item.description.toLowerCase()
                let filter = this.state.filter.toLowerCase()

                let nameMatch = name.includes(filter)  
                let descriptionMatch = description.includes(filter)

                return nameMatch || descriptionMatch
              })
              .map((item, index) => <DAGItem key={`${item.type}-${item.name}`} value={item} />)
          }
          </Box>
        </Box>
      </Box>
    )
  }
}

DAGEditorSideBar.propTypes = {
  items: PropTypes.array,
}

DAGEditorSideBar.defaultProps = {
  items: [],
}

export default DAGEditorSideBar
