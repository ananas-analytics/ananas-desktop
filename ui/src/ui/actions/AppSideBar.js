import actions from './types'

function clickAppMenu(index) {
  return {
    type: actions.CLICK_APP_MENU,
    index,
  }
}

function toggleAppSideBar() {
  return {
    type: actions.TOGGLE_APP_SIDEBAR,
  }
}

export default {
  clickAppMenu,
  toggleAppSideBar,
}
