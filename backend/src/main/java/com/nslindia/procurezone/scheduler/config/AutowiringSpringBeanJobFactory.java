package com.nslindia.procurezone.scheduler.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

/**
 * Quartz Job Factory with Spring autowiring support
 */
@Component
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {

    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
        beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    @NonNull
    protected Object createJobInstance(@NonNull TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        return job;
    }
}
