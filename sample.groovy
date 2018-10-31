def cicdProjects = [

   [application_platform: 'Test',application_name:'smp',scm: 'git@git.drfirst.com:devops-se/chef-dev-rcopia.git',dev:'devuda',qa:'devuda',sysOps:'devuda',label:'slave1'], 
 
]


for (project in cicdProjects) {
def application_platform = project['application_platform']
def application_name = project['application_name']
def projRepo = project['scm']
def dev = project['dev']
def qa = project['qa']
def sysOps = project['sysOps']
def agentLabel = project['label']
def gitCred='svc-devops'

job("${application_platform}_${application_name}_Dev-Deploy") {
  steps {
      logRotator { numToKeep 5 }
     label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"  }
    shell{
command('''echo hello''')
}

     environmentVariables {
        propertiesFile('envVars.properties')
    }


    scm{ git { remote{
          url("${projRepo}")
            credentials("$gitCred")
            branches('*/master')  }  } }
   downstreamParameterized  {
          trigger("${application_platform}_${application_name}_Dev-Promotion") {
        parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}
        }
  }
   wrappers {
        buildName('${BUILD_NUMBER}')
        colorizeOutput()
        timestamps()
      preBuildCleanup{
       cleanupParameter()}
    }
      publishers {
        wsCleanup()
    }
     }

       job("${application_platform}_${application_name}_Dev-Promotion") {
  steps {
      logRotator { numToKeep 5 }
     label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"
     }
    }
   wrappers {
        buildName('${BUILD_NUMBER}')
       colorizeOutput()
        timestamps()
    }
  properties{
        promotions{
            promotion {
                name("DevtoQA")
                icon('star-gold')
                conditions {
                    manual("$dev")
                }
              actions {
                   downstreamParameterized  {
          trigger("${application_platform}_${application_name}_QA-Upload") {
          parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}    }  }   }   }}
   publishers {
        wsCleanup()
    }
}

job("${application_platform}_${application_name}_QA-Upload") {
    steps {
      logRotator { numToKeep 5 }
       label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"  }

       scm{ git { remote{
          url("${projRepo}")
            credentials("$gitCred")
            branches('*/master')  }  } }

}
     wrappers {
        buildName('${BUILD_NUMBER}')
        colorizeOutput()
       preBuildCleanup{
       cleanupParameter()}

   }

  steps {
    shell{
command('''ehco hello''')

    }


        downstreamParameterized  {
          trigger("${application_platform}_${application_name}_QA-Deploy") {

        parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}

        }
}
   publishers {
        wsCleanup()
    }

}

job("${application_platform}_${application_name}_QA-Deploy") {
  steps {
      logRotator { numToKeep 5 }
    label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"  }
      shell{
      command('''echo hello''')
}


    scm{ git { remote{
          url("${projRepo}")
            credentials("$gitCred")
            branches('*/master')  }  } }


    wrappers {
        buildName('${BUILD_NUMBER}')
        colorizeOutput()
         preBuildCleanup{
       cleanupParameter()}
    }

       downstreamParameterized  {
          trigger("${application_platform}_${application_name}_QA-SignOff") {
        parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}
        }
      }
   publishers {
        wsCleanup()
    }

}

job("${application_platform}_${application_name}_QA-Signoff") {
  steps {
      logRotator { numToKeep 5 }
    label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"
      }
          steps { scm{ git { remote{
          url('git@git.drfirst.com:devops-se/chef-sysops.git')
            credentials('svc-devops')
            branches('*/master')  }  } }
    }
    }
   wrappers {
        buildName('${BUILD_NUMBER}')
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
                    manual("$qa")
                }
              actions {
                   downstreamParameterized  {
          trigger("${application_platform}_${application_name}_Staging-Promotion") {
          parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}    }  }   }   }}

   publishers {
        wsCleanup()
    }
}

job("${application_platform}_${application_name}_Staging-Promotion") {
  steps {
      logRotator { numToKeep 5 }
       label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"
      }
    }
   wrappers {
        buildName('${BUILD_NUMBER}')
        colorizeOutput()
        timestamps()
    }
  properties{
        promotions{
            promotion {
              name("${JOB_NAME}")
                icon('star-gold')
                conditions {
                    manual("$sysOps")
                }
              actions {
                   downstreamParameterized  {
          trigger("${application_platform}_${application_name}_Staging-Upload") {
          parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}    }  }   }   }}

   publishers {
        wsCleanup()
    }

}



