package org.processmining.ocel.discoveryvisualization;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginDescriptorID;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.impl.AbstractPluginDescriptor;
import org.processmining.framework.plugin.impl.PluginDescriptorIDImpl;
import org.processmining.framework.plugin.impl.PluginDescriptorImpl;

public class PluginDescriptorImpl2 extends AbstractPluginDescriptor {

	private final AnnotatedElement annotatedElement;
	// This list contains either Class<? extends Object> or
	// Class<? extends Object[]>
	private final PluginDescriptorID id;
	private final List<List<Class<?>>> parameterTypes;
	private List<String> parameterNames;
	private final List<Class<?>> returnTypes;
	private final List<Method> methods;
	private final List<String> returnNames;
	private final String name;
	//	public Class<? extends PluginContext> contextType = null;
	private final Class<?> declaringClass;
	private final PackageDescriptor pack;

	private String help;
	private String[] keywords;
	private PluginCategory[] categories;
	private PluginQuality quality;
	private PluginLevel level;
	
	public PluginDescriptorImpl2(Method method, PackageDescriptor pack) throws Exception {
		assert (method != null);
		assert (method.isAnnotationPresent(Plugin.class));
		this.pack = pack;
		id = new PluginDescriptorIDImpl(method);
		parameterTypes = new ArrayList<List<Class<?>>>(1);
		ArrayList<Class<?>> list = new ArrayList<Class<?>>(method.getParameterTypes().length - 1);
		parameterTypes.add(list);
		declaringClass = method.getDeclaringClass();

		for (Class<?> par : method.getParameterTypes()) {
			if (!PluginContext.class.isAssignableFrom(par)) {
				list.add(par);
			}
		}
		methods = new ArrayList<Method>(1);
		methods.add(method);

		annotatedElement = method;
		name = method.getAnnotation(Plugin.class).name();
		help = method.getAnnotation(Plugin.class).help();
		keywords = method.getAnnotation(Plugin.class).keywords();
		categories = method.getAnnotation(Plugin.class).categories();
		quality = method.getAnnotation(Plugin.class).quality();
		level = method.getAnnotation(Plugin.class).level();
		//		System.out.println("PluginDescriptorImpl,\"" + name + "\",\"" + (pack == null ? "" : pack.getName()) + "\"");

		parameterNames = Arrays.asList(getAnnotation(Plugin.class).parameterLabels());
		if (parameterNames.size() == 0) {
			parameterNames = new ArrayList<String>(parameterTypes.size());
			for (Class<?> par : list) {
				parameterNames.add(par.getSimpleName());
			}
		}

		returnTypes = Arrays.asList(getAnnotation(Plugin.class).returnTypes());
		assert (getAnnotation(Plugin.class).returnLabels().length == returnTypes.size());
		returnNames = Arrays.asList(getAnnotation(Plugin.class).returnLabels());
	}

	public PluginDescriptorImpl2(Class<?> type, Class<? extends PluginContext> acceptedContext, PackageDescriptor pack)
			throws Exception {
		//assert (type != null);
		//assert (type.isAnnotationPresent(Plugin.class));

		this.pack = pack;
		id = new PluginDescriptorIDImpl(type);
		annotatedElement = type;
		declaringClass = type;
		methods = new ArrayList<Method>();

		String[] pls = getAnnotation(Plugin.class).parameterLabels();

		int max = -1;
		for (Method method : type.getMethods()) {
			if (method.isAnnotationPresent(PluginVariant.class)) {
				if (method.getParameterTypes()[0].isAssignableFrom(acceptedContext)) {
					methods.add(method);
				}

				int[] requiredPar = method.getAnnotation(PluginVariant.class).requiredParameterLabels();
				for (int i : requiredPar) {
					if (i > max) {
						max = i;
					}
				}
			}
		}
		max++;

		parameterTypes = new ArrayList<List<Class<?>>>(methods.size());

		for (Method method : methods) {
			int[] requiredPar = method.getAnnotation(PluginVariant.class).requiredParameterLabels();
			ArrayList<Class<?>> list = new ArrayList<Class<?>>(method.getParameterTypes().length - 1);
			for (int i = 0; i < requiredPar.length; i++) {
				list.add(method.getParameterTypes()[i + 1]);
			}
			parameterTypes.add(list);
		}

		name = type.getAnnotation(Plugin.class).name();
		help = type.getAnnotation(Plugin.class).help();
		keywords = type.getAnnotation(Plugin.class).keywords();
		categories = type.getAnnotation(Plugin.class).categories();
		quality = type.getAnnotation(Plugin.class).quality();
		level = type.getAnnotation(Plugin.class).level();
		//		System.out.println("PluginDescriptorImpl,\"" + name + "\",\"" + (pack == null ? "" : pack.getName()) + "\"");

		// There are either no parameters, or all parameters are required at least once
		// in all variants, ignoring the specific context.
		//
		if ((pls.length > 0) && (pls.length < max)) {
			String message = "Plugin " + name
					+ " could not be added as a plugin. There is at lease one declared parameter type,"
					+ " which is not used by any of the plugin's variants.";
			throw new AssertionError(message);
		}

		parameterNames = Arrays.asList(pls);

		returnTypes = Arrays.asList(type.getAnnotation(Plugin.class).returnTypes());
		assert (getAnnotation(Plugin.class).returnLabels().length == returnTypes.size());
		returnNames = Arrays.asList(type.getAnnotation(Plugin.class).returnLabels());
	}

