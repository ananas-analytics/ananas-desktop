// @flow
import ua from'universal-analytics'
import { machineIdSync } from 'node-machine-id'

import log from '../log'


let uid = machineIdSync({original: true})
let uhash = 0
let trackEvent = () => {}

log.info('user id:', uid)

// #if process.env.NODE_ENV === 'production'
let visitor = ua('UA-143770934-1', uid, {strictCidFormat: false})

trackEvent = (category :string, action :string, label :string, value :number) => {
  log.info(`${category} ${action} ${label} ${value}`)
  visitor
    .event({
      ec: category,
      ea: action,
      el: label,
      ev: value,
    })
    .send()
}

function strHash(str: string) :number {
  var hash = 0, i, chr
  if (str.length === 0) return hash
  for (i = 0; i < str.length; i++) {
    chr   = str.charCodeAt(i)
    hash  = ((hash << 5) - hash) + chr
    hash |= 0 // Convert to 32bit integer
  }
  return hash
}

uid,
uhash = strHash(uid),
trackEvent
// #endif

// #if process.env.NODE_ENV !== 'production'
uid = 'dev-user',
uhash = 0,
trackEvent = (... args: any) => {
  log.info('dev mode, tracker mock:', JSON.stringify(args))
}
// #endif

export {
  uid,
  uhash,
  trackEvent,
}
