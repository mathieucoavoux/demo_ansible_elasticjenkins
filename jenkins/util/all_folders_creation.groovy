@Grab('org.yaml:snakeyaml:1.19')

/**
 * This groovy script is used to create Jenkins folders with Job DSL
 * It will read a file under the directory ''PROJECT_DIRECTORY''/jenkins with the name structure.yml
 * The variable PROJECT_DIRECTORY can either be a global variable or a job parameter
 *
 * CAUTION: if you insert credentials in your folder this job will remove them.
 *
 * @author: Mathieu COAVOUX
**/


import org.yaml.snakeyaml.Yaml


def getconfig(CONFIG_FILE) {
    Yaml parser = new Yaml()
    LinkedHashMap example = parser.load((CONFIG_FILE as File).text)
    return example
}

def jenkins_structure_file_name = PROJECT_DIRECTORY+'/jenkins/structure.yml'
def jenkins_structure_fd = new File(jenkins_structure_file_name)
if(jenkins_structure_fd.exists()) {
    structure_config = getconfig(jenkins_structure_file_name)
    structure_config.folders.each {
        def folderDescription = it.description
        folder(it.name) {
            description(folderDescription)
        }
    }
}