---
id: version-0.8.0-use-existing-project
title: Use existing project
original_id: use-existing-project
---

This guide assumes your organization already has a Ananas project hosted on GitHub / GitLab / BitBucket / etc. If you're not sure if your project is already hosted on one of these services, or if you don't have access to the repository, you should check with your account administrator.

## Where to find your projects

All your projects created from Ananas Analytics Desktop could be found in following folder:

### MacOS

`/Library/Application Support/AnanasAnalytics/`

### Windows

`[User Home]/AppData/AnanasAnalytics/`

### Linux

`~/.config/AnanasAnalytics/`

## Open your project folder

When you first created your project, a folder is created in the config path `.config/AnanasAnalytics` with the ID of your project. At a later stage you can still open it
by clicking on the import button. The desktop application will read it for you. 

## Using git CLI

If you're using the git CLI, you'll need to set up git authentication. GitHub has [an excellent overview of how this works](https://help.github.com/en/articles/connecting-to-github-with-ssh), and a simple guide on how to quickly get set up.

Once you've set up your SSH keys for git, you can make a clone of your dbt project with:

git clone git@github.com:your-organization/your-dbt-project.git

Then you can read the project from the UI by opening 'your-dbt-project' folder.

## Verifying project folder

Along with previously existing files and directories, your `.config` folder should contain a folder for any created project with its generated ID and a structure similar to:

```bash
${HOME}/.config
├── {project_id}
│   ├── ananas.yml
│   └── README.md
```

`ananas.yml` file is where the data flow steps are defined. 

The ananas yml file structure is similar to : 

```yml
dag:
  connections: []
  nodes: []
id: the-unique-project-id
name: project name
steps: {}
variables: []
```

## Best practices

* Use a template with variables for any non technical users so that they don't have to input the postgreSQL variables or any technical configuration details. 

