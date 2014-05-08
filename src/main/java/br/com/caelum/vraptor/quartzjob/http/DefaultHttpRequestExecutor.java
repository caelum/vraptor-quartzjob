package br.com.caelum.vraptor.quartzjob.http;

import java.io.IOException;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

import br.com.caelum.vraptor.environment.Property;

@Priority(Interceptor.Priority.LIBRARY_AFTER)
@Alternative
@ApplicationScoped
public class DefaultHttpRequestExecutor implements HttpRequestExecutor {
	
	public static final String VRAPTOR_QUARTZ_KEY = "vraptor.quartz.key";

	/**
	 * @deprecated cdi only
	 */
	DefaultHttpRequestExecutor() {
	}
	
	@Inject
	@Property(defaultValue="unsecured", value=VRAPTOR_QUARTZ_KEY) 
	private String securityKey;

	@Override
	public int execute(String url) {
		HttpClient client = new HttpClient();
		try {
			PostMethod method = new PostMethod(url);
			method.addParameter(VRAPTOR_QUARTZ_KEY, securityKey);
			return client.executeMethod(method);
		} catch (HttpException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
