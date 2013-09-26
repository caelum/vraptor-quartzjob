package br.com.caelum.vraptor.quartzjob;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import br.com.caelum.vraptor.environment.Environment;

@javax.enterprise.context.ApplicationScoped
public class Env {

	private Environment env;
	private ServletContext context;

	@Deprecated // CDI eyes only
	public Env() {}

	@Inject
	public Env(Environment env, ServletContext context) {
		this.env = env;
		this.context = context;
	}

	public Env in(String name, Runnable toExecute) {
		if (env.getName().equals(name)) {
			toExecute.run();
		}
		return this;
	}

	public String host() {
		return env.get("host");
	}

	public String get(String key) {
		return env.get(key);
	}

	public String getHostAndContext() {
		return host() + context.getContextPath();
	}
}