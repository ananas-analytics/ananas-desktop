// @flow
const ua = require('universal-analytics')
const { machineIdSync } = require('node-machine-id')

const log = require('../log')

let uid = machineIdSync({original: true})

log.info('user id:', uid)

// #if process.env.NODE_ENV === 'production'
let visitor = ua('UA-143770934-1', uid, {strictCidFormat: false})

function trackEvent(category :string, action :string, label :string, value :number) {
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

module.exports = { 
  uid,
  uhash: strHash(uid),
  trackEvent 
}
// #endif

// #if process.env.NODE_ENV !== 'production'
module.exports = {
  uid: 'dev-user',
  uhash: 0,
  trackEvent: (... args: any) => {
    log.info('dev mode, tracker mock:', JSON.stringify(args))
  },
}
// #endif
