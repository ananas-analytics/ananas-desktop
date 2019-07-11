const ua = require('universal-analytics')
const { machineIdSync } = require('node-machine-id')

const log = require('../common/log')

let uid = machineIdSync({original: true})

log.info('user id:', uid)

let visitor = ua('UA-143770934-1', uid, {strictCidFormat: false})

function trackEvent(category, action, label, value) {
  visitor
    .event({
      ec: category,
      ea: action,
      el: label,
      ev: value,
    })
    .send()
}

module.exports = { trackEvent }
