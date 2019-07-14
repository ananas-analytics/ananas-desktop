// @flow
const ua = require('universal-analytics')
const { machineIdSync } = require('node-machine-id')

const log = require('../log')

let uid = machineIdSync({original: true})

log.info('user id:', uid)

let visitor = ua('UA-143770934-1', uid, {strictCidFormat: false})

function trackEvent(category :string, action :string, label :string, value :number) {
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
