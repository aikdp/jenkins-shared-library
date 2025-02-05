def call(Map configMap){       //call is deafualy function
    pipeline {
        agent {
            label 'AGENT-1'
        }
        options{
            disableConcurrentBuilds()
            timeout(time:30, unit: 'MINUTES')
        }
        parameters {
            booleanParam(name: 'deploy', defaultValue: false, description: 'Select to Deploy or Not')   
        }
        environment{
            appVersion = ''
            region = 'us-east-1'
            account_id = '537124943253'
            project = configMap.get("project")
            environment = 'dev'
            component  = configMap.get("component")
        }

        stages {
            stage('Read the Version') {
                steps {
                script{      //script means groovy scripts
                    def packageJson = readJSON file: 'package.json'
                    appVersion = packageJson.version
                    echo "App version: ${appVersion}"
                }
                }
            }
            stage('Install dependencies') {
                steps {
                sh 'npm install'
                }
            }
            // stage('SonarQube analysis') {
            //     environment{
            //         scannerHome = tool 'sonar-6.0'  //sonar config
            //     }
            //     steps {
            //         // script {
            //         //     scannerHome = tool '<sonarqubeScannerInstallation>'// must match the name of an actual scanner installation directory on your Jenkins build agent
            //         // }
            //         withSonarQubeEnv('sonar-6.0') {// If you have configured more than one global server connection, you can specify its name as configured in Jenkins
            //         sh "${scannerHome}/bin/sonar-scanner"
            //         }
            //     }
            // }
            // //Setting up a pipeline pause until the quality gate is computed
            // stage("Quality Gate") {
            //     steps {
            //         timeout(time: 5, unit: 'MINUTES') {
            //             // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
            //             // true = set pipeline to UNSTABLE, false = don't
            //             waitForQualityGate abortPipeline: true
            //         }
            //     }
            // }
            stage('Build docker image') {
                steps {                         //dot means current directory
                    // withAWS(region: 'us-east-1', credentials: 'aws-creds') {
                    //     sh """
                    //     docker build -t kdprasad028/backend:${appVersion} .
                    //     docker images
                    //     """
                    // }
                    withAWS(region: 'us-east-1', credentials: 'aws-creds') {    //please ADD credentials in jenkins site
                        sh """
                        aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${account_id}.dkr.ecr.us-east-1.amazonaws.com

                        docker build -t ${account_id}.dkr.ecr.us-east-1.amazonaws.com/${project}/${environment}/${component}:${appVersion} .

                        docker images

                        docker push ${account_id}.dkr.ecr.us-east-1.amazonaws.com/${project}/${environment}/${component}:${appVersion}

                        """
                    }
                }
            }
            stage ('Deploy') {     //trigger backend-cd job
                when {
                    expression {params.deploy}
                }
                steps{
                    build job: "../${component}-cd", parameters: [      //backend-cd -->give same name your pipeline as well.
                        string(name: 'version', value: "$appVersion"),
                        string(name: 'ENVIRONMENT', value: "dev"),    //here it fix, dev.
                        ], wait: true   //means, if cd sucess then only CI will sucess. otherwise pipeline fails.
                }
            }
        }
        post {
            always{
                echo "This is ALWAYS section, always say hello"
                deleteDir()
            }

            success{
                echo "This section print when pipeline is success, I am SUCCESS"
            }

            failure{
                echo "this section failed when pipeline is FAILED. I am FAIL"
            }
        }
    }
}