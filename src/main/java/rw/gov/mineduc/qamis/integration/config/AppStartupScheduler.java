package rw.gov.mineduc.qamis.integration.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import rw.gov.mineduc.qamis.integration.service.InspectionService;

@Component
public class AppStartupScheduler implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private InspectionService inspectionService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event){
        inspectionService.ScheduleInspectionQamisUpdate();
    }
}
