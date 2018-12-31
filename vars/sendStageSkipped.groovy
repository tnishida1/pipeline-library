#!/usr/bin/env groovy
import org.Slack.Slack
import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput

def call(def Message, String s = null) {
  Slack slack = new Slack()

  Message.message.attachments.eachWithIndex { attachment, index ->
    if (attachment.text != '' && attachment.text != null){
      def n = attachment.text.replaceAll(":not_started: ", "")
      def name = n.replaceAll(": Not started", "")
      if ("${name}" == "${env.STAGE_NAME}"){
        def payload = slack.sendStageSkipped(Message, "${env.SLACK_ROOM}", name, Message.ts, index, Message.message.attachments.size(), s)
        def m = sh(returnStdout: true, script: "curl --silent -X POST -H 'Authorization: Bearer ${env.SLACK_TOKEN}' -H \"Content-Type: application/json\" --data \'${payload}\' ${env.SLACK_WEBHOOK_URL}/api/chat.update").trim() 
        def json = readJSON text: m
        Message = json
      }
    }
  }
  return Message
}

