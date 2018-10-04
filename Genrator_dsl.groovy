def cicdProjects = [

  [scm: 'git@git.drfirst.com:devops-se/chef-dev-pi.git',dev:'srahman,fahmed,vkumar,jsingh',qa:'srahman,fahmed,vkumar,jsingh',sysOps:'ksatpathy,bkhanal,pnguyen,hpotti',
  ,config_reponame:'',label:'slave',app_platform: 'pi',app_type:"pianalytics",svn_path:'https://svnexternal.drfirst.com/svn/deploy/pi2/properties/pi2.instance.properties/pi2-toolbar-analytics-server.instance.properties', special_char:''],

]

for (project in cicdProjects) {
def projRepo = project['scm']
def dev = project['dev']
def qa = project['qa']
def sysOps = project['sysOps']
def agentLabel = project['label']
def gitCred='svc-devops'
def svnCred='svn'
def app_platform=project['app_platform']
def app_type=project['app_type']
def config_reponame=project['config_reponame']
def svn_path=project['svn_path']
def special_char = project['special_char']

job("RR_${app_type}_Generator") {
  steps {
      logRotator { numToKeep 5 }
        label("${agentLabel}")
    deliveryPipelineConfiguration("RR_${app_type}_Promotion", 'Generator')
    //label("${agentLabel}")
    scm{ git { remote{
        url("git@git.drfirst.com:devops-se/jenkins-dsl.git")
          credentials("svc-devops")
          branches('*/master')  }  } }
          parameters {
              stringParam "BUILD_NUMBER"  }
              wrappers {
                buildName('${BUILD_NUMBER}')
                buildUserVars()
                colorizeOutput()
                 preBuildCleanup{
               cleanupParameter()}
            }
  }

   steps {
    shell{
command('''
curl -s -u "chefuser:chenisarockstar" -XPOST http://10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d  "{\\"request_type\\" : \\"farm_inventory\\", \\"environment\\" :\\"Development\\",\\"app_platform\\":'''+"""\\"${app_platform}\\\""""+''', \\"app_type\\":'''+"""\\"${app_type}\\\""""+''', \\"environment_type\\": \\"primary\\"}" |  jq '.farm_info[] | .app_farm' > farm_list
sed -i -e 's/\\\"//g' farm_list
dev_list=""
num=0
 while read line || [[ -n "$line" ]]; do
     num=$(( $num + 1 ))
     if [ "$list" = "" ]; then
      echo "1st line"
          list="$list$line"
          prm="\'$line $num\'"
      else
      echo "2nd line"
          list="$list,$line"
          prm="$prm,\'$line $num\'"
      fi;

  echo "1-$list"
done < farm_list
echo "list=$list" > build.properties
echo "prm=$prm"  >>  build.properties
#echo "dev_list=$dev_list" >> build.properties
echo "scm=''' + """$projRepo"""+'''"  >> build.properties
echo "dev=''' + """$dev"""+'''" >> build.properties
echo "qa=''' + """$qa"""+'''"  >> build.properties
echo "sysOps=''' + """$sysOps"""+'''"  >> build.properties
echo "config_reponame=''' + """$config_reponame"""+'''" >> build.properties
echo "app_platform=''' + """$app_platform"""+'''" >> build.properties
echo "app_type=''' + """$app_type"""+'''" >> build.properties
echo "svn_path=''' + """$svn_path"""+'''" >>  build.properties
echo "agentLabel=''' + """$agentLabel"""+'''" >>  build.properties
echo "special_char=''' + """$special_char"""+'''" >>  build.properties
if [ "$BUILD_NUMBER" == "" ]; then
	echo "BUILD_NUMBER=$BUILD_ID"  >>  build.properties
fi;

'''
)

    }
	environmentVariables {
        propertiesFile('build.properties')
    }

dsl {
  external('./PI/PI_RR.groovy')
            removeAction('DELETE')

}

downstreamParameterized  {
  trigger("RR_${app_type}_Promotion") {
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
messageContent('${JOB_NAME} - ${BUILD_RESULT} -${BUILD_NUMBER} -  ${BUILD_USER}- ${JOB_URL}')
messageType("markdown")
// To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
roomList {
sparkRoom {
rName("Common")
rId("0e436070-aa12-11e8-afe")
}
}
credentialsId("Webexbot")
} }
 publishers {
      wsCleanup()
  }
}

job("RQ_${app_type}_Generator") {
  steps {
      logRotator { numToKeep 5 }
        label("${agentLabel}")
    deliveryPipelineConfiguration("RQ_${app_type}_Promotion", 'Generator')
    //label("${agentLabel}")
    scm{ git { remote{
        url("git@git.drfirst.com:devops-se/jenkins-dsl.git")
          credentials("svc-devops")
          branches('*/master')  }  } }
          parameters {
              stringParam "BUILD_NUMBER"  }
              wrappers {
                buildName('${BUILD_NUMBER}')
                buildUserVars()
                colorizeOutput()
                 preBuildCleanup{
               cleanupParameter()}
            }
  }

   steps {
    shell{
command('''
curl -s -u "chefuser:chenisarockstar" -XPOST http://10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d  "{\\"request_type\\" : \\"farm_inventory\\", \\"environment\\" :\\"QA\\",\\"app_platform\\":'''+"""\\"${app_platform}\\\""""+''', \\"app_type\\":'''+"""\\"${app_type}\\\""""+''', \\"environment_type\\": \\"primary\\"}" |  jq '.farm_info[] | .app_farm' > farm_list
sed -i -e 's/\\\"//g' farm_list
dev_list=""
num=0
 while read line || [[ -n "$line" ]]; do
     num=$(( $num + 1 ))
     if echo "$line" | grep 'prod'; then
       echo "its qa-prod"
       else
     if [ "$list" = "" ]; then
           echo "1st line"
               list="$list$line"
           else
           echo "2nd line"
               list="$list,$line"
           fi;
     fi;

  echo "1-$list"
done < farm_list
echo "list=$list" > build.properties
echo "prm=$prm"  >>  build.properties
echo "scm=''' + """$projRepo"""+'''"  >> build.properties
echo "dev=''' + """$dev"""+'''" >> build.properties
echo "qa=''' + """$qa"""+'''"  >> build.properties
echo "sysOps=''' + """$sysOps"""+'''"  >> build.properties
echo "config_reponame=''' + """$config_reponame"""+'''" >> build.properties
echo "app_platform=''' + """$app_platform"""+'''" >> build.properties
echo "app_type=''' + """$app_type"""+'''" >> build.properties
echo "svn_path=''' + """$svn_path"""+'''" >>  build.properties
echo "agentLabel=''' + """$agentLabel"""+'''" >>  build.properties
echo "special_char=''' + """$special_char"""+'''" >>  build.properties
if [ "$BUILD_NUMBER" == "" ]; then
	echo "BUILD_NUMBER=$BUILD_ID"  >>  build.properties
fi;
'''
)

    }
	environmentVariables {
        propertiesFile('build.properties')
    }

dsl {
  external('./PI/PI_RQ.groovy')
            removeAction('DELETE')

}

downstreamParameterized  {
  trigger("RQ_${app_type}_Promotion") {
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
messageContent('${JOB_NAME} - ${BUILD_RESULT} -${BUILD_NUMBER} -  ${BUILD_USER}- ${JOB_URL}')
messageType("markdown")
// To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
roomList {
sparkRoom {
rName("Common")
rId("0e436070-aa12-11e8-afe")
}
}
credentialsId("Webexbot")
} }
 publishers {
      wsCleanup()
  }
}

job("DS_${app_type}_Generator") {
  steps {
      logRotator { numToKeep 5 }
        label("${agentLabel}")
    deliveryPipelineConfiguration("DS_${app_type}_Promotion", 'Generator')
    //label("${agentLabel}")
    scm{ git { remote{
        url("git@git.drfirst.com:devops-se/jenkins-dsl.git")
          credentials("svc-devops")
          branches('*/master')  }  } }
          parameters {
              stringParam "BUILD_NUMBER"  }
              wrappers {
                buildName('${BUILD_NUMBER}')
                buildUserVars()
                colorizeOutput()
                 preBuildCleanup{
               cleanupParameter()}
            }
  }

   steps {
    shell{
command('''
curl -s -u "chefuser:chenisarockstar" -XPOST http://10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d  "{\\"request_type\\" : \\"farm_inventory\\", \\"environment\\" :\\"Staging\\",\\"app_platform\\":'''+"""\\"${app_platform}\\\""""+''', \\"app_type\\":'''+"""\\"${app_type}\\\""""+''', \\"environment_type\\": \\"primary\\"}" |  jq '.farm_info[] | .app_farm' > farm_list
sed -i -e 's/\\\"//g' farm_list
dev_list=""
num=0
 while read line || [[ -n "$line" ]]; do
     num=$(( $num + 1 ))
     if [ "$list" = "" ]; then
      echo "1st line"
          list="$list$line"
          prm="\'$line $num\'"
      else
      echo "2nd line"
          list="$list,$line"
          prm="$prm,\'$line $num\'"
      fi;

  echo "1-$list"
done < farm_list
echo "list=$list" > build.properties
echo "prm=$prm"  >>  build.properties

echo "scm=''' + """$projRepo"""+'''"  >> build.properties
echo "dev=''' + """$dev"""+'''" >> build.properties
echo "qa=''' + """$qa"""+'''"  >> build.properties
echo "sysOps=''' + """$sysOps"""+'''"  >> build.properties
echo "config_reponame=''' + """$config_reponame"""+'''" >> build.properties
echo "app_platform=''' + """$app_platform"""+'''" >> build.properties
echo "app_type=''' + """$app_type"""+'''" >> build.properties
echo "svn_path=''' + """$svn_path"""+'''" >>  build.properties
echo "agentLabel=''' + """$agentLabel"""+'''" >>  build.properties
echo "special_char=''' + """$special_char"""+'''" >>  build.properties
if [ "$BUILD_NUMBER" == "" ]; then
	echo "BUILD_NUMBER=$BUILD_ID"  >>  build.properties
fi;
'''
)

    }
	environmentVariables {
        propertiesFile('build.properties')
    }

dsl {
external('./PI/PI_DS.groovy')
            removeAction('DELETE')

}

downstreamParameterized  {
  trigger("DS_${app_type}_Promotion") {
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
messageContent('${JOB_NAME} - ${BUILD_RESULT} -${BUILD_NUMBER} -  ${BUILD_USER}- ${JOB_URL}')
messageType("markdown")
// To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
roomList {
sparkRoom {
rName("Common")
rId("0e436070-aa12-11e8-afe")
}
}
credentialsId("Webexbot")
} }
 publishers {
      wsCleanup()
  }
}

job("VZ_${app_type}_Generator") {
  steps {
      logRotator { numToKeep 5 }
        label("${agentLabel}")
    deliveryPipelineConfiguration("VZ_${app_type}_Promotion", 'Generator')
    //label("${agentLabel}")
    scm{ git { remote{
        url("git@git.drfirst.com:devops-se/jenkins-dsl.git")
          credentials("svc-devops")
          branches('*/master')  }  } }
          parameters {
              stringParam "BUILD_NUMBER"  }
              wrappers {
                buildName('${BUILD_NUMBER}')
                buildUserVars()
                colorizeOutput()
                 preBuildCleanup{
               cleanupParameter()}
            }
  }

   steps {
    shell{
command('''
curl -s -u "chefuser:chenisarockstar" -XPOST http://10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d  "{\\"request_type\\" : \\"farm_inventory\\", \\"environment\\" :\\"Production\\",\\"app_platform\\":'''+"""\\"${app_platform}\\\""""+''', \\"app_type\\":'''+"""\\"${app_type}\\\""""+''', \\"environment_type\\": \\"primary\\"}" |  jq '.farm_info[] | .app_farm' > farm_list
sed -i -e 's/\\\"//g' farm_list
dev_list=""
num=0
 while read line || [[ -n "$line" ]]; do
     num=$(( $num + 1 ))
     if [ "$list" = "" ]; then
      echo "1st line"
          list="$list$line"
          prm="\'$line $num\'"
      else
      echo "2nd line"
          list="$list,$line"
          prm="$prm,\'$line $num\'"
      fi;

  echo "1-$list"
done < farm_list
echo "list=$list" > build.properties
echo "prm=$prm"  >>  build.properties
echo "scm=''' + """$projRepo"""+'''"  >> build.properties
echo "dev=''' + """$dev"""+'''" >> build.properties
echo "qa=''' + """$qa"""+'''"  >> build.properties
echo "sysOps=''' + """$sysOps"""+'''"  >> build.properties
echo "config_reponame=''' + """$config_reponame"""+'''" >> build.properties
echo "app_platform=''' + """$app_platform"""+'''" >> build.properties
echo "app_type=''' + """$app_type"""+'''" >> build.properties
echo "svn_path=''' + """$svn_path"""+'''" >>  build.properties
echo "agentLabel=''' + """$agentLabel"""+'''" >>  build.properties
echo "special_char=''' + """$special_char"""+'''" >>  build.properties
if [ "$BUILD_NUMBER" == "" ]; then
	echo "BUILD_NUMBER=$BUILD_ID"  >>  build.properties
fi;
'''
)

    }
	environmentVariables {
        propertiesFile('build.properties')
    }

dsl {
external('./PI/PI_VZ.groovy')
            removeAction('DELETE')

}

downstreamParameterized  {
  trigger("VZ_${app_type}_Promotion") {
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
messageContent('${JOB_NAME} - ${BUILD_RESULT} -${BUILD_NUMBER} -  ${BUILD_USER}- ${JOB_URL}')
messageType("markdown")
// To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
roomList {
sparkRoom {
rName("Common")
rId("0e436070-aa12-11e8-afe")
}
}
credentialsId("Webexbot")
} }
 publishers {
      wsCleanup()
  }
}

//SVN_Git job
job("RR_${app_type}_Config") {
    deliveryPipelineConfiguration("${app_type}_CI", 'Config')
    steps {
      logRotator { numToKeep 5 }
      label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"  }
       multiscm { git { remote{
         url("git@git.drfirst.com:devops-config-files-rcopia/${config_reponame}.git")
            credentials("${gitCred}")
            branches('*/master')  }
                 extensions{
                   relativeTargetDirectory('config')
                 }}

      git { remote{
         url("$projRepo")
                 credentials("${gitCred}")
            branches('*/master')  }
                 extensions{
                   relativeTargetDirectory('scm')
                 }}

           svn {     location("$svn_path") {
            credentials("$svnCred")
            directory('svn')
            depth(SvnDepth.INFINITY)
           } } } }

     wrappers {
        buildName('${BUILD_NUMBER}')
          buildUserVars()
        colorizeOutput()
       preBuildCleanup{
       cleanupParameter()}   }

  steps {
    shell{
command('''
echo "test" >envVars.properties
if ! echo "${BUILD_NUMBER}"  | grep -q '''+"""$app_type"""+''' ; then
echo "need to create the build number"
grep "version" scm/data_bags/'''+"""app_$app_type"""+'''/dev.json | head -1 > tmp
version=$(cat tmp | awk -F':' '{print $2}')
version="${version//,/}"
version="${version//\\"/}"
version="${version// /}"
grep "build" scm/data_bags/'''+"""app_$app_type"""+'''/dev.json | head -1 > build
build=$(cat build | awk -F':' '{print $2}')
  build="${build//,/}"
  build="${build//\\"/}"
  build="${build// /}"
echo "BUILD_NUMBER='''+"""$app_type"""+'''_${version}.${build}_${BUILD_ID}" >envVars.properties
else
version_no=`echo ${BUILD_NUMBER}| rev | cut -d_ -f2| rev`
   version=`echo ${version_no} | rev | cut -d"." -f2-  | rev`
  build=`echo ${version_no} | rev | cut -d'.' -f 1 | rev`

fi
cd config
git checkout master
cp -fr ../svn/* .
git status
git add .
if [ -n "$(git status --porcelain)" ]; then
  git commit -m "pushing the '''+"""$app_type"""+''' config file from svn to git and tagging with the version $version"
else
  echo 'no changes';
fi
  git tag v$version.$build --force
  git push
  git push origin v$version.$build --force
'''
)
    }
      environmentVariables {
        propertiesFile('envVars.properties')
    }
  downstreamParameterized  {
          trigger("RR_${app_type}_Generator") {

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
messageContent('${JOB_NAME} - ${BUILD_RESULT} -${BUILD_NUMBER} - By - ${BUILD_USER}- ${JOB_URL}')
messageType("markdown")
// To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
roomList {
sparkRoom {
rName("Common")
rId("0e436070-aa12-11e8-afe")
}
}
credentialsId("Webexbot")
} }

  publishers {
     wsCleanup()
    }
}
//Signoff JOB
job("RQ_${app_type}_SignOff") {
  deliveryPipelineConfiguration("RQ_${app_type}_SignOff", 'SignOff')
  steps {
      logRotator { numToKeep 5 }

     //label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"
     }
	properties{
        promotions{
            promotion {
                name("RQ_${app_type}_SignOff")
                icon('star-gold')
                conditions {
                    manual("${sysOps}") }

  actions {
                   downstreamParameterized  {
          trigger("DS_${app_type}_Generator") {
          parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
          }}    }  } }}}
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
rId("0e436070-aa12-11e8-afe")
}
}
credentialsId("Webexbot")
} }
  }}

  job("DP_${app_type}_Promotion") {
    deliveryPipelineConfiguration("DP_${app_type}", 'Promotion')
    steps {
        logRotator { numToKeep 5 }

       //label("${agentLabel}")
    parameters {
      stringParam "BUILD_NUMBER"
       }
  	properties{
          promotions{
              promotion {
                  name("DP_${app_type}_DeployAll")
                  icon('star-gold')
                  conditions {
                      manual("${sysOps}") }

    actions {
                     downstreamParameterized  {
            trigger("DP_${app_type}_DeployAll") {
            parameters{
            predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
            }}    }  } }}}
          publishers {
  sparkNotifyPostBuilder {
  disable(false)
  skipOnFailure(false)
  skipOnSuccess(false)
  skipOnAborted(false)
  skipOnUnstable(false)
  // Define the message to send to spark space To display the build result: ${BUILD_RESULT} Environment variable examples: $BUILD_URL ${BUILD_URL} ${env.BUILD_URL} Commonly used variables: ${JOB_NAME} ${JOB_URL} ${BUILD_NUMBER} ${BUILD_RESULT} ${BUILD_URL} ${BUILD_DISPLAY_NAME} ${BUILD_ID} ${BUILD_TAG} ${JAVA_HOME} ${JAVA_VERSION} ${JENKINS_URL} ${JENKINS_VERSION} ${NODE_LABELS} ${NODE_NAME} ${PATH} ${PWD} ${EXECUTOR_NUMBER} if using SCM or SCM trigger (see the corresponding plugin wiki for additional environment variables): ${BRANCH_NAME} ${CHANGE_ID} ${CHANGE_URL} ${CHANGE_TITLE} ${CHANGE_AUTHOR} ${CHANGE_AUTHOR_DISPLAY_NAME} ${CHANGE_AUTHOR_EMAIL} ${CHANGE_TARGET} if using the Environment Injector Plugin: ${BUILD_CAUSE} ${BUILD_CAUSE_MANUALTRIGGER} ${BUILD_CAUSE_SCMTRIGGER} ${BUILD_CAUSE_UPSTREAMTRIGGER} ${ROOT_BUILD_CAUSE} All environment variables injected into the build can also be used
  messageContent('${JOB_NAME} - ${BUILD_RESULT} -${BUILD_NUMBER} - By - ${BUILD_USER}- ${JOB_URL}')
  messageType("markdown")
  // To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
  roomList {
  sparkRoom {
  rName("Common")
  rId("0e436070-aa12-11e8-afe")
  }
  }
  credentialsId("Webexbot")
  } }
    }}


    job("DP_${app_type}_DeployAll") {
       deliveryPipelineConfiguration("DP_${app_type}", 'DeployAll')
      steps {
          logRotator { numToKeep 5 }
          label("${agentLabel}")
          shell
       	 {
       	     command('''
             curl -s -u "chefuser:chenisarockstar" -XPOST http://10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d  '''+""" \"{\\\"request_type\\\" : \\\"farm_inventory\\\",\\\"environment\\\" : \\\"Production\\\",\\\"app_platform\\\":\\\"${app_platform}\\\",\\\"app_type\\\":\\\"${app_type}\\\",\\\"environment_type\\\":\\\"dr\\\"}\" """ + '''|  jq '.farm_info[] | .app_farm' > farm_list
             sed -i -e 's/\"//g' farm_list
             while read line || [[ -n "$line" ]]; do
             curl -s -u "chefuser:chenisarockstar" -XPOST http://chefuser:chenisarockstar@10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d  '''+ """ \"{\\\"request_type\\\" : \\\"app_inventory\\\",\\\"environment\\\" : \\\"Production\\\",\\\"app_platform\\\":\\\"${app_platform}\\\",\\\"app_type\\\":\\\"${app_type}\\\",\\\"environment_type\\\":\\\"dr\\\", \\\"app_farm\\\":""" + '''\\\"${line}\\\"}\"  |  jq '.host_info[] | .host_name' > machine_list
             cat machine_list| while read line || [[ -n "$line" ]]; do
             line="${line//\"/}"
             machine_name=$line
echo $machine_name
eval "knife node show $machine_name"
echo "hostname=${machine_name}" > env.properties
eval "knife ssh 'name:$machine_name' -x chefuser  -i '~/.chef/chefuser.pem'   -a ipaddress  \"sudo echo '{\\\"application_management\\\":{\\\"enable_loadbalancer\\\":\\\"true\\\"}}' | sudo chef-client -j /dev/stdin\""
   done;
done < farm_list'''
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
              trigger("QAProd_${app_type}_DeployAll") {
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
    messageContent('${JOB_NAME} - Machine_list- ${machine_list} - ${BUILD_RESULT} -${BUILD_NUMBER} -Promoted By- ${BUILD_USER}- ${JOB_URL}')
    messageType("markdown")
    // To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
    roomList {
    sparkRoom {
    rName("Common")
    rId("0e436070-aa12-11e8-afe")
    }
    }
    credentialsId("Webexbot")
    } }

      publishers {
         wsCleanup()
        }
      }
      //qa-prod deploy job
job("QAProd_${app_type}_DeployAll") {
         deliveryPipelineConfiguration("QAprod_${app_type}", 'QAprod-Deploy')
        steps {
            logRotator { numToKeep 5 }
            label("${agentLabel}")
            steps {
            shell{
                command(''' version=`echo ${BUILD_NUMBER}| rev | cut -d_ -f3 | rev`
            version="\"version\":\"`echo $version`\","
            build=`echo ${BUILD_NUMBER}| rev | cut -d_ -f2 | rev`
            build="\"build\":\"`echo $build`\","
            git checkout master
            sed -i "1,/\"version.*/s/\"version.*/`echo $version`/g"  '''+ """ "data_bags/app_${app_type}/qa_prod.json" """ + '''

            sed -i "1,/\"build.*/s/\"build.*/`echo $build`/g" '''+ """ "data_bags/app_${app_type}/qa_prod.json" """ + '''

            git add .
            if [ -n "$(git status --porcelain)" ]; then
            git commit -m "promoting the version from dev to qa"
            git push origin master
            knife data bag  from file ''' + """ app_${app_type}  data_bags/app_${app_type}/qa_prod.json """+ '''
            echo "test"
            else
            echo 'no changes';
            fi'''
            )
            }
            shell{
              command('''
              curl -s -u "chefuser:chenisarockstar" -XPOST http://10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d ''' + """ \"{\\\"request_type\\\" : \\\"farm_inventory\\\",\\\"environment\\\" : \\\"QA\\\",\\\"app_platform\\\":\\\"${app_platform}\\\",\\\"app_type\\\":\\\"${app_type}\\\",\\\"environment_type\\\":\\\"primary\\\"}\" """ + ''' |  jq '.farm_info[] | .app_farm' > farm_list
  sed -i -e 's/\"//g' farm_list
   while read line || [[ -n "$line" ]]; do
  if echo "$line" | grep 'prod'; then
  curl -s -u "chefuser:chenisarockstar" -XPOST  http://chefuser:chenisarockstar@10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d '''+""" \"{\\\"request_type\\\" : \\\"app_inventory\\\",\\\"environment\\\" : \\\"QA\\\",\\\"app_platform\\\":\\\"${app_platform}\\\",\\\"app_type\\\":\\\"${app_type}\\\",\\\"environment_type\\\":\\\"primary\\\", \\\"app_farm\\\":""" + '''\\\"${line}\\\"}\"  |  jq '.host_info[] | .host_name' > machine_list
cat machine_list| while read line || [[ -n "$line" ]]; do
   line="${line//\"/}"
  machine_name=$line
  echo $machine_name
  eval "knife node show $machine_name"
  eval "knife ssh 'name:$machine_name' -x chefuser  -i '~/.chef/chefuser.pem'   -a ipaddress  \"sudo echo '{\\\"application_management\\\":{\\\"enable_loadbalancer\\\":\\\"true\\\"}}' | sudo chef-client -j /dev/stdin\""
     done;
  else
   echo "its qa machine"
  fi;
  done < farm_list
;'''
            )}

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
            trigger("Demo_${app_type}_Promotion") {
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
      messageContent('${JOB_NAME} - Machine_list- ${machine_list} - ${BUILD_RESULT} -${BUILD_NUMBER} -Promoted By- ${BUILD_USER}- ${JOB_URL}')
      messageType("markdown")
      // To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
      roomList {
      sparkRoom {
      rName("Common")
      rId("0e436070-aa12-11e8-afe")
      }
      }
      credentialsId("Webexbot")
      } }

        publishers {
           wsCleanup()
          }
        }
      }
      job("Demo_${app_type}_Promotion") {
        deliveryPipelineConfiguration("Demo_${app_type}", 'Promotion')
        steps {
            logRotator { numToKeep 5 }

           //label("${agentLabel}")
        parameters {
          stringParam "BUILD_NUMBER"
           }
      	properties{
              promotions{
                  promotion {
                      name("Demo_${app_type}_Promotion")
                      icon('star-gold')
                      conditions {
                          manual("${sysOps}") }

        actions {
                         downstreamParameterized  {
                trigger("Demo_${app_type}_DeployAll") {
                parameters{
                predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                }}    }  } }}}
              publishers {
      sparkNotifyPostBuilder {
      disable(false)
      skipOnFailure(false)
      skipOnSuccess(false)
      skipOnAborted(false)
      skipOnUnstable(false)
      // Define the message to send to spark space To display the build result: ${BUILD_RESULT} Environment variable examples: $BUILD_URL ${BUILD_URL} ${env.BUILD_URL} Commonly used variables: ${JOB_NAME} ${JOB_URL} ${BUILD_NUMBER} ${BUILD_RESULT} ${BUILD_URL} ${BUILD_DISPLAY_NAME} ${BUILD_ID} ${BUILD_TAG} ${JAVA_HOME} ${JAVA_VERSION} ${JENKINS_URL} ${JENKINS_VERSION} ${NODE_LABELS} ${NODE_NAME} ${PATH} ${PWD} ${EXECUTOR_NUMBER} if using SCM or SCM trigger (see the corresponding plugin wiki for additional environment variables): ${BRANCH_NAME} ${CHANGE_ID} ${CHANGE_URL} ${CHANGE_TITLE} ${CHANGE_AUTHOR} ${CHANGE_AUTHOR_DISPLAY_NAME} ${CHANGE_AUTHOR_EMAIL} ${CHANGE_TARGET} if using the Environment Injector Plugin: ${BUILD_CAUSE} ${BUILD_CAUSE_MANUALTRIGGER} ${BUILD_CAUSE_SCMTRIGGER} ${BUILD_CAUSE_UPSTREAMTRIGGER} ${ROOT_BUILD_CAUSE} All environment variables injected into the build can also be used
      messageContent('${JOB_NAME} - ${BUILD_RESULT} -${BUILD_NUMBER} - By - ${BUILD_USER}- ${JOB_URL}')
      messageType("markdown")
      // To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
      roomList {
      sparkRoom {
      rName("Common")
      rId("0e436070-aa12-11e8-afe")
      }
      }
      credentialsId("Webexbot")
      } }
        }}

job("Demo_${app_type}_DeployAll") {
           deliveryPipelineConfiguration("Demo_${app_type}", 'Deploy')
          steps {
              logRotator { numToKeep 5 }
              label("${agentLabel}")
              shell
             {
                 command('''
curl -s -u "chefuser:chenisarockstar" -XPOST http://10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d  '''+""" \"{\\\"request_type\\\" : \\\"farm_inventory\\\",\\\"environment\\\" : \\\"Demo\\\",\\\"app_platform\\\":\\\"${app_platform}\\\",\\\"app_type\\\":\\\"${app_type}\\\",\\\"environment_type\\\":\\\"dr\\\"}\" """ + '''|  jq '.farm_info[] | .app_farm' > farm_list
sed -i -e 's/\"//g' farm_list
while read line || [[ -n "$line" ]]; do
curl -s -u "chefuser:chenisarockstar" -XPOST http://chefuser:chenisarockstar@10.100.16.23/portal/sysops/vm_farm_inventory_api.php -d  '''+ """ \"{\\\"request_type\\\" : \\\"app_inventory\\\",\\\"environment\\\" : \\\"Demo\\\",\\\"app_platform\\\":\\\"${app_platform}\\\",\\\"app_type\\\":\\\"${app_type}\\\",\\\"environment_type\\\":\\\"dr\\\", \\\"app_farm\\\":""" + '''\\\"${line}\\\"}\"  |  jq '.host_info[] | .host_name' > machine_list
cat machine_list| while read line || [[ -n "$line" ]]; do
line="${line//\"/}"
machine_name=$line
    echo $machine_name
    eval "knife node show $machine_name"
    echo "hostname=${machine_name}" > env.properties
    eval "knife ssh 'name:$machine_name' -x chefuser  -i '~/.chef/chefuser.pem'   -a ipaddress  \"sudo echo '{\\\"application_management\\\":{\\\"enable_loadbalancer\\\":\\\"true\\\"}}' | sudo chef-client -j /dev/stdin\""
       done;
    done < farm_list'''
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
      }
            publishers {
        sparkNotifyPostBuilder {
        disable(false)
        skipOnFailure(false)
        skipOnSuccess(false)
        skipOnAborted(false)
        skipOnUnstable(false)
        // Define the message to send to spark space To display the build result: ${BUILD_RESULT} Environment variable examples: $BUILD_URL ${BUILD_URL} ${env.BUILD_URL} Commonly used variables: ${JOB_NAME} ${JOB_URL} ${BUILD_NUMBER} ${BUILD_RESULT} ${BUILD_URL} ${BUILD_DISPLAY_NAME} ${BUILD_ID} ${BUILD_TAG} ${JAVA_HOME} ${JAVA_VERSION} ${JENKINS_URL} ${JENKINS_VERSION} ${NODE_LABELS} ${NODE_NAME} ${PATH} ${PWD} ${EXECUTOR_NUMBER} if using SCM or SCM trigger (see the corresponding plugin wiki for additional environment variables): ${BRANCH_NAME} ${CHANGE_ID} ${CHANGE_URL} ${CHANGE_TITLE} ${CHANGE_AUTHOR} ${CHANGE_AUTHOR_DISPLAY_NAME} ${CHANGE_AUTHOR_EMAIL} ${CHANGE_TARGET} if using the Environment Injector Plugin: ${BUILD_CAUSE} ${BUILD_CAUSE_MANUALTRIGGER} ${BUILD_CAUSE_SCMTRIGGER} ${BUILD_CAUSE_UPSTREAMTRIGGER} ${ROOT_BUILD_CAUSE} All environment variables injected into the build can also be used
        messageContent('${JOB_NAME} - Machine_list- ${machine_list} - ${BUILD_RESULT} -${BUILD_NUMBER} -Promoted By- ${BUILD_USER}- ${JOB_URL}')
        messageType("markdown")
        // To get the Space ID, go to web.ciscospark.com, select the space you want to message and get the ID from the URL.
        roomList {
        sparkRoom {
        rName("Common")
        rId("0e436070-aa12-11e8-afe")
        }
        }
        credentialsId("Webexbot")
        } }

          publishers {
             wsCleanup()
            }
          }
          
queue("RR_${app_type}_Generator")
queue("RQ_${app_type}_Generator")
queue("DS_${app_type}_Generator")
queue("VZ_${app_type}_Generator")
}
