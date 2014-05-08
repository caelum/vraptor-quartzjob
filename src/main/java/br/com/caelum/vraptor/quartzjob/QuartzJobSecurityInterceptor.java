package br.com.caelum.vraptor.quartzjob;
import static br.com.caelum.vraptor.quartzjob.http.DefaultHttpRequestExecutor.VRAPTOR_QUARTZ_KEY;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import br.com.caelum.vraptor.Accepts;
import br.com.caelum.vraptor.AroundCall;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.controller.ControllerMethod;
import br.com.caelum.vraptor.environment.Property;
import br.com.caelum.vraptor.interceptor.SimpleInterceptorStack;
import br.com.caelum.vraptor.quartzjob.CronTask;
import br.com.caelum.vraptor.view.Results;

@Intercepts
public class QuartzJobSecurityInterceptor {
	
	@Property(defaultValue="unsecured", value=VRAPTOR_QUARTZ_KEY) 
	private String securityKey;
	@Inject
	private HttpServletRequest request;
	@Inject
	private Result result;
	
	@AroundCall
	public void verifySecurity(SimpleInterceptorStack stack) {
		String key = request.getParameter(VRAPTOR_QUARTZ_KEY);
		if (key != null && key.equals(securityKey)) {
			stack.next();
			return;
		}
		result.use(Results.http()).sendError(SC_FORBIDDEN);
	}
	
	@Accepts
    public boolean accepts(ControllerMethod method) {
		Class<?> controllerClass = method.getController().getType();
        return CronTask.class.isAssignableFrom(controllerClass);
    }

}
