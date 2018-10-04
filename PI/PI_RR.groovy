def projRepo = "${scm}"
def dev ="${dev}"
def qa = "${qa}"
def sysOps = "${sysOps}"
def agentLabel = "${agentLabel}"
def gitCred='svc-devops'
def svnCred='svn'
def app_platform= "${app_platform}"
def app_type="${app_type}"
def config_reponame="${config_reponame}"
def svn_path="${svn_path}"
def special_char = "${special_char}"

def environment = "Development"
def farms = "${list}"
def farms_list = farms.split(',')
for (farm in farms_list) {
def app_farm="${farm}"

/* 1st Deploy*/
job("RR_${app_farm}_1stDeploy") {
    deliveryPipelineConfiguration("RR_${app_farm}", '1stDeploy_${host_name}')
    description('FullPDV-${pdv}')
steps {

shell{
  command('''
curl -s -u "chefuser:chenisarockstar" -XPOST http://chefuser:chenisarockstar@10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d  "{\\"request_type\\" : \\"app_inventory\\",\\"environment\\" : '''+"""\\"${environment}\\\""""+''',\\"app_platform\\":'''+"""\\"${app_platform}\\\""""+''',\\"app_type\\":'''+"""\\"${app_type}\\\""""+''',\\"environment_type\\":\\"primary\\", \\"app_farm\\":'''+"""\\"${app_farm}\\\""""+'''}" |  jq '.host_info[0]' | jq .'host_name' > machine_list
cat machine_list| while read line || [[ -n "$line" ]]; do
line="${line//\\"/}"
machine_name=$line

echo $machine_name
#eval "knife node show $machine_name"
echo "host_name=$machine_name" > build.properties
eval "knife ssh 'name:$machine_name' -x chefuser  -i '~/.chef/chefuser.pem'   -a ipaddress  \\\"sudo echo '{\\\\\\"application_management\\\\\\":{\\\\\\"enable_loadbalancer\\\\\\":\\\\\\"true\\\\\\"}}' | sudo chef-client -j /dev/stdin\\\""
done;'''
)
}
      logRotator { numToKeep 5 }
      environmentVariables {
        propertiesFile('build.properties')
    }
      label("${agentLabel}")

     	  conditionalSteps {
    condition {
        stringsMatch('${pdv}', 'enabled', false)
    }
    runner('Fail')
  steps {


    downstreamParameterized {
trigger("RR_${app_farm}_FullPDV") {
parameters {
predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
}
}
}

  }
 }
 	  conditionalSteps {
    condition {
        stringsMatch('${pdv}', 'manual', false)
    }
    runner('Fail')
  steps {


    downstreamParameterized {
trigger("RR_${app_farm}_PromoteAll") {
parameters {
predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
}
}
}
  }
 }
     	  conditionalSteps {
    condition {
        stringsMatch('${pdv}', 'disabled', false)
    }
    runner('Fail')
  steps {
    downstreamParameterized {
trigger("RR_${app_farm}_PromoteAll") {
parameters {
predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
}}}  }
 }
  parameters {
      stringParam("BUILD_NUMBER","","")
	   stringParam("pdv","disabled","")
	  }
wrappers {
        buildName('${BUILD_NUMBER}')
        buildUserVars()
        colorizeOutput()
         preBuildCleanup{
       cleanupParameter()}
    }

  }
publishers {
sparkNotifyPostBuilder {
disable(false)
skipOnFailure(false)
skipOnSuccess(false)
skipOnAborted(false)
skipOnUnstable(false)
// Define the message to send to spark space To display the build result: ${BUILD_RESULT} Environment variable examples: $BUILD_URL ${BUILD_URL} ${env.BUILD_URL} Commonly used variables: ${JOB_NAME} ${JOB_URL} ${BUILD_NUMBER} ${BUILD_RESULT} ${BUILD_URL} ${BUILD_DISPLAY_NAME} ${BUILD_ID} ${BUILD_TAG} ${JAVA_HOME} ${JAVA_VERSION} ${JENKINS_URL} ${JENKINS_VERSION} ${NODE_LABELS} ${NODE_NAME} ${PATH} ${PWD} ${EXECUTOR_NUMBER} if using SCM or SCM trigger (see the corresponding plugin wiki for additional environment variables): ${BRANCH_NAME} ${CHANGE_ID} ${CHANGE_URL} ${CHANGE_TITLE} ${CHANGE_AUTHOR} ${CHANGE_AUTHOR_DISPLAY_NAME} ${CHANGE_AUTHOR_EMAIL} ${CHANGE_TARGET} if using the Environment Injector Plugin: ${BUILD_CAUSE} ${BUILD_CAUSE_MANUALTRIGGER} ${BUILD_CAUSE_SCMTRIGGER} ${BUILD_CAUSE_UPSTREAMTRIGGER} ${ROOT_BUILD_CAUSE} All environment variables injected into the build can also be used
messageContent('${JOB_NAME} - ${BUILD_RESULT} - ${JOB_URL}')
messageType("markdown")
// To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
roomList {
sparkRoom {
rName("Common")
rId("0e436070-aa12-11e8-afef-ef84edbc65b0")
}
}
credentialsId("Webexbot")
} }

}

  /* FullPDV*/
job("RR_${app_farm}_FullPDV") {
	  deliveryPipelineConfiguration("RR_${app_farm}", 'FullPDV')
  steps {
      logRotator { numToKeep 5 }

    label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"
      }

    }
   wrappers {
        buildName('${BUILD_NUMBER}')
        buildUserVars()
        colorizeOutput()
        preBuildCleanup{
       cleanupParameter()}
    }
  properties{
        promotions{
            promotion {
              name("${JOB_NAME}")
                icon('star-gold')
                conditions {
                    manual("${dev}")
                }
              actions {
                   downstreamParameterized  {
          trigger("RR_${app_farm}_PromoteAll") {
          parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }  }    }  }   }   }}

    publishers {
sparkNotifyPostBuilder {
disable(false)
skipOnFailure(false)
skipOnSuccess(false)
skipOnAborted(false)
skipOnUnstable(false)
// Define the message to send to spark space To display the build result: ${BUILD_RESULT} Environment variable examples: $BUILD_URL ${BUILD_URL} ${env.BUILD_URL} Commonly used variables: ${JOB_NAME} ${JOB_URL} ${BUILD_NUMBER} ${BUILD_RESULT} ${BUILD_URL} ${BUILD_DISPLAY_NAME} ${BUILD_ID} ${BUILD_TAG} ${JAVA_HOME} ${JAVA_VERSION} ${JENKINS_URL} ${JENKINS_VERSION} ${NODE_LABELS} ${NODE_NAME} ${PATH} ${PWD} ${EXECUTOR_NUMBER} if using SCM or SCM trigger (see the corresponding plugin wiki for additional environment variables): ${BRANCH_NAME} ${CHANGE_ID} ${CHANGE_URL} ${CHANGE_TITLE} ${CHANGE_AUTHOR} ${CHANGE_AUTHOR_DISPLAY_NAME} ${CHANGE_AUTHOR_EMAIL} ${CHANGE_TARGET} if using the Environment Injector Plugin: ${BUILD_CAUSE} ${BUILD_CAUSE_MANUALTRIGGER} ${BUILD_CAUSE_SCMTRIGGER} ${BUILD_CAUSE_UPSTREAMTRIGGER} ${ROOT_BUILD_CAUSE} All environment variables injected into the build can also be used
messageContent('${JOB_NAME} - ${BUILD_RESULT} - ${JOB_URL}')
messageType("markdown")
// To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
roomList {
sparkRoom {
rName("Common")
rId("0e436070-aa12-11e8-afef-ef84edbc65b0")
}
}
credentialsId("Webexbot")
} }
   publishers {
        wsCleanup()
    }
}
/* PromoteAll job*/
job("RR_${app_farm}_PromoteAll") {
	  deliveryPipelineConfiguration("RR_${app_farm}", 'PromoteAll')
  steps {
      logRotator { numToKeep 5 }

    label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"
      }
    }
   wrappers {
        buildName('${BUILD_NUMBER}')
        buildUserVars()
        colorizeOutput()
        preBuildCleanup{
       cleanupParameter()}
    }
  properties{
        promotions{
            promotion {
              name("${JOB_NAME}")
                icon('star-gold')
                conditions {
                    manual("${dev}")
                }
              actions {
                   downstreamParameterized  {
          trigger("RR_${app_farm}_DeployAll") {
          parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}    }  }   }   }}

    publishers {
sparkNotifyPostBuilder {
disable(false)
skipOnFailure(false)
skipOnSuccess(false)
skipOnAborted(false)
skipOnUnstable(false)
// Define the message to send to spark space To display the build result: ${BUILD_RESULT} Environment variable examples: $BUILD_URL ${BUILD_URL} ${env.BUILD_URL} Commonly used variables: ${JOB_NAME} ${JOB_URL} ${BUILD_NUMBER} ${BUILD_RESULT} ${BUILD_URL} ${BUILD_DISPLAY_NAME} ${BUILD_ID} ${BUILD_TAG} ${JAVA_HOME} ${JAVA_VERSION} ${JENKINS_URL} ${JENKINS_VERSION} ${NODE_LABELS} ${NODE_NAME} ${PATH} ${PWD} ${EXECUTOR_NUMBER} if using SCM or SCM trigger (see the corresponding plugin wiki for additional environment variables): ${BRANCH_NAME} ${CHANGE_ID} ${CHANGE_URL} ${CHANGE_TITLE} ${CHANGE_AUTHOR} ${CHANGE_AUTHOR_DISPLAY_NAME} ${CHANGE_AUTHOR_EMAIL} ${CHANGE_TARGET} if using the Environment Injector Plugin: ${BUILD_CAUSE} ${BUILD_CAUSE_MANUALTRIGGER} ${BUILD_CAUSE_SCMTRIGGER} ${BUILD_CAUSE_UPSTREAMTRIGGER} ${ROOT_BUILD_CAUSE} All environment variables injected into the build can also be used
messageContent('${JOB_NAME} - ${BUILD_RESULT} - ${JOB_URL}')
messageType("markdown")
// To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
roomList {
sparkRoom {
rName("Common")
rId("0e436070-aa12-11e8-afef-ef84edbc65b0")
}
}
credentialsId("Webexbot")
} }
   publishers {
        wsCleanup()
    }
}
/* DeployAll job*/
job("RR_${app_farm}_DeployAll") {
   deliveryPipelineConfiguration("RR_${app_farm}", 'DeployAll')
  steps {
      logRotator { numToKeep 5 }
      label("${agentLabel}")
      shell
      {
          command('''
          curl -s -u \"chefuser:chenisarockstar\" -XPOST http://chefuser:chenisarockstar@10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d '''+ """ \"{\\\"request_type\\\" : \\\"app_inventory\\\",\\\"environment\\\" : \\\"${environment}\\\",\\\"app_platform\\\":\\\"${app_platform}\\\",\\\"app_type\\\":\\\"${app_type}\\\",\\\"environment_type\\\":\\\"primary\\\", \\\"app_farm\\\":\\\"${app_farm}\\\"}\" > machine_list
         """ + ''' cat machine_list | jq '.host_info[0] | .host_name' > 1st_machine
          cat machine_list | jq '.host_info[0] | .host_ip' > 1stmachine_ip
          sed -i 's/\\"//g' 1stmachine_ip
          ip=$(<1stmachine_ip)

          cat machine_list | jq '.host_info[] | .host_name' > machines
          mch=$(<machines)
          echo "machine_list=${mch}" > env.properties

           cat machines| while read line || [[ -n "$line" ]]; do

          machine_name=$line
          echo $machine_name
            if [[ `cat 1st_machine` == $machine_name ]]; then

          echo "inside 1st loop"
  curl -s -u "chefuser:chenisarockstar" -XPOST  '''+ """ http://chefuser:chenisarockstar@10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d  \"{\\\"request_type\\\" : \\\"app_inventory\\\",\\\"environment\\\" : \\\"${environment}\\\",\\\"app_platform\\\":\\\"${app_platform}\\\",\\\"app_type\\\":\\\"${app_type}\\\",\\\"environment_type\\\":\\\"primary\\\", \\\"app_farm\\\":\\\"${app_farm}\\\"}\" """+''' |jq ".host_info[] | select(.host_name=="${machine_name}")| .instance_info[] | .app_port" > port_list
             sed -i 's/\\"//g' port_list
             machine_name="${machine_name//\\"/}"
            cat port_list| while read line || [[ -n "$line" ]]; do
     curl -sk -u "chefuser:chenisarockstar" -H "Content-Type: application/json" -X PUT '''+ """http://10.100.16.23/portal/sysops/f5_ltm_handler_api.php -d \"{\\\"environment\\\":\\\"${environment}\\\", \\\"datacenter\\\": \\\"RR\\\",\\\"request_type\\\": \\\"application\\\",\\\"network_zone\\\":\\\"DMZ\\\",\\\"action\\\": \\\"enable\\\",\\\"host_name\\\": """ + ''' \\\"${machine_name}\\\",\\\"host_ip\\\": \\\"${ip}\\\",\\\"app_port\\\": \\\"${line}\\\",''' + """ \\\"app_platform\\\": \\\"pi\\\",\\\"app_type\\\": \\\"${app_type}\\\",\\\"app_farm\\\": \\\"${app_farm}\\\","""+''' \\\"chefnode\\\": \\\"${machine_name}\\\"}\"
          echo "appport_${line}"
           done;

          else
          echo $machine_name
          eval "knife node show $machine_name"
          eval "knife ssh 'name:$machine_name' -x chefuser  -i '~/.chef/chefuser.pem'   -a ipaddress  \\\"sudo  echo '{\\\\\\"application_management\\\\\\":{\\\\\\"enable_loadbalancer\\\\\\":\\\\\\"true\\\\\\"}}' | sudo chef-client -j /dev/stdin\\\""
           fi;
           done;'''
     )
      }

  parameters {
      stringParam "BUILD_NUMBER"  }
      wrappers {
        buildName('${BUILD_NUMBER}')
        buildUserVars()
        colorizeOutput()
         preBuildCleanup{
       cleanupParameter()}
    }
        downstreamParameterized  {
          trigger("RQ_${app_type}_Generator") {
        parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}
        }
}
    publishers {
sparkNotifyPostBuilder {
disable(false)
skipOnFailure(false)
skipOnSuccess(false)
skipOnAborted(false)
skipOnUnstable(false)
// Define the message to send to spark space To display the build result: ${BUILD_RESULT} Environment variable examples: $BUILD_URL ${BUILD_URL} ${env.BUILD_URL} Commonly used variables: ${JOB_NAME} ${JOB_URL} ${BUILD_NUMBER} ${BUILD_RESULT} ${BUILD_URL} ${BUILD_DISPLAY_NAME} ${BUILD_ID} ${BUILD_TAG} ${JAVA_HOME} ${JAVA_VERSION} ${JENKINS_URL} ${JENKINS_VERSION} ${NODE_LABELS} ${NODE_NAME} ${PATH} ${PWD} ${EXECUTOR_NUMBER} if using SCM or SCM trigger (see the corresponding plugin wiki for additional environment variables): ${BRANCH_NAME} ${CHANGE_ID} ${CHANGE_URL} ${CHANGE_TITLE} ${CHANGE_AUTHOR} ${CHANGE_AUTHOR_DISPLAY_NAME} ${CHANGE_AUTHOR_EMAIL} ${CHANGE_TARGET} if using the Environment Injector Plugin: ${BUILD_CAUSE} ${BUILD_CAUSE_MANUALTRIGGER} ${BUILD_CAUSE_SCMTRIGGER} ${BUILD_CAUSE_UPSTREAMTRIGGER} ${ROOT_BUILD_CAUSE} All environment variables injected into the build can also be used
messageContent('${JOB_NAME} - ${BUILD_RESULT} - ${JOB_URL}')
messageType("markdown")
// To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
roomList {
sparkRoom {
rName("Common")
rId("0e436070-aa12-11e8-afef-ef84edbc65b0")
}
}
credentialsId("Webexbot")
} }

  publishers {
     wsCleanup()
    }
  }
}

