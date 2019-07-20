/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');

const MarkdownBlock = CompLibrary.MarkdownBlock; /* Used to read markdown */
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

class HomeSplash extends React.Component {
  render() {
    const {siteConfig, language = ''} = this.props;
    const {baseUrl, docsUrl} = siteConfig;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    const docUrl = doc => `${baseUrl}${docsPart}${langPart}${doc}`;

    const SplashContainer = props => (
      <div className="homeContainer" >
        <div className="homeSplashFade">
          <div className="wrapper homeWrapper">{props.children}</div>
        </div>
      </div>
    );

    const Logo = props => (
      <div className="projectLogo">
        <img src={props.img_src} alt="Project Logo" />
      </div>
    );

    const ProjectTitle = () => (
      <div>
        <h2 className="projectTitle">
          Build Analytics in minutes
          <small>Connect data from anywhere. Transform, analyze, and visualize with simple steps</small>
        </h2>
        <video autoPlay={true} loop={true} muted={true} playsInline={true}>
          <source src="videos/hero-img.webm" type="video/webm"/>
          <source src="videos/hero-img.mp4" type="video/mp4"/>
        </video>
      </div>
    );

    const PromoSection = props => (
      <div className="section promoSection">
        <div className="promoRow">
          <div className="pluginRowBlock">{props.children}</div>
        </div>
      </div>
    );

    const Button = props => (
      <div className="pluginWrapper buttonWrapper">
        <a className="button" style={{fontSize: '1.2rem', padding: '10px 20px'}} href={props.href} target={props.target}>
          {props.children}
        </a>
      </div>
    );

    return (
      <SplashContainer>
        <div className="inner" style={{marginTop: '60px'}}>
          <ProjectTitle siteConfig={siteConfig} />
          <PromoSection>
            <Button href={docUrl('user-guide/getting-started')} target='_blank'>Get Started</Button>
            <Button href='https://github.com/ananas-analytics/ananas-desktop' target='_blank'>Github Repo</Button>
          </PromoSection>
        </div>
      </SplashContainer>
    );
  }
}

