package br.com.caelum.vraptor.quartzjob;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.environment.Environment;
import br.com.caelum.vraptor.environment.ServletBasedEnvironment;

@WebListener
public class ListenerQuartz implements ServletContextListener{
	
	private final static Logger logger = LoggerFactory.getLogger(ListenerQuartz.class);	

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			logger.info("Quartz servlet config initializing...");
			Environment environment = new ServletBasedEnvironment(event.getServletContext());
			
			if(!"production".equalsIgnoreCase(environment.getName())){
				return;
			}
		
			final String url = environment.get("host") + "/jobs/configure";

			Runnable quartzMe = new Runnable() {
				
				@Override
				public void run() {
					try {
						waitForServerStartup();
						logger.info("Invoking quartz configurator at " + url);
						HttpClient http = new HttpClient();
						http.executeMethod(new GetMethod(url));
					} catch (Exception e) {
						logger.error("Could not start quartz!", e);
					}
				}

				private void waitForServerStartup() throws InterruptedException {
					Thread.sleep(10000);
				}
			};
			new Thread(quartzMe).start();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}

}
