pipeline {
    agent {
        label any
    }
    stages {
        stage('Create EC2 Instance') {
            steps {
                wrap([$class: 'ElasticJenkinsWrapper', 'stepName' : 'create_ec2_instance']) {
                    wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                        withCredentials([string(credentialsId: 'amazon_aws_access', variable: 'AWS_ACCESS_KEY_ID')]) {
                            withCredentials([string(credentialsId: 'amazon_aws_secret', variable: 'AWS_SECRET_ACCESS_KEY')]) {
                                sh 'export ANSIBLE_FORCE_COLOR=true; \
                                    ansible-playbook \
                                    -c local \
                                    -t ec2_creation \
                                    $PROJECT_DIRECTORY/ansible/ec2.yml \
                                    -e ENV_INSTANCE_TYPE=$ENV_INSTANCE_TYPE \
                                    -e ENV_VM_TYPE=$ENV_VM_TYPE \
                                    -e ENV_EC2_NUMBER=$ENV_EC2_NUMBER \
                                    -e ENV_REGION_NAME=$ENV_REGION_NAME \
                                    -e ENV_INSTANCE_TAG_NAME=$ENV_INSTANCE_TAG_NAME \
                                    -e ENV_AUTO_CORRELATION_ID=$AUTO_CORRELATION_ID'
                            }
                        }
                    }
                }
            }
        }
        stage('Tomcat installation') {
            steps {
                wrap([$class: 'ElasticJenkinsWrapper', 'stepName' : 'tomcat_installation']) {
                    wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'xterm']) {
                        withCredentials([string(credentialsId: 'amazon_aws_access', variable: 'AWS_ACCESS_KEY_ID')]) {
                            withCredentials([string(credentialsId: 'amazon_aws_secret', variable: 'AWS_SECRET_ACCESS_KEY')]) {
                                sh 'export ANSIBLE_FORCE_COLOR=true; \
                                    ansible-playbook \
                                    -c local \
                                    -t tomcatinstall \
                                    $PROJECT_DIRECTORY/ansible/tomcat.yml'
                            }
                        }
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