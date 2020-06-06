/**
 * @jest-environment node
 */
import os from 'os'
import fs from 'fs'
const fsPromises = fs.promises
import path from 'path'
import ExtensionHelper from '../model/ExtensionHelper'

describe('extension helper tests', () => {
  test('should install extension', async () => {
    jest.setTimeout(30000)

    const prefix = `${os.tmpdir()}${path.sep}aa-test-`
    const dest = await fsPromises.mkdtemp(prefix, { encoding: 'utf-8'  })
    console.log(dest)
    let result = await ExtensionHelper.InstallExtension('ananas-extension-example', {
      version: '0.1.0',
      resolved: 'https://github.com/ananas-analytics/ananas-extension-example/releases/download/v0.1.0/ananas-extension-example-0.1.0.zip',
      checksum: null
    }, dest)

    expect(result).toBe('OK')

    const extensionFolder = path.join(dest, 'ananas-extension-example', '0.1.0')
    result = await fsPromises.access(
      extensionFolder,
      fs.constants.F_OK)
    expect(result).toBeUndefined() // extension folder exists

    result = await fsPromises.access(
      path.join(extensionFolder, 'metadata.yml'),
      fs.constants.F_OK)
    expect(result).toBeUndefined()

    result = await fsPromises.access(
      path.join(extensionFolder, 'extension.yml'),
      fs.constants.F_OK)
    expect(result).toBeUndefined()

    result = await fsPromises.access(
      path.join(extensionFolder, 'editor', 'org.ananas.extension.example.transform.sql.yml'),
      fs.constants.F_OK)
    expect(result).toBeUndefined()

    // reinstall it again
    console.log('install it again')
    const dest1 = `${os.tmpdir()}${path.sep}aa-test1`
    result = await ExtensionHelper.InstallExtension('ananas-extension-example', {
      version: '0.1.0',
      resolved: 'https://github.com/ananas-analytics/ananas-extension-example/releases/download/v0.1.0/ananas-extension-example-0.1.0.zip',
      checksum: null
    }, dest)

    expect(result).toBe('OK')
  })

  test('make sure extension installed', async () => {
    const prefix = `${os.tmpdir()}${path.sep}aa-test-`
    const projectPath = await fsPromises.mkdtemp(prefix, { encoding: 'utf-8'  })
    console.log(projectPath)
    let result = await ExtensionHelper.MakeSureExtensionInstalled(projectPath, 'ananas-extension-example', {
      version: '0.1.0',
      resolved: 'https://github.com/ananas-analytics/ananas-extension-example/releases/download/v0.1.0/ananas-extension-example-0.1.0.zip',
      checksum: null
    })

    expect(result).toBe('OK')

    result = await ExtensionHelper.MakeSureExtensionInstalled(projectPath, 'ananas-extension-example', {
      version: '0.1.0',
      resolved: 'https://github.com/ananas-analytics/ananas-extension-example/releases/download/v0.1.0/ananas-extension-example-0.1.0.zip',
      checksum: null
    })
    expect(result).toBe('OK')

    const extensionFolder = path.join(projectPath, 'metadata', 'ananas-extension-example', '0.1.0')
    result = await fsPromises.access(
      path.join(extensionFolder, 'editor', 'org.ananas.extension.example.transform.sql.yml'),
      fs.constants.F_OK)
    expect(result).toBeUndefined()

    result = await fsPromises.access(
      path.join(extensionFolder, 'metadata.yml'),
      fs.constants.F_OK)
    expect(result).toBeUndefined()


  })
})
