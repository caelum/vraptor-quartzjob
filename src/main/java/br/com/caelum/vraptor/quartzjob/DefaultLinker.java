package br.com.caelum.vraptor.quartzjob;

import java.lang.reflect.Method;

import javax.inject.Inject;

import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.proxy.MethodInvocation;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.proxy.SuperMethod;

@SuppressWarnings({"rawtypes","unchecked"})
public class DefaultLinker implements Linker {

	private Method method;
	private Object[] args;
	private Proxifier proxifier;
	private Router router;
	private Class controllerType;
	private Env env;

	@Deprecated// CDI eyes only
	public DefaultLinker() {}

	@Inject
	DefaultLinker(Proxifier p, Router router, Env env) {
		this.proxifier = p;
		this.router = router;
		this.env = env;
	}

	@Override
	public <T> T linkTo(T controller) {
		return (T) linkTo(controller.getClass().getSuperclass());
	}

	@Override
	public <T> T linkTo(Class<T> controllerType) {
		this.controllerType = controllerType;
		MethodInvocation<T> invocation = new CacheInvocation();
		return proxifier.proxify(controllerType,invocation);
	}

	class CacheInvocation implements MethodInvocation{
		public Object intercept(Object proxy, Method method, Object[] args,
				SuperMethod superMethod) {
			DefaultLinker.this.method = method;
			DefaultLinker.this.args = args;
			return null;
		}
	}

	@Override
	public String get() {
		return env.getHostAndContext() + router.urlFor(controllerType, method, args);
	}

}
