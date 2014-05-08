package br.com.caelum.vraptor.quartzjob.http;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

@ApplicationScoped
public class DefaultHttpRequestExecutor implements HttpRequestExecutor {

	@Override
	public int execute(String url) {
		HttpClient client = new HttpClient();
		try {
			return client.executeMethod(new PostMethod(url));
		} catch (HttpException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
