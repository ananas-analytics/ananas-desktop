---
id: version-0.8.0-project-overview
title: Ananas Project Structure
original_id: project-overview
---

## Ananas Workspace

All your projects created from Ananas Analytics Desktop and settings could be found in **Ananas Workspace**: 

### MacOS

`/Library/Application Support/AnanasAnalytics/`

### Windows

`[User Home]/AppData/AnanasAnalytics/`

### Linux

`~/.config/AnanasAnalytics/`

You can create an Ananas Project manually to any location, and import it to Ananas Desktop from the GUI. The imported project is **NOT** copied to Ananas Workspace. 



## Ananas Workspace Directory Structure

When you create a new project in Ananas Desktop, the workspace should contain a folder for any created project with its generated ID and a structure similar to:

```bash
${Ananas Workspace}
├── {project_id_1}/
├── {project_id_2}/
├── {project_id_3}/
├── {project_id_4}/
├── workspace.yml
├── engine.yml
├── ...
```

`workspace.yml` file is where project metadata and workspace settings are defined.

`engine.yml` is where to find the global execution engine settings.

Each project has its own directory with project id as the directory name.

## Ananas Project Directory Structure

An Ananas Project has the following structure:

```bash
${Ananas Project}
├── ananas.yml
├── README.md
├── layout.yml
├── profile_dev.yml
├── profile_prod.yml
├── ...
```

The only mandatory file is `ananas.yml`, in which it defines:

- steps, their settings, and optionally step output schemas
- connections among steps
- variable definitions

`README.md` is an optional markdown documentation for the project

`layout.yml` is the generated graph layout. Usually, you won't need to create or edit this file by your own. Ananas Desktop GUI Application will create it when first time loading your project.

`profile_xx.yml` is the execution profile file, which contains the execution engine settings and default variable values used for job execution. The typical usage is creating different profile file for different environments.

You can attach any other files in your project, for example, example data source file. 

> You can reference the files in your project with predefined variable **PROJECT_PATH**
