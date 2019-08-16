const log = require('electron-log')


// #if process.env.NODE_ENV !== 'production'
log.transports.console.level = 'debug'
log.transports.file.level = 'debug'
// #endif

// #if process.env.NODE_ENV === 'production'
log.transports.console.level = 'info'
log.transports.file.level = 'info'
// #endif

module.exports = {
  error: (... args) => {
    log.error(... args)
  },
  warn: (... args) => {
    log.warn(... args)
  },
  info: (... args) => {
    log.info(... args)
  },
  verbose: (... args) => {
    log.verbose(... args)
  },
  debug: (... args) => {
    log.debug(... args)
  },
  silly: (... args) => {
    log.silly(... args)
  },
}