/* Promotion job*/
job("RR_${app_type}_Promotion") {
  deliveryPipelineConfiguration("RR_${app_type}_Promotion", 'Promotion')
  steps {
      logRotator { numToKeep 5 }
        label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"
     }
     wrappers {
       buildName('${BUILD_NUMBER}')
       buildUserVars()
       colorizeOutput()
        preBuildCleanup{
      cleanupParameter()}
   }
     properties{
           promotions{
               promotion {
                   name("RR_${app_type}_Promotion")
                   icon('star-gold')
                   conditions {
                       manual("${dev}")
                     parameters {

                  for (farm in farms_list){
                     def app_farm1="${farm}"

                     app_farm1 = app_farm1.replaceAll("[\\.-]","_").replaceAll("\"","")
                     // app_farm1 = app_farm1.replaceAll("-","_")
                     stringParam("${app_farm1}", "", 'Assign priority values b/w 1 to 3')
                  }
               }
           }
        actions {
             downstreamParameterized  {
             trigger("RR_${app_type}") {
             parameters{
                 predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
               for (farm in farms_list){
                     def app_farm1="${farm}"
                     app_farm1 = app_farm1.replaceAll("[\\.-]","_")
                 predefinedProps(["$app_farm1": "\${$app_farm1}"])

               } }} }
                   String s1 = ''
                   String s2 = ''
                   String s3 = ""
                   for (farm in farms_list){
                     def app_farm1="${farm}"
                     app_farm1 = app_farm1.replaceAll("[\\.-]","_")
                 //-z "$rcopiaauditlog101" && -z "$rcopiaauditlog201"
                     s1=s1.concat(" -z \$${app_farm1} &&")
                     s2=s2.concat(" \$${app_farm1} == *[^[:digit:]]* ||")
                     s3=s3.concat("\$${app_farm1} ")
               }
                  s1 = s1.reverse().drop(2).reverse()
                  s2 = s2.reverse().drop(2).reverse()
                   shell("""echo Hello World!
   str=(${s3})

   for i in \${str[@]}
   do
     count=\$(grep -o \${i} <<< \${str[*]} | wc -l)
     echo "count: \$count"
     if [[ \$count == 2 ]]; then
        echo "Duplicate Priorities Entered.Please prioritize in sequence manner."
        exit 1
     fi
   done
   if [[ ${s1} ]];
   then
       echo "you haven't given any priorities"
       exit 1
   elif [[ ${s2} ]];
   then
       echo "your input is not a number"
       exit 1
   fi
   """)
    } }}}
    publishers {
    sparkNotifyPostBuilder {
    disable(false)
    skipOnFailure(false)
    skipOnSuccess(false)
    skipOnAborted(false)
    skipOnUnstable(false)
    // Define the message to send to spark space To display the build result: ${BUILD_RESULT} Environment variable examples: $BUILD_URL ${BUILD_URL} ${env.BUILD_URL} Commonly used variables: ${JOB_NAME} ${JOB_URL} ${BUILD_NUMBER} ${BUILD_RESULT} ${BUILD_URL} ${BUILD_DISPLAY_NAME} ${BUILD_ID} ${BUILD_TAG} ${JAVA_HOME} ${JAVA_VERSION} ${JENKINS_URL} ${JENKINS_VERSION} ${NODE_LABELS} ${NODE_NAME} ${PATH} ${PWD} ${EXECUTOR_NUMBER} if using SCM or SCM trigger (see the corresponding plugin wiki for additional environment variables): ${BRANCH_NAME} ${CHANGE_ID} ${CHANGE_URL} ${CHANGE_TITLE} ${CHANGE_AUTHOR} ${CHANGE_AUTHOR_DISPLAY_NAME} ${CHANGE_AUTHOR_EMAIL} ${CHANGE_TARGET} if using the Environment Injector Plugin: ${BUILD_CAUSE} ${BUILD_CAUSE_MANUALTRIGGER} ${BUILD_CAUSE_SCMTRIGGER} ${BUILD_CAUSE_UPSTREAMTRIGGER} ${ROOT_BUILD_CAUSE} All environment variables injected into the build can also be used
    messageContent('${JOB_NAME} - ${BUILD_RESULT} -${BUILD_NUMBER} - ${JOB_URL}')
    messageType("markdown")
    // To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
    roomList {
    sparkRoom {
    rName("Common")
    rId("0e436070-aa12-11e8-afef-ef84edbc65b0")
    }
    }
    credentialsId("Webexbot")
    } }
  }}

  /* Runner job*/
