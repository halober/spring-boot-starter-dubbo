package com.halober.dubbo.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.halober.dubbo.annotation.Inject;

public class AnnotationBean extends com.alibaba.dubbo.config.spring.AnnotationBean {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AnnotationBean.class);

	private ConcurrentMap<String, ReferenceBean<?>> referenceSelfConfigs = null;

	private ApplicationContext applicationContext;

	private String[] annotationPackages;

	@SuppressWarnings("unchecked")
	public AnnotationBean() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		super();
		Field field = com.alibaba.dubbo.config.spring.AnnotationBean.class.getDeclaredField("referenceConfigs");
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		referenceSelfConfigs = (ConcurrentMap<String, ReferenceBean<?>>) field.get(this);
		field.setAccessible(false);

	}

	@Override
	public void setPackage(String annotationPackage) {
		super.setPackage(annotationPackage);
		this.annotationPackages = (annotationPackage == null || annotationPackage.length() == 0) ? null : Constants.COMMA_SPLIT_PATTERN.split(annotationPackage);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		super.setApplicationContext(applicationContext);
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (!isMatchPackage(bean)) {
			return bean;
		}
		this.buildMethod(bean);
		this.buildField(bean);
		return super.postProcessBeforeInitialization(bean, beanName);
	}

	private void buildField(Object bean) {
		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				Inject inject = field.getAnnotation(Inject.class);
				if (inject != null) {
					Object value = this.refer(inject, field.getType());
					if (value != null) {
						field.set(bean, value);
					}
				}
			} catch (Throwable e) {
				logger.error("Failed to init remote service reference at filed {} in class {}, cause: {}", field.getName(), bean.getClass().getName(), e.getMessage(), e);
			}
		}
	}

	private void buildMethod(Object bean) {
		Method[] methods = bean.getClass().getMethods();
		for (Method method : methods) {
			String name = method.getName();
			if (name.length() > 3 && name.startsWith("set") && method.getParameterTypes().length == 1
					&& Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
				try {
					Inject inject = method.getAnnotation(Inject.class);
					if (inject != null) {
						Object value = refer(inject, method.getParameterTypes()[0]);
						if (value != null) {
							method.invoke(bean, new Object[] { value });
						}
					}
				} catch (Throwable e) {
					logger.error("Failed to init remote service reference at method {} in class {}, cause: {}", name, bean.getClass().getName(), e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 如果可以直接从spring容器加载，就不从dubbo容器加载
	 * 
	 * @param inject
	 * @param referenceClass
	 * @return
	 */
	private Object refer(Inject inject, Class<?> referenceClass) {
		try {
			Object obj = applicationContext.getBean(referenceClass);
			if (obj != null)
				return obj;
		} catch (BeansException e) {
			logger.debug("从spring上下文无法正确注入{}，将从dubbo中加载  , Error Message:{}", referenceClass, e.getMessage());
		}
		return refer(inject.value(), referenceClass);
	}

	protected Object refer(Reference reference, Class<?> referenceClass) { // method.getParameterTypes()[0]
		String interfaceName;
		if (!"".equals(reference.interfaceName())) {
			interfaceName = reference.interfaceName();
		} else if (!void.class.equals(reference.interfaceClass())) {
			interfaceName = reference.interfaceClass().getName();
		} else if (referenceClass.isInterface()) {
			interfaceName = referenceClass.getName();
		} else {
			throw new IllegalStateException( "The @Reference undefined interfaceClass or interfaceName, and the property type " + referenceClass.getName() + " is not a interface.");
		}
		String key = String.format("%s/%s:%s", reference.group(), interfaceName, reference.version());
		ReferenceBean<?> referenceConfig = referenceSelfConfigs.get(key);
		if (referenceConfig == null) {
			referenceConfig = new ReferenceBean<Object>(reference);
			if (void.class.equals(reference.interfaceClass()) && "".equals(reference.interfaceName()) && referenceClass.isInterface()) {
				referenceConfig.setInterface(referenceClass);
			}
			if (applicationContext != null) {
				referenceConfig.setApplicationContext(applicationContext);
				if (reference.registry() != null && reference.registry().length > 0) {
					List<RegistryConfig> registryConfigs = new ArrayList<RegistryConfig>();
					for (String registryId : reference.registry()) {
						if (registryId != null && registryId.length() > 0) {
							registryConfigs.add((RegistryConfig) applicationContext.getBean(registryId, RegistryConfig.class));
						}
					}
					referenceConfig.setRegistries(registryConfigs);
				}
				if (reference.consumer() != null && reference.consumer().length() > 0) {
					referenceConfig.setConsumer((ConsumerConfig) applicationContext.getBean(reference.consumer(), ConsumerConfig.class));
				}
				if (reference.monitor() != null && reference.monitor().length() > 0) {
					referenceConfig.setMonitor((MonitorConfig) applicationContext.getBean(reference.monitor(), MonitorConfig.class));
				}
				if (reference.application() != null && reference.application().length() > 0) {
					referenceConfig.setApplication((ApplicationConfig) applicationContext.getBean(reference.application(), ApplicationConfig.class));
				}
				if (reference.module() != null && reference.module().length() > 0) {
					referenceConfig.setModule((ModuleConfig) applicationContext.getBean(reference.module(), ModuleConfig.class));
				}
				if (reference.consumer() != null && reference.consumer().length() > 0) {
					referenceConfig.setConsumer((ConsumerConfig) applicationContext.getBean(reference.consumer(), ConsumerConfig.class));
				}
				try {
					referenceConfig.afterPropertiesSet();
				} catch (RuntimeException e) {
					throw (RuntimeException) e;
				} catch (Exception e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			referenceSelfConfigs.putIfAbsent(key, referenceConfig);
			referenceConfig = referenceSelfConfigs.get(key);
		}
		return referenceConfig.get();
	}

	private boolean isMatchPackage(Object bean) {
		if (annotationPackages == null || annotationPackages.length == 0) {
			return true;
		}
		String beanClassName = bean.getClass().getName();
		for (String pkg : annotationPackages) {
			if (beanClassName.startsWith(pkg)) {
				return true;
			}
		}
		return false;
	}

}