class Index extends React.Component {
  render() {
    const {config: siteConfig, language = ''} = this.props;
    const {baseUrl} = siteConfig;

    const Block = props => (
      <Container
        padding={['bottom', 'top']}
        id={props.id}
        background={props.background}>
        <GridBlock
          align={props.align}
          contents={props.children}
          layout={props.layout}
        />
      </Container>
    );

    

    const Features = () => (
      <Block align="center" layout="threeColumn" background="light">
        {[
          {
            content: 'Designed for non-technical users at the first place. It keeps the technical barriers very low by providing a drag and drop interface to build both ETL and analysis.',
            image: `${baseUrl}img/landing/data-share.svg`,
            imageAlign: 'top',
            title: 'Built for Non-technical User',
          },
          {
            content: 'Make it possible to query, transform and analysis with standard SQL for not only tranditional relational database but also other data sources, for example, file.',
            image: `${baseUrl}img/landing/sql.svg`,
            imageAlign: 'top',
            title: 'Powered by SQL',
          },
          {
            content: 'Built-in analysis execution engine makes it possible to run your analysis job on your own computer without internet connection.',
            image: `${baseUrl}img/landing/disconnected.svg`,
            imageAlign: 'top',
            title: 'Offline Mode',
          },
          {
            content: 'Comes with technical tools for engineers to test, and run Ananas data projects in cloud or on premise. Share preconfigured project with non-technical users.',
            image: `${baseUrl}img/landing/networking.svg`,
            imageAlign: 'top',
            title: 'Collaborate with Engineers',
          },
          {
            content: 'Ananas Desktop has been used on production for both small ad-hoc queries and terabyte big data analysis',
            image: `${baseUrl}img/landing/database.svg`,
            imageAlign: 'top',
            title: 'Scalable',
          },
        ]}
      </Block>
    );

    const Connection = () => {
      return (
        <Block align="left" background="white">
          {[
            {
              content: '- Connect to relational database, NoSQL, File, API, and more ...\n' + 
              '- Support both structured and unstructured data\n' +
              '- Join and concatenate data from different sources',
              image: `${baseUrl}img/landing/undraw_process_e90d.svg`,
              imageAlign: 'right',
              title: 'Connect data from anywhere in any format',
            },
          ]}
        </Block>
      )
    } 

    const LessCode = () => {
      return (
        <Block align="left" background="light">
          {[
            {
              content: '- Minimal knowledge required for Simple SQL\n' + 
                '- User friendly desktop interface',
              image: `${baseUrl}img/landing/undraw_code_typing_7jnv.svg`,
              imageAlign: 'left',
              title: 'Avoid the hassle of complex SQL or Python',
            },
          ]}
        </Block>
      )
    }

    const Visualization = () => {
      return (
        <Block align="left" background="white">
          {[
            {
              content: '- Explore data with built-in charts\n' + 
                '- Easy to use chart configuration',
              image: `${baseUrl}img/landing/undraw_all_the_data_h4ki.svg`,
              imageAlign: 'right',
              title: 'Visualize with few clicks',
            },
          ]}
        </Block>
      )
    }

    const ExecutionEngine = () => {
      return (
        <Block align="left" background="light">
          {[
            {
              content: '- Spark\n' + 
                '- Flink\n' +
                '- Google Dataflow\n' + 
                '- Local Execution\n' +
                '- More coming soon',
              image: `${baseUrl}img/landing/undraw_processing_qj6a.svg`,
              imageAlign: 'left',
              title: 'Execute your job with your existing data platform',
            },
          ]}
        </Block>
      )
    }
    
    const Secure = () => {
      return (
        <Block align="left" background="white">
          {[
            {
              content: '- You own your data\n' + 
                '- Data is processed on your computer or your infrastructure\n' +
                '- No credential is stored',
              image: `${baseUrl}img/landing/undraw_security_o890.svg`,
              imageAlign: 'right',
              title: 'Your data is safe and secure',
            },
          ]}
        </Block>
      )
    }

    const SupportedSources = () => (
      <div 
        className="productShowcaseSection paddingBottom"
        style={{textAlign: 'center', marginTop: '100px'}}>
        <h2>Featured Data Sources</h2>
        <Block align="center" layout="fourColumn" background="light">
          {[
            {
              content: '',
              image: `${baseUrl}img/landing/mysql.svg`,
              imageAlign: 'top',
              title: '#### MySQL',
            },
            {
              content: '',
              image: `${baseUrl}img/landing/PostgreSQL_logo.svg`,
              imageAlign: 'top',
              title: '#### Postgres',
            },
            {
              content: '',
              image: `${baseUrl}img/landing/csv.svg`,
              imageAlign: 'top',
              title: '#### CSV File',
            },
            {
              content: '',
              image: `${baseUrl}img/landing/json-file.svg`,
              imageAlign: 'top',
              title: '#### JSON Log',
            },
            {
              content: '',
              image: `${baseUrl}img/landing/google-gcs.svg`,
              imageAlign: 'top',
              title: '#### Google Cloud Storage',
            },
            {
              content: '',
              image: `${baseUrl}img/landing/google-bigquery.svg`,
              imageAlign: 'top',
              title: '#### Google BigQuery',
            },
            {
              content: '',
              image: `${baseUrl}img/landing/google-sql.svg`,
              imageAlign: 'top',
              title: '#### Google SQL',
            },
            {
              content: '',
              image: `${baseUrl}img/landing/log.svg`,
              imageAlign: 'top',
              title: '#### Plain Text Log',
            },
          ]}
        </Block>
      </div>
    );

    const Examples = () => (
      <div
        className="productShowcaseSection paddingBottom"
        style={{textAlign: 'center'}}>
        <h2>Featured Data Sources</h2>
        <MarkdownBlock>These are features of this project</MarkdownBlock>
      </div>
    );

    return (
      <div>
        <HomeSplash siteConfig={siteConfig} language={language} />
        <div className="mainContainer">
          <Features />
          <Connection />
          <LessCode />
          <Visualization />
          <ExecutionEngine />
          <Secure />
          <SupportedSources />
        </div>
      </div>
    );
  }
}

module.exports = Index;
