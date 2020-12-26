import React from 'react'
import PropTypes from 'prop-types'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import AppSideBarMenuItem from './AppSideBarMenuItem'

import { Workshop, Code, Services, SchedulePlay, SettingsOption, Cubes } from 'grommet-icons'

const AppSideBarMenu = ({ activeIndex, expand,
  onClickMenu }) => {
  return (
    <Box flex>

      <AppSideBarMenuItem active={activeIndex === 0} expand={expand}
        onClick={() => onClickMenu(0)} >
        { expand ? <Text>Analysis Board</Text> : <Workshop size='20px' /> }
      </AppSideBarMenuItem>

      <AppSideBarMenuItem active={activeIndex === 1} expand={expand}
        onClick={() => onClickMenu(1)} >
        { expand ? <Text>Execution Engine</Text> : <Services size='20px' /> }
      </AppSideBarMenuItem>

      <AppSideBarMenuItem active={activeIndex === 2} expand={expand}
        onClick={() => onClickMenu(2)} >
        { expand ? <Text>Variables</Text> : <Code size='20px' /> }
      </AppSideBarMenuItem>

      {/*
      <AppSideBarMenuItem active={activeIndex === 3} expand={expand}
        onClick={() => onClickMenu(3)} >
        { expand ? <Text>Extensions</Text> : <Cubes size='20px' /> }
      </AppSideBarMenuItem>

      <AppSideBarMenuItem active={activeIndex === 4} expand={expand}
        onClick={() => onClickMenu(4)} >
        { expand ? <Text>Settings</Text> : <SettingsOption size='20px' /> }
      </AppSideBarMenuItem>
      */}




      {/*
      <AppSideBarMenuItem active={activeIndex === 3} expand={expand}
        onClick={() => onClickMenu(3)} >
        { expand ? <Text>Automation</Text> : <SchedulePlay size='20px' /> }
      </AppSideBarMenuItem>
      */}

      {/*
      <AppSideBarMenuItem active={activeIndex === 3} expand={expand}
        onClick={() => onClickMenu(3)} >
        { expand ? <Text>Project Settings</Text> : <SettingsOption size='20px' /> }
      </AppSideBarMenuItem>
      */}

    </Box>
  )
}

AppSideBarMenu.propTypes = {
  activeIndex: PropTypes.number,

  onClickMenu: PropTypes.func,
}

AppSideBarMenu.defaultProps = {
  activeIndex: 0,

  onClickMenu: () => {},
}

export default AppSideBarMenu
