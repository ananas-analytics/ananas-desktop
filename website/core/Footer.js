/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

class Footer extends React.Component {
  docUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    const docsUrl = this.props.config.docsUrl;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    return `${baseUrl}${docsPart}${langPart}${doc}`;
  }

  pageUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    return baseUrl + (language ? `${language}/` : '') + doc;
  }

  render() {
    return (
      <footer className="nav-footer" id="footer">
        <section className="sitemap">
          <a href={this.props.config.baseUrl} className="nav-home">
            {this.props.config.footerIcon && (
              <img
                src={this.props.config.baseUrl + this.props.config.footerIcon}
                alt={this.props.config.title}
                width="66"
                height="82"
              />
            )}
          </a>
          <div>
            <h5>Docs</h5>
            <a href={this.docUrl('user-guide/overview')}>
              Getting Started
            </a>
            <a href={this.docUrl('user-guide/getting-started')}>
              User Guides
            </a>
            <a href={this.docUrl('developer-guide/overview')}>
              Developer Guides
            </a>
          </div>
          <div>
            <h5>Contact</h5>
            {/*
            <a href={this.pageUrl('users.html', this.props.language)}>
              User Showcase
            </a>
            <a
              href="http://stackoverflow.com/questions/tagged/"
              target="_blank"
              rel="noreferrer noopener">
              Stack Overflow
            </a>
            */}

            <a href="mailto:contact@ananasanalytics.com">
              Email
            </a>
            <a href="https://join.slack.com/t/ananas-analytics/shared_invite/enQtNTY2ODAxMDE5NzgxLTM1NGZhNzc3ZWY3MDg3MjQ1NTgxNGExYTgyOTNiNTRhMTA2N2RkNDFjOTNhYjEwODY4YWQ3NmZmYjM1NTY2ZWY"
              target="_blank"
              rel="noreferrer noopener">
              Slack Channel</a>
            <a
              href="https://twitter.com/bhoustudio"
              target="_blank"
              rel="noreferrer noopener">
              Twitter
            </a>
          </div>
          <div>
            <h5>Social</h5>
            <a href={`${this.props.config.baseUrl}blog`}>Development Blog</a>
            <a href="https://github.com/ananas-analytics/ananas-desktop">GitHub</a>
            <a
              className="github-button"
              href={this.props.config.repoUrl}
              data-icon="octicon-star"
              data-count-href="/facebook/docusaurus/stargazers"
              data-show-count="true"
              data-count-aria-label="# stargazers on GitHub"
              aria-label="Star this project on GitHub">
              Star
            </a>
          </div>
        </section>

        {/*<a
          href="https://opensource.facebook.com/"
          target="_blank"
          rel="noreferrer noopener"
          className="fbOpenSource">
          <img
            src={`${this.props.config.baseUrl}img/oss_logo.png`}
            alt="Facebook Open Source"
            width="170"
            height="45"
          />
        </a>*/}

        <section className="copyright">{this.props.config.copyright}</section>
      </footer>
    );
  }
}

module.exports = Footer;
