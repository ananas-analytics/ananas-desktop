const { app, BrowserWindow, Menu } = require('electron')
const path = require('path')
const { spawn } = require('child_process')

const log = require('./src/common/log')

const { init } = require('./src/main')

const MetadataLoader = require('./src/common/model/MetadataLoader')
const EditorMetadataLoader = require('./src/common/model/EditorMetadataLoader')
const { getResourcePath } = require('./src/main/util')
const metadataResourcePath = getResourcePath('metadata')
const editorResourcePath = getResourcePath('editor')

let runner = null

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let win = null

function createWindow () {
  // Create the browser window.
  win = new BrowserWindow({ 
    width: 1440, height: 1280, 
  })
	win.maximize()

  // and load the index.html of the app.
  win.loadFile('public/index.html')

  // Open the DevTools.
  // #if process.env.NODE_ENV !== 'production'
  win.webContents.openDevTools()
  // #endif

  // Emitted when the window is closed.
  win.on('closed', () => {
    // Dereference the window object, usually you would store windows
    // in an array if your app supports multi windows, this is the time
    // when you should delete the corresponding element.
    win = null
  })

  let template = [{
    label: 'Application',
    submenu: [
      { label: 'About Application', selector: 'orderFrontStandardAboutPanel:' },
      { type: 'separator' },
      { label: 'Quit', accelerator: 'Command+Q', click: function() { app.quit() }}
    ]}, {
    label: 'Edit',
    submenu: [
      { label: 'Undo', accelerator: 'CmdOrCtrl+Z', selector: 'undo:' },
      { label: 'Redo', accelerator: 'Shift+CmdOrCtrl+Z', selector: 'redo:' },
      { type: 'separator' },
      { label: 'Cut', accelerator: 'CmdOrCtrl+X', selector: 'cut:' },
      { label: 'Copy', accelerator: 'CmdOrCtrl+C', selector: 'copy:' },
      { label: 'Paste', accelerator: 'CmdOrCtrl+V', selector: 'paste:' },
      { label: 'Select All', accelerator: 'CmdOrCtrl+A', selector: 'selectAll:' }
    ]} 
  ]

  Menu.setApplicationMenu(Menu.buildFromTemplate(template))

  // #if process.env.NODE_ENV === 'production'
  startRunner()
  // #endif
}

function getRunnerPath() {
  // #if process.env.NODE_ENV === 'production'
  switch(process.platform) {
    case 'darwin':
      return path.join(app.getAppPath(), '..', 'resources/runner/Contents/MacOS/ananas')
    case 'win32':
      return path.join(app.getAppPath(), '..', 'resources/runner/ananas')
    case 'linux':
      return path.join(app.getAppPath(), '..', 'resources/runner/ananas')
  } 
  // #endif

  // #if process.env.NODE_ENV !== 'production'
  switch(process.platform) {
    case 'darwin':
      return path.join(app.getAppPath(), 'resources/runner/Contents/MacOS/ananas')
    case 'win32':
      return path.join(app.getAppPath(), 'resources/runner/ananas.exe')
    case 'linux':
      return path.join(app.getAppPath(), 'resources/runner/ananas')
  }
  // #endif
}

function startRunner() {
  const runnerPath = getRunnerPath()  

  log.info(`runner path: ${runnerPath}`)

  runner = spawn(runnerPath)
  runner.stdout.on('data', (data) => {
    log.info(data.toString())
  })

  runner.stderr.on('data', (data) => {
    log.error(data.toString())
  })

  runner.on('exit', (code) => {
    log.info(`Child exited with code ${code}`)
  })
}


function stopRunner() {
  if (runner !== null) {
    runner.kill('SIGKILL')
  }
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
let metadata = null
app.on('ready', () => {
  MetadataLoader.getInstance().loadFromDir(metadataResourcePath)
    .then(meta => {
      metadata = meta
    })
    .then(() => {
      return EditorMetadataLoader.getInstance().loadFromDir(editorResourcePath)
    })
    .then(editors => {
      init(metadata, editors)
      createWindow()
    })
    // TODO: check updates
    .catch(err => {
      log.error(err.message, err.stack)
    })
})

// Quit when all windows are closed.
app.on('window-all-closed', () => {
  // On macOS it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
  if (process.platform !== 'darwin') {
    app.quit()
  }
  stopRunner()
})

app.on('activate', () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (win === null) {
    createWindow()
  }
})

