// @flow

import axios from 'axios'

import log from '../log'

import type { PlainUser } from './flowtypes.js'

export default class User {
  plainUser: PlainUser

  constructor(user: PlainUser) {
    this.plainUser = user
  }

  toPlainObject() {
    return this.plainUser
  }

  static Login(url: string, email: string, password: string) :Promise<User> {
    log.debug('login', url, email)
    return axios.post(url, {
      email,
      password
    })
    .then(res => {
      if (res.data.code !== 200) {
        throw new Error(res.message)
      }
      return new User(res.data.data)
    })
  }
}

