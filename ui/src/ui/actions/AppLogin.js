import actions from './types'

function login(user) {
  return {
    type: actions.LOGIN,
    user,
  }
}

export default {
  login,
}
