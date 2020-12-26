//const log = require('electron-log')

import log from 'electron-log'

// #if process.env.NODE_ENV !== 'production'
log.transports.console.level = 'debug'
log.transports.file.level = 'debug'
// #endif

// #if process.env.NODE_ENV === 'production'
log.transports.console.level = 'info'
log.transports.file.level = 'info'
// #endif

function error(... args) {
  log.error(... args)
}

function warn(... args) {
  log.warn(... args)
}

function info(... args) {
  log.info(... args)
}

function verbose(... args) {
  log.verbose(... args)
}

function debug(... args) {
  log.debug(... args)
}

function silly(... args) {
  log.silly(... args)
}

export default {
  error,
  warn,
  info,
  verbose,
  debug,
  silly,
}
