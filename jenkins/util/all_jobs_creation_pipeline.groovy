@Grab('org.yaml:snakeyaml:1.19')

import org.yaml.snakeyaml.Yaml

/**
 * This job create all jobs defined in the ''PROJECT_DIRECTORY''/jenkins/jobs directory
 * Environments variables that must be set in Jenkins:
 *   - PROJECT_DIRECTORY: the base directory which contains the script
 *   - REPO_URL: the git/bitbucket url where the groovy script is located
 *   - REPO_CREDENTIAL: the credential id used to clone the repository
 *   - DEFAULT_BRANCH: the branch to clone
 *   - REPO_SUBDIR: the subdirectory name where to put the downloaded files from the repository
 *
 * This script will also add a default parameter name ''AUTO_CORRELATION_ID''. The uniq id generated will be used by jobs
 * Note that this parameter is an embedded grovvy script which requires approval to run for the first time
**/

def getconfig(CONFIG_FILE) {
    Yaml parser = new Yaml()
    LinkedHashMap example = parser.load((CONFIG_FILE as File).text)
    return example
}

def gettitle(CONFIG_FILE) {
  Yaml parser = new Yaml()
  def example = parser.load((CONFIG_FILE as File).text)
  return example.title

}


def groovy_scripts = new FileNameFinder().getFileNames(PROJECT_DIRECTORY+'/jenkins/jobs', '*.groovy' )
groovy_scripts.each {
  def file_name_array = it.split('.groovy')
  def CONFIG_FILE = file_name_array[0]+'_config.yml'
  def groovy_script_fd = new File(it)
  def SCRIPT_NAME = groovy_script_fd.getName()
  config_file_fd = new File(CONFIG_FILE)
  if(config_file_fd.exists()) {
    def LinkedHashMap listparams = getconfig(CONFIG_FILE)
    //Check if listparams.parameters length

        pipelineJob(gettitle(CONFIG_FILE)) {
            if (DISABLED_PROJECTS == "true") {
                println "Disabling project : "+gettitle(CONFIG_FILE)
                disabled(true)
            }else{
                println "Enabling project : "+gettitle(CONFIG_FILE)
                disabled(false)
            }
            if(listparams.concurrent == false) {
                concurrentBuild(false);
            }
            parameters {
                listparams.parameters.each{
                      switch(it.type) {
                            case "Choice":
                                choiceParam(it.name, it.choices, it.description);
                                break;
                            case "String":
                                stringParam(it.name,it.default_value,it.description);
                                break;
                            case "Password":
                                nonStoredPasswordParam(it.name, it.description);
                                break;
                            case "Boolean":
                                booleanParam(it.name,it.default_value, it.description);
                                break;
                            case "File":
                                fileParam(it.name,it.description);
                                break;
                            default: println "Something else";
                      }
                }
               /*
                * Add correlation_id
                */
                activeChoiceParam('AUTO_CORRELATION_ID') {
                    description('Auto-generated id')
                    choiceType('SINGLE_SELECT')
                    groovyScript {
                        script('def my_date() {return new Date().getTime()}; def my_random(){ Math.abs(new Random().nextInt() % 99999 + 1)}; return ["AUTO_"+my_date()+"_"+my_random()]')
                        fallbackScript('return ["DO_NOT_USE"]')
                    }
                }
            }
            if(listparams.triggers != null && listparams.triggers.cron != null) {
                triggers {
                  cron(listparams.triggers.cron)
                }
              }
            logRotator {
              numToKeep(10)
            }
            definition {
              cpsScm {
                    scm {
                      git {
                        remote {
                          name('Repository')
                          url(REPO_URL)
                          credentials(REPO_CREDENTIAL)

                        }
                        branch('$DEFAULT_BRANCH')
                        scriptPath(REPO_SUBDIR+'/jenkins/jobs/'+SCRIPT_NAME)
                        extensions {
                          relativeTargetDirectory(REPO_SUBDIR)
                          cloneOptions {
                            timeout(2)
                          }
                        }
                      }
                    }
                }
            }

        }


  }else{
    println "WARNING: CONFIG_FILE DOES NOT EXIST: ${CONFIG_FILE} !!!!"
  }
}

