package br.com.caelum.vraptor.quartzjob;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.environment.Environment;
import br.com.caelum.vraptor.environment.ServletBasedEnvironment;

@WebServlet(urlPatterns="/jobs/firstTime", displayName="quartz-startup", loadOnStartup=1)
public class QuartzStartupServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private final static Logger logger = LoggerFactory.getLogger(QuartzStartupServlet.class);

	@Override 
	public void init(ServletConfig cfg) throws ServletException {
		
		super.init(cfg);
		
		try {
			
			Environment environment = new ServletBasedEnvironment(getServletContext());
			
			if(!"production".equalsIgnoreCase(environment.getName())){
				return;
			}
		
			final String url = "http://localhost:8080" + cfg.getServletContext().getContextPath() + "/jobs/configure";

			Runnable quartzMe = new Runnable() {
				
				@Override
				public void run() {
					try {
						waitForServerStartup();
						logger.info("Invoking quartz configurator at " + url);
						HttpClient http = new HttpClient();
						http.executeMethod(new GetMethod(url));
					} catch (Exception e) {
						logger.error("Could not start quartz!");
					}
				}

				private void waitForServerStartup() throws InterruptedException {
					Thread.sleep(10000);
				}
			};
			new Thread(quartzMe).start();
			
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
