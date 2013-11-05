package edu.sjsu.cmpe.procurement.jobs;

import com.google.common.collect.Sets;
import com.yammer.dropwizard.lifecycle.Managed;
import edu.sjsu.cmpe.procurement.annotations.Every;
import edu.sjsu.cmpe.procurement.parser.TimeUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobManager implements Managed {

    private static final Logger log = LoggerFactory.getLogger(JobManager.class);
    private Reflections reflections = null;
    protected Scheduler scheduler;

    public JobManager() {
       
    }
    
    public JobManager(String scanUrl) {
        reflections = new Reflections(scanUrl);
    }
    
    @Override
    public void start() throws Exception {
    	scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        scheduleAllJobsWithEveryAnnotation();
        
    }
    
    @Override
    public void stop() throws Exception {
    	Thread.sleep(100);
        scheduler.shutdown(true);
    }
    
    private List<Class<? extends Job>> getJobClasses(Class annotation) {
        Set<Class<? extends Job>> jobs = (Set<Class<? extends Job>>) reflections.getSubTypesOf(Job.class);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotation);

        return Sets.intersection(new HashSet<Class<? extends Job>>(jobs), annotatedClasses).immutableCopy().asList();
    }

   
    private void scheduleAllJobsWithEveryAnnotation() throws SchedulerException {
    	List<Class<? extends Job>> everyJobClasses = getJobClasses(Every.class);
        log.info("Jobs with @Every annotation: " + everyJobClasses);

        for (Class<? extends org.quartz.Job> clazz : everyJobClasses) {
            Every annotation = clazz.getAnnotation(Every.class);
            int secondDelay = TimeUtil.parseDuration(annotation.value());
            SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(secondDelay).repeatForever();
            Trigger trigger = TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).build();

            JobBuilder jobBuilder = JobBuilder.newJob(clazz);
            scheduler.scheduleJob(jobBuilder.build(), trigger);
        }
    }
}


