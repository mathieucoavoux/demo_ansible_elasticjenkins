pipeline {
    agent any
    stages {
        stage('Create Docker image') {
            steps {
                wrap([$class: 'ElasticJenkinsWrapper', 'stepName' : 'create_ec2_instance']) {
                    wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                        sh 'export ANSIBLE_FORCE_COLOR=true; \
                            ansible-playbook \
                            -c local \
                            -t dumb_docker \
                            $PROJECT_DIRECTORY/ansible/ec2.yml \
                            -e ENV_AUTO_CORRELATION_ID=$AUTO_CORRELATION_ID'
                    }

                }
            }
        }
        stage('Run docker') {
            steps {
                wrap([$class: 'ElasticJenkinsWrapper', 'stepName' : 'create_ec2_instance']) {
                    wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                        sh 'export ANSIBLE_FORCE_COLOR=true; \
                            ansible-playbook \
                            -c local \
                            -t dumb_deploy \
                            $PROJECT_DIRECTORY/ansible/ec2.yml \
                            -e ENV_AUTO_CORRELATION_ID=$AUTO_CORRELATION_ID'
                    }
                }
            }
        }
    }
    post {
        always {
             wrap([$class: 'ElasticJenkinsWrapper']) {
                wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                    sh 'echo Operation completed' // both wrappers applied here
                }
             }
        }
    }
}