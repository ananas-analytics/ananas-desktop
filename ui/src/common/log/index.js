const log = require('electron-log')

log.transports.console.level = 'debug'
log.transports.file.level = 'debug'

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