	public PluginDescriptorImpl2(String className, String name, Class<?>[] parTypes, PackageDescriptor pack) throws Exception {
		this(Class.forName(className).getMethod(name, parTypes), pack);
	}

	public PackageDescriptor getPackage() {
		return pack;
	}

	public int getMostSignificantResult() {
		return getAnnotation(Plugin.class).mostSignificantResult();
	}

	AnnotatedElement getAnnotatedElement() {
		return annotatedElement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.plugin.PluginDescriptor#hasAnnotation(java
	 * .lang.Class)
	 */
	public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
		return getAnnotatedElement().isAnnotationPresent(annotationClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.plugin.PluginDescriptor#getAnnotation(java
	 * .lang.Class)
	 */
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return getAnnotatedElement().getAnnotation(annotationClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.PluginDescriptor#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.PluginDescriptor#hashCode()
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.plugin.PluginDescriptor#equals(java.lang.
	 * Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof PluginDescriptorImpl)) {
			return false;
		}
		PluginDescriptorImpl otherDesc = (PluginDescriptorImpl) other;
		//return id.equals(otherDesc.id);
		return false;
		
		// note: this does not compare whether the plugins have been loaded by
		// the same class loader
		// return getMethod().getDeclaringClass().getName().equals(
		// otherDesc.getMethod().getDeclaringClass().getName())
		// && getMethod().getName()
		// .equals(otherDesc.getMethod().getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.PluginDescriptor#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	protected Object[] execute(PluginContext context, int methodIndex, Object... allArgs) throws Exception {
		Method method = getMethod(methodIndex);
		if (returnTypes.size() > 1) { // method.getReturnType().isArray())
			// {
			Object[] result;
			if ((method.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
				result = (Object[]) method.invoke(null, allArgs);
			} else {
				result = (Object[]) method.invoke(declaringClass.newInstance(), allArgs);
			}

			return result;
		} else {
			Object result;
			if ((method.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
				result = method.invoke(null, allArgs);
			} else {
				result = method.invoke(declaringClass.newInstance(), allArgs);
			}

			return new Object[] { result };
		}
	}

	protected Method findMethod(Object[] allArgs) {
		for (Method m : ((Class<?>) annotatedElement).getMethods()) {
			if (m.isAnnotationPresent(PluginVariant.class)) {
				boolean match = (m.getParameterTypes().length == allArgs.length);
				for (int i = 0; (i < allArgs.length) && match; i++) {
					Class<?> type = m.getParameterTypes()[i];
					if (type.isArray()) {
						if (allArgs[i].getClass().isArray()) {
							for (Object o : (Object[]) allArgs[i]) {
								match &= type.getComponentType().isAssignableFrom(o.getClass());
							}
						} else {
							match = false;
						}
					} else {
						match &= type.isAssignableFrom(allArgs[i].getClass());
					}
				}
				if (match) {
					return m;
				}
			}
		}
		return null;
	}

	/**
	 * returns the labels of the objects returned if this plugin is invoked, in
	 * the order in which they are returned
	 * 
	 * @return
	 */
	public List<Class<? extends Object>> getReturnTypes() {
		return Collections.unmodifiableList(returnTypes);
	}

	public List<Class<?>> getParameterTypes(int methodIndex) {
		return Collections.unmodifiableList(getParameterTypes().get(methodIndex));
	}

	public Class<?> getPluginParameterType(int methodIndex, int parameterIndex) {
		if (methods.get(methodIndex).isAnnotationPresent(PluginVariant.class)) {
			int[] req = methods.get(methodIndex).getAnnotation(PluginVariant.class).requiredParameterLabels();
			for (int j = 0; j < req.length; j++) {
				if (req[j] == parameterIndex) {
					return parameterTypes.get(methodIndex).get(j);
				}
			}
			return null;
		} else {
			return parameterTypes.get(methodIndex).get(parameterIndex);
		}
	}

	public List<List<Class<?>>> getParameterTypes() {
		return Collections.unmodifiableList(parameterTypes);
	}

	public List<String> getParameterNames() {
		return Collections.unmodifiableList(parameterNames);
	}

	public Set<Class<?>> getTypesAtParameterIndex(int index) {
		HashSet<Class<?>> result = new HashSet<Class<?>>();
		for (int j = 0; j < methods.size(); j++) {
			Class<?> name = getPluginParameterType(j, index);
			if (name != null) {
				result.add(name);
			}
		}

		return result;
	}

	/**
	 * Return the number of methods in this plugin.
	 * 
	 * @return
	 */
	public int getNumberOfMethods() {
		return methods.size();
	}

	public List<String> getParameterNames(int methodIndex) {
		if (methods.get(methodIndex).isAnnotationPresent(PluginVariant.class)) {
			ArrayList<String> list = new ArrayList<String>();
			int[] req = methods.get(methodIndex).getAnnotation(PluginVariant.class).requiredParameterLabels();
			for (int i : req) {
				list.add(parameterNames.get(i));
			}
			return Collections.unmodifiableList(list);
		} else {
			return Collections.unmodifiableList(parameterNames);
		}
	}

	public String getPluginParameterName(int methodIndex, int parameterIndex) {
		if (methods.get(methodIndex).isAnnotationPresent(PluginVariant.class)) {
			int[] req = methods.get(methodIndex).getAnnotation(PluginVariant.class).requiredParameterLabels();
			for (int j = 0; j < req.length; j++) {
				if (req[j] == parameterIndex) {
					return parameterNames.get(parameterIndex);
				}
			}
			return null;
		} else {
			return parameterNames.get(parameterIndex);
		}
	}

	/**
	 * returns the types of the objects returned if this plugin is invoked, in
	 * the order in which they are returned
	 * 
	 * @return
	 */
	public List<String> getReturnNames() {
		return Collections.unmodifiableList(returnNames);
	}

	public PluginDescriptorID getID() {
		return id;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends PluginContext> getContextType(int methodIndex) {
		return (Class<? extends PluginContext>) methods.get(methodIndex).getParameterTypes()[0];
	}

	public int compareTo(PluginDescriptor plugin) {
		if (plugin.equals(this)) {
			return 0;
		}

		int c = getName().toLowerCase().compareTo(plugin.getName().toLowerCase());
		if (c == 0) {
			c = id.compareTo(plugin.getID());
		}
		return c;
	}

	Method getMethod(int i) {
		return methods.get(i);
	}

	public int getIndexInParameterNames(int methodIndex, int methodParameterIndex) {
		if (methods.get(methodIndex).isAnnotationPresent(PluginVariant.class)) {
			int[] req = methods.get(methodIndex).getAnnotation(PluginVariant.class).requiredParameterLabels();
			return req[methodParameterIndex];
		} else {
			return methodParameterIndex;
		}
	}

	public int getIndexInMethod(int methodIndex, int parameterIndex) {
		if (methods.get(methodIndex).isAnnotationPresent(PluginVariant.class)) {
			int[] req = methods.get(methodIndex).getAnnotation(PluginVariant.class).requiredParameterLabels();
			for (int i = 0; i < req.length; i++) {
				if (req[i] == parameterIndex) {
					return i;
				}
			}
			return -1;
		} else {
			return parameterIndex;
		}
	}

	public String getMethodLabel(int methodIndex) {
		if (methods.get(methodIndex).isAnnotationPresent(PluginVariant.class)
				&& !methods.get(methodIndex).getAnnotation(PluginVariant.class).variantLabel().equals("")) {
			return methods.get(methodIndex).getAnnotation(PluginVariant.class).variantLabel();
		} else {
			return name;
		}
	}

	public boolean isUserAccessible() {
		return getAnnotation(Plugin.class).userAccessible();
	}

	public boolean handlesCancel() {
		return getAnnotation(Plugin.class).handlesCancel();
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass, int methodIndex) {
		return getMethod(methodIndex).getAnnotation(annotationClass);
	}

	public boolean hasAnnotation(Class<? extends Annotation> annotationClass, int methodIndex) {
		return getMethod(methodIndex).getAnnotation(annotationClass) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.PluginDescriptor#getName()
	 */
	public String getHelp() {
		return help;
	}

	public String getMethodHelp(int methodIndex) {
		if (methods.get(methodIndex).isAnnotationPresent(PluginVariant.class)
				&& !methods.get(methodIndex).getAnnotation(PluginVariant.class).help().equals("")) {
			return methods.get(methodIndex).getAnnotation(PluginVariant.class).help();
		} else {
			return name;
		}
	}

	public String[] getKeywords() {
		return this.keywords;
	}

	public String[] getCategories() {
		String[] categoryLabels = new String[this.categories.length];
		for(int i = 0; i < this.categories.length; i++){
			categoryLabels[i] = this.categories[i].getName();//.getName();
		}
		return categoryLabels;
	}

	public boolean meetsQualityThreshold() {
		return Boot.PLUGIN_QUALITY_THRESHOLD.getValue() <= quality.getValue();
	}

	public boolean meetsLevelThreshold() {
		return Boot.PLUGIN_LEVEL_THRESHOLD.getValue() <= level.getValue();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public URL getURL() {
		// TODO Auto-generated method stub
		return null;
	}
}
