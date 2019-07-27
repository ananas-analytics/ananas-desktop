// @flow

const vcmp = require('semver-compare')
const pack = require('../../../package.json')
const axios = require('axios')

const { uhash } = require('./analytics') 
const uuidv4 = require('uuid/v4')


// checkUpdate
// { 
//    version: [binary version],
//    downloadPage: [download page url]
//    rollout: [number rollout percentage],
// }

function checkUpdate() {
  let version = pack.version || '0.0.1'

  // FIXME: this is a quick fix for linux which can't get the package version.
  // TODO: better way to handle the update on linux
  if (version === '0.0.1') {
    // remember to update this version when having new release
    version = '0.9.0'
  }

  let hash = 99
  if (uhash) {
    hash = uhash 
  }
  hash = hash % 100

  // get version list 
  return axios.get(`https://raw.githubusercontent.com/ananas-analytics/ananas-desktop/master/versions.json?n=${uuidv4()}`, {
      headers: {
        'Cache-Control': 'no-cache'
      }
    })
    .then(resp => {
      let versions = resp.data
      versions.sort((a, b) => -vcmp(a.version, b.version))

      for (let i = 0; i < versions.length; i++) {
        if (vcmp(versions[i].version, version) > 0) {
          console.log(versions[i].rollout, hash)
          if (versions[i].rollout > hash) {
            return versions[i]
          }
        } else {
          return null
        }
      }
    })
}

module.exports = {
  checkUpdate,
}
