import jenkins.model.*

def matchedJobs = Jenkins.instance.items.findAll { job ->
    job.name =~ /RR_*/
}
    
matchedJobs.each { job ->
    println job.name
    //job.delete()
    //println "Deleted ${job.delete}"
}
