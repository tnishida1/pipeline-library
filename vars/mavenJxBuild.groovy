#!/bin/env groovy

def call(params) {
    if (!params) params = [:]
    def appVersion = params.get("version", "")
    sendBuildEvent(eventType: 'build')
    container('maven') {
        def pom = readMavenPom file: 'pom.xml'
        if (appVersion) {
            sh "mvn versions:set -DnewVersion=${appVersion}"
        } else {
            appVersion = pom.version.split("-")[0] + "-${BUILD_NUMBER}"
        }
        env.VERSION = appVersion
        env.APP_NAME = pom.artifactId
        env.GROUP_ID = pom.groupId

        sh "mvn clean install"
        sh "skaffold version"

        sh 'env'
        if (env.BRANCH_NAME == 'master') {
            sh "git checkout master"
            sh "git config --global credential.helper store"
            sh "jx step git credentials"
            sh "jx step tag --version ${appVersion}"
        }

        sh "skaffold build -f skaffold.yaml"
        sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:$VERSION"
        sh "echo hi jon"

        if (env.BRANCH_NAME.contains("PR")) {
            dir('charts/preview') {
                sh "make preview"
                sh "jx preview --app $APP_NAME --dir ../.."
                sh "echo takumin 1"
                env.APP_URL = sh returnStdout: true, script: 'jx get previews -c'
                sh "echo takumin 2"
            }
            echo "url to sample app is: ${env.APP_URL}"
        }
    }
}
