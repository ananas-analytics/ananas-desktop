// @flow

import fs from 'fs'
import path from 'path'
import axios from 'axios'
import unzipper from 'unzipper'
import mkdirp from 'mkdirp'

const fsPromises = fs.promises

import type { PlainExtension } from './flowtypes.js'

class ExtensionHelper {
  /**
   * Make sure all extensions are installed, if not, install it
   * An installed extension is determined by:
   * 1. [projectPath]/extensions/[extension name]/[version] folder exists
   * 2. [projectPath]/metadata/[extension name]/[version] folder exists
   *
   * 1) is not required, as for the UI, only metadata is required.
   * So this method only checks if metadata/[extension name]/[version] exists
   *
   * @param projectPath string, the path of the project
   * @param extensions {[string]:Extension}, the map of extension description, including version,
   * resolve url, and checksum
   * @return Promise<'OK'>
   */
  static async MakeSureAllExtensionsInstalled(projectPath: string, extensions: {[string]:PlainExtension})
    :Promise<'OK'> {
    let failed = []
    for (let name in extensions) {
      try {
        await ExtensionHelper.MakeSureExtensionInstalled(projectPath, name, extensions[name])
      } catch (err) {
        failed.push(name)
      }
    }
    if (failed.length > 0) {
      throw new Error(`Failed to load following extension(s): ${failed.join(',')}`)
    }
    return 'OK'
  }

  static async MakeSureExtensionInstalled(projectPath: string, name: string,
    extension: PlainExtension) :Promise<'OK'> {
    let metadataPath = path.join(projectPath, 'metadata', name, 'metadata.yml')
    // let metadataPath = path.join(projectPath, 'metadata', name, extension.version, 'metadata.yml')
    try {
      await fsPromises.access(metadataPath, fs.constants.F_OK)
    } catch (err) {
      await ExtensionHelper.InstallExtension(name, extension, path.join(projectPath, 'metadata'))
    }
    return 'OK'
  }

  /**
   * Install one extension to a specified place
   */
  static async InstallExtension(name: string, extension: PlainExtension, dest: string) :Promise<any> {
    console.log(`install extensions ${name}`)
    let unzippedFolder = path.join(dest, name)
    // $FlowFixMe
    await fsPromises.mkdir(unzippedFolder, { recursive: true })
    const response = await axios({
            method: 'get',
            url: extension.resolved,
            responseType: 'stream',
          })
    await response.data
      .pipe(unzipper.Parse())
      .on('entry', entry => {
        const fileName = entry.path
        const type = entry.type // 'Directory' or 'File'
        if (fileName.startsWith('editor/') || fileName.startsWith('resource/')
          || fileName === 'extension.yml' || fileName === 'metadata.yml') {
          const destPath = path.join(unzippedFolder, fileName)
          if (type === 'Directory') {
            mkdirp.sync(destPath)
            entry.autodrain()
          } else {
            entry.pipe(fs.createWriteStream(destPath))
          }
        } else {
          entry.autodrain()
        }
      })
      // $FlowFixMe
      .promise()

    return 'OK'
  }
}

export default ExtensionHelper
