---
id: where-to-find
title: Where to find X?
---

In this page, you can find all Ananas Analytics Desktop generated folders / files.

## Where to find Ananas Workspace ?

All your projects created from Ananas Analytics Desktop and settings could be found in **Ananas Workspace**: 


### MacOS

`/Library/Application Support/AnanasAnalytics/`

### Windows

`[User Home]/AppData/AnanasAnalytics/`

### Linux

`~/.config/AnanasAnalytics/`

## Where to find my Ananas project ?

When you create a new project in Ananas Desktop, the workspace should contain a folder for any created project with its generated ID and a structure similar to:

```bash
${Ananas Workspace}
├── {project_id}
│   ├── ananas.yml
│   └── README.md
├── workspace.yml
├── engine.yml
```

`workspace.yml` file is where project metadata and workspace settings are defined

`ananas.yml` file is where the data flow steps are defined. 

`README.md` is where to find the project description.

`engine.yml` is where to find the global execution engine settings.

## Where to find Ananas logs?

### MacOS
`~/Library/Logs/AnanasAnalytics/log.log`

### Windows

`%USERPROFILE%\AppData\Roaming\AnanasAnalytics\log.log`

### Linux

`~/.config/AnanasAnalytics/log.log`


