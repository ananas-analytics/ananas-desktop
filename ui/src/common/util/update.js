// @flow

const pack = require('../../../package.json')

import vcmp from 'semver-compare'
import axios from 'axios'

import { uhash } from './analytics'
import uuidv4 from 'uuid/v4'

import log from '../log'

// checkUpdate
// {
//    version: [binary version],
//    downloadPage: [download page url]
//    rollout: [number rollout percentage],
// }

export function checkUpdate() {
  let version = pack.version || '0.0.1'

  // FIXME: this is a quick fix for linux which can't get the package version.
  // TODO: better way to handle the update on linux
  if (version === '0.0.1') {
    // remember to update this version when having new release
    version = '0.10.0'
  }

  let hash = 99
  if (uhash) {
    hash = uhash
  }
  hash = hash % 100

  log.info(`detect current version ${version}`)
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
          log.info(`new version ${versions[i].version}, ${versions[i].rollout}, ${hash}`)
          if (versions[i].rollout > hash) {
            return versions[i]
          }
        } else {
          return null
        }
      }
    })
}