job("${application_platform}_${application_name}_Staging-Upload") {
    steps {
      logRotator { numToKeep 5 }
         label("${agentLabel}")
  parameters {

    stringParam "BUILD_NUMBER"  }

      steps { scm{ git { remote{
          url('git@git.drfirst.com:devops-se/chef-sysops.git')
            credentials('svc-devops')
            branches('*/master')  }  } }
    }
}
     wrappers {
        buildName('${BUILD_NUMBER}')
        colorizeOutput()
         preBuildCleanup{
        cleanupParameter()}
   }

  steps {
    shell{
command('''ehco hello''')

    }
    downstreamParameterized  {
          trigger("${application_platform}_${application_name}_Prod-Promotion") {

        parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}

        }
      }

   publishers {
        wsCleanup()
    }
}


job("${application_platform}_${application_name}_Prod-Promotion") {
  steps {
      logRotator { numToKeep 5 }
       label("${agentLabel}")
  parameters {
    stringParam "BUILD_NUMBER"
      }
    }
   wrappers {
        buildName('${BUILD_NUMBER}')
        colorizeOutput()
        timestamps()
        preBuildCleanup{
       cleanupParameter()}
    }
  properties{
        promotions{
            promotion {
              name("${JOB_NAME}")
                icon('star-gold')
                conditions {
                    manual("$sysOps")
                }
              actions {
                   downstreamParameterized  {
          trigger("${application_platform}_${application_name}_Prod-Upload") {
          parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}    }  }   }   }}
   publishers {
        wsCleanup()
    }
}

job("${application_platform}_${application_name}_Prod-Upload") {
    steps {
      logRotator { numToKeep 5 }
       label("${agentLabel}")
  parameters {

    stringParam "BUILD_NUMBER"  }

      steps { scm{ git { remote{
          url('git@git.drfirst.com:devops-se/chef-sysops.git')
            credentials('svc-devops')
            branches('*/master')  }  } }
    }
}
     wrappers {
        buildName('${BUILD_NUMBER}')
        colorizeOutput()
        timestamps()
        preBuildCleanup{
       cleanupParameter()}
   }

  steps {
    shell{
command(''' echo hello ''')


    }

    downstreamParameterized  {
          trigger("${application_platform}_${application_name}_QA_Prod-Deploy") {

        parameters{
          predefinedProps([BUILD_NUMBER: '$BUILD_NUMBER'])
                  }}
        }
  }
publishers {
        wsCleanup()
    }
}

job("${application_platform}_${application_name}_QA_Prod-Deploy") {
  steps {
      logRotator { numToKeep 5 }
       label("${agentLabel}")
  parameters {
    stringParam "DEV_EMAIL"
    stringParam "BUILD_NUMBER"  }
  steps {  scm{ git { remote{
          url("${projRepo}")
            credentials("$gitCred")
            branches('*/master')  }  } }
          }
    }
    wrappers {
         buildName('${BUILD_NUMBER}')
         colorizeOutput()
         timestamps()
         preBuildCleanup{
       	cleanupParameter()}
      }
     steps {
	shell{
              command(''' echo hello''')
        }
     }
 publishers {
        wsCleanup()
    }
   }

deliveryPipelineView("${application_platform}_${application_name}") {

    pipelineInstances(1)
    showAggregatedPipeline(true)
    columns(1)
    sorting(Sorting.TITLE)
    allowRebuild(true)
    updateInterval(2)
    enableManualTriggers()
    showAvatars()
    showChangeLog(true)
    pipelines {
          component('DEV & QA', "${application_platform}_${application_name}_Dev-Deploy")
          component('Staging & Prod',"${application_platform}_${application_name}_Staging-Promotion")
        }
  configure { view ->
    def components = view / componentSpecs
    components.'se.diabol.jenkins.pipeline.DeliveryPipelineView_-ComponentSpec'[0] << lastJob("${application_platform}_${application_name}_QA-Signoff")
    components.'se.diabol.jenkins.pipeline.DeliveryPipelineView_-ComponentSpec'[1] << lastJob("${application_platform}_${application_name}_QA_Prod-Deploy")
  }
}
}