job("RR_${app_type}") {
 deliveryPipelineConfiguration("RR_${app_type}_Promotion", 'Runner')
steps {
      logRotator { numToKeep 5 }
        label("${agentLabel}")
      wrappers {
        buildName('${BUILD_NUMBER}')
        buildUserVars()
        colorizeOutput()
         preBuildCleanup{
       cleanupParameter()}
    }
  parameters {
    stringParam "BUILD_NUMBER"
    fl = farms.replaceAll("\",\""," ")
    fl = fl.replaceAll("\"","").replaceAll("[\\.-]","_")
    //fl = fl.replaceAll("-","_")
    stringParam "farms_list","${fl}"
  }

  shell{
 	command('''
 	IFS=,
farm=($farms_list)
 cat /dev/null > envVars.properties
 #j=1
 len=${#farm[@]}
 for (( i=1; i<=${len}; i++ ));
 do
   declare st${i}_priority=''
 done

 for i in ${farm[@]}
 do
   echo $i
   priority=$(eval echo \\${${i}})
   echo $priority
   if [ ! -z "$priority" ]; then
     i="${i//_/'''+"${special_char}"+'''}"
     echo $i > $i
     declare st${priority}_priority=$i
     echo st${priority}_priority=$(echo $i) >> envVars.properties
   fi
 done

 for ((i=1;i<=${len};i++));
  do
    priority=$(eval echo \\${st${i}_priority})
    if [ -z $priority ]; then
     echo st${i}_priority='dummy' >> envVars.properties
   fi
 done
''')
     }

    environmentVariables {
        propertiesFile('envVars.properties')

    }
i=0
    for (farm in farms_list){
      i=i+1
      st="\${st${i}_priority}"
      app_farm1 = farm.replaceAll("\"","")
      app_farm_sc = app_farm1.replaceAll("[\\.-]","_")
      steps {
      parameters {
        stringParam ("${app_farm_sc}","", "")
      }
      }

       conditionalSteps {
    condition {

      fileExists("${st}", BaseDir.WORKSPACE)
    }
    runner('Fail')
    steps {
      downstreamParameterized {
                  // A comma separated list of projects to build
          trigger("RR_${st}_1stDeploy") {
              block {
                buildStepFailure('never')
                failure('never')
                unstable('never')
              }
            parameters {
              predefinedProp('BUILD_NUMBER', '$BUILD_NUMBER')
            }
          }
        }
      }
 }
  conditionalSteps {
    condition {
      // Does not run the build steps.
      neverRun()
    }
    runner('Fail')
    steps {
      downstreamParameterized {
                  // A comma separated list of projects to build
          trigger("RR_${app_farm1}_1stDeploy") {
              block {
                buildStepFailure('never')
                failure('never')
                unstable('never')
              }
            parameters {
              predefinedProp('BUILD_NUMBER', '$BUILD_NUMBER')
            }
          }
        }
      }
 }
  }
// Define the message to send to spark space To display the build result: ${BUILD_RESULT} Environment variable examples: $BUILD_URL ${BUILD_URL} ${env.BUILD_URL} Commonly used variables: ${JOB_NAME} ${JOB_URL} ${BUILD_NUMBER} ${BUILD_RESULT} ${BUILD_URL} ${BUILD_DISPLAY_NAME} ${BUILD_ID} ${BUILD_TAG} ${JAVA_HOME} ${JAVA_VERSION} ${JENKINS_URL} ${JENKINS_VERSION} ${NODE_LABELS} ${NODE_NAME} ${PATH} ${PWD} ${EXECUTOR_NUMBER} if using SCM or SCM trigger (see the corresponding plugin wiki for additional environment variables): ${BRANCH_NAME} ${CHANGE_ID} ${CHANGE_URL} ${CHANGE_TITLE} ${CHANGE_AUTHOR} ${CHANGE_AUTHOR_DISPLAY_NAME} ${CHANGE_AUTHOR_EMAIL} ${CHANGE_TARGET} if using the Environment Injector Plugin: ${BUILD_CAUSE} ${BUILD_CAUSE_MANUALTRIGGER} ${BUILD_CAUSE_SCMTRIGGER} ${BUILD_CAUSE_UPSTREAMTRIGGER} ${ROOT_BUILD_CAUSE} All environment variables injected into the build can also be used
publishers {
sparkNotifyPostBuilder {
disable(false)
skipOnFailure(false)
skipOnSuccess(false)
skipOnAborted(false)
skipOnUnstable(false)
// Define the message to send to spark space To display the build result: ${BUILD_RESULT} Environment variable examples: $BUILD_URL ${BUILD_URL} ${env.BUILD_URL} Commonly used variables: ${JOB_NAME} ${JOB_URL} ${BUILD_NUMBER} ${BUILD_RESULT} ${BUILD_URL} ${BUILD_DISPLAY_NAME} ${BUILD_ID} ${BUILD_TAG} ${JAVA_HOME} ${JAVA_VERSION} ${JENKINS_URL} ${JENKINS_VERSION} ${NODE_LABELS} ${NODE_NAME} ${PATH} ${PWD} ${EXECUTOR_NUMBER} if using SCM or SCM trigger (see the corresponding plugin wiki for additional environment variables): ${BRANCH_NAME} ${CHANGE_ID} ${CHANGE_URL} ${CHANGE_TITLE} ${CHANGE_AUTHOR} ${CHANGE_AUTHOR_DISPLAY_NAME} ${CHANGE_AUTHOR_EMAIL} ${CHANGE_TARGET} if using the Environment Injector Plugin: ${BUILD_CAUSE} ${BUILD_CAUSE_MANUALTRIGGER} ${BUILD_CAUSE_SCMTRIGGER} ${BUILD_CAUSE_UPSTREAMTRIGGER} ${ROOT_BUILD_CAUSE} All environment variables injected into the build can also be used
messageContent('${JOB_NAME} - ${BUILD_RESULT} -${BUILD_NUMBER} - ${JOB_URL}')
messageType("markdown")
// To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
roomList {
sparkRoom {
rName("Common")
rId("0e436070-aa12-11e8-afef-ef84edbc65b0")
}
}
credentialsId("Webexbot")
} }

}
}
