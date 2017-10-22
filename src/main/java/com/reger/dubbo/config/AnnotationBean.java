package com.reger.dubbo.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.reger.dubbo.annotation.Inject;

public class AnnotationBean extends com.alibaba.dubbo.config.spring.AnnotationBean {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AnnotationBean.class);

	private final Map<String, ReferenceBean<?>> referenceSelfConfigs;

	private final Set<ServiceConfig<?>> serviceConfigs;

	private ApplicationContext applicationContext;

	private String[] annotationPackages;

	@SuppressWarnings("unchecked")
	public AnnotationBean() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		super();
		Field referenceField = com.alibaba.dubbo.config.spring.AnnotationBean.class.getDeclaredField("referenceConfigs");
		if (!referenceField.isAccessible()) {
			referenceField.setAccessible(true);
		}
		referenceSelfConfigs = (Map<String, ReferenceBean<?>>) referenceField.get(this);
		referenceField.setAccessible(false);
		Field serviceField = com.alibaba.dubbo.config.spring.AnnotationBean.class.getDeclaredField("serviceConfigs");
		if (!serviceField.isAccessible()) {
			serviceField.setAccessible(true);
		}
		serviceConfigs = (Set<ServiceConfig<?>>) serviceField.get(this);
		serviceField.setAccessible(false);

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
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (!isMatchPackage(bean)) {
			return bean;
		}
		Class<?> clazs = this.getOriginalClass(bean);
		Service service = clazs.getAnnotation(Service.class);
		if (service != null) {
			if (void.class.equals(service.interfaceClass()) && "".equals(service.interfaceName())) {
				Class<?>[] interfaces = clazs.getInterfaces();
				Assert.notEmpty(interfaces, "Failed to export remote service class " + clazs.getName() + ", cause: The @Service undefined interfaceClass or interfaceName, and the service class unimplemented any interfaces.");
				for (Class<?> interfaceClass : interfaces) {
					this.export(bean, service, interfaceClass);
					logger.debug("dubbo成功将{}以{}方式导出", beanName, interfaceClass);
				}
			} else {
				this.export(bean, service, null);
			}
		}
		return bean;
	}

	private void export(Object bean, Service service, Class<?> interfaceClass) {
		ServiceBean<Object> serviceConfig = new ServiceBean<Object>(service);
		if (interfaceClass != null) {
			serviceConfig.setInterface(interfaceClass);
		}
		if (applicationContext != null) {
			serviceConfig.setApplicationContext(applicationContext);
			if (service.registry() != null && service.registry().length > 0) {
				serviceConfig.setRegistries(this.getRegistryConfigs(service.registry()));
			}
			if (service.provider() != null && service.provider().length() > 0) {
				serviceConfig.setProvider(this.getBean(service.provider(), ProviderConfig.class));
			}
			if (service.monitor() != null && service.monitor().length() > 0) {
				serviceConfig.setMonitor(this.getBean(service.monitor(), MonitorConfig.class));
			}
			if (service.application() != null && service.application().length() > 0) {
				serviceConfig.setApplication(this.getBean(service.application(), ApplicationConfig.class));
			}
			if (service.module() != null && service.module().length() > 0) {
				serviceConfig.setModule(this.getBean(service.module(), ModuleConfig.class));
			}
			if (service.provider() != null && service.provider().length() > 0) {
				serviceConfig.setProvider(this.getBean(service.provider(), ProviderConfig.class));
			}
			if (service.protocol() != null && service.protocol().length > 0) {
				serviceConfig.setProtocols(this.getProtocolConfigs(service.protocol()));
			}
			this.InitializingBean(serviceConfig);
		}
		serviceConfig.setRef(bean);
		serviceConfigs.add(serviceConfig);
		serviceConfig.export();
	}

	private void InitializingBean(org.springframework.beans.factory.InitializingBean bean) {
		try {
			bean.afterPropertiesSet();
		} catch (RuntimeException e) {
			throw (RuntimeException) e;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private List<ProtocolConfig> getProtocolConfigs(String[] protocols) {
		List<ProtocolConfig> protocolConfigs = new ArrayList<ProtocolConfig>();
		for (String protocolId : protocols) {
			if (protocolId != null && protocolId.length() > 0) {
				protocolConfigs.add(this.getBean(protocolId, ProtocolConfig.class));
			}
		}
		return protocolConfigs;
	}

	private List<RegistryConfig> getRegistryConfigs(String[] registrys) {
		List<RegistryConfig> registryConfigs = new ArrayList<RegistryConfig>();
		for (String registryId : registrys) {
			if (registryId != null && registryId.length() > 0) {
				registryConfigs.add(this.getBean(registryId, RegistryConfig.class));
			}
		}
		return registryConfigs;
	}

	private <T> T getBean(String name, Class<T> clazz) {
		return (T) applicationContext.getBean(name, clazz);
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (!isMatchPackage(bean)) {
			return bean;
		}
		this.buildMethod(bean);
		this.buildField(bean);
		return bean;
	}

	private void buildField(final Object bean) {
		ReflectionUtils.doWithFields(this.getOriginalClass(bean),new FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				try {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					Inject inject = field.getAnnotation(Inject.class);
					Class<?> type = field.getType();
					if (inject != null) {
						field.set(bean, AnnotationBean.this.refer(inject, type));
					} else {
						Reference reference = field.getAnnotation(Reference.class);
						if (reference != null) {
							field.set(bean, AnnotationBean.this.refer(reference, type));
						}
					}
				} catch (Throwable e) {
					logger.error("Failed to init remote service reference at filed {} in class {}, cause: {}", field.getName(), AnnotationBean.this.getOriginalClass(bean).getName(), e.getMessage(), e);
					throw new BeanInitializationException("Failed to init remote service reference at filed " + field.getName() + " in class " + bean.getClass().getName(), e);
				}
			
			}
		});  
	}

	private void buildMethod(final Object bean) {
		ReflectionUtils.doWithMethods(this.getOriginalClass(bean), new MethodCallback() {
			
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {

				if (method.getParameterTypes().length == 1 && Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
					try {
						Inject inject = method.getAnnotation(Inject.class);
						Class<?> type = method.getParameterTypes()[0];
						if (inject != null) {
							method.invoke(bean, new Object[] { AnnotationBean.this.refer(inject, type) });
						} else {
							Reference reference = method.getAnnotation(Reference.class);
							if (reference != null) {
								method.invoke(bean, new Object[] { AnnotationBean.this.refer(reference, type) });
							}
						}
					} catch (Throwable e) {
						logger.error("Failed to init remote service reference at method {} in class {}, cause: {}", method.getName(), AnnotationBean.this.getOriginalClass(bean).getName(), e.getMessage(), e);
						throw new BeanInitializationException("Failed to init remote service reference at method " + method.getName() + " in class " + bean.getClass().getName(), e);
					}
				}

			}
		}); 
	}

	/**
	 * 如果可以直接从spring容器加载，就不从dubbo容器加载
	 * 
	 * @param inject 注入的注解参数
	 * @param referenceClass 需要注入的类类型
	 * @return 注入的类实例
	 */
	protected Object refer(Inject inject, Class<?> referenceClass) {
		try {
			String beanName = inject.name().trim();
			if (beanName.isEmpty()) {
				Object obj = applicationContext.getBean(referenceClass);
				if (obj != null)
					return obj;
			} else {
				Object obj = this.getBean(beanName, referenceClass);
				if (obj != null)
					return obj;
			}
		} catch (BeansException e) {
			logger.debug("从spring上下文无法正确注入{}，将从dubbo中加载  , Error Message:{}", referenceClass, e.getMessage(), e);
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
		if (!referenceSelfConfigs.containsKey(key)) {
			referenceSelfConfigs.put(key,  this.initReferenceConfig(reference, referenceClass));
		}
		return referenceSelfConfigs.get(key).get();
	}

	private ReferenceBean<?> initReferenceConfig(Reference reference, Class<?> referenceClass) {
		ReferenceBean<?> referenceConfig = new ReferenceBean<Object>(reference);
		if (void.class.equals(reference.interfaceClass()) && "".equals(reference.interfaceName()) && referenceClass.isInterface()) {
			referenceConfig.setInterface(referenceClass);
		}
		if (applicationContext != null) {
			referenceConfig.setApplicationContext(applicationContext);
			if (reference.registry() != null && reference.registry().length > 0) {
				referenceConfig.setRegistries(this.getRegistryConfigs(reference.registry()));
			}
			if (reference.consumer() != null && reference.consumer().length() > 0) {
				referenceConfig.setConsumer(this.getBean(reference.consumer(), ConsumerConfig.class));
			}
			if (reference.monitor() != null && reference.monitor().length() > 0) {
				referenceConfig.setMonitor(this.getBean(reference.monitor(), MonitorConfig.class));
			}
			if (reference.application() != null && reference.application().length() > 0) {
				referenceConfig.setApplication(this.getBean(reference.application(), ApplicationConfig.class));
			}
			if (reference.module() != null && reference.module().length() > 0) {
				referenceConfig.setModule(this.getBean(reference.module(), ModuleConfig.class));
			}
			if (reference.consumer() != null && reference.consumer().length() > 0) {
				referenceConfig.setConsumer(this.getBean(reference.consumer(), ConsumerConfig.class));
			}
			this.InitializingBean(referenceConfig);
		}
		return referenceConfig;
	}

	/**
	 * 匹配类实例是否在包中
	 * 
	 * @param bean
	 *            被判断的类
	 * @return 是否包含
	 */
	protected boolean isMatchPackage(Object bean) {
		if (annotationPackages == null || annotationPackages.length == 0) {
			return true;
		}
		String beanClassName = this.getOriginalClass(bean).getName();
		for (String pkg : annotationPackages) {
			if (beanClassName.startsWith(pkg)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取bean的原始类型
	 * 
	 * @param bean
	 *            输入的bean对象
	 * @return bean的原始类型
	 */
	private Class<?> getOriginalClass(Object bean) {
		if (AopUtils.isAopProxy(bean))
			return AopUtils.getTargetClass(bean);
		return bean.getClass();
	}

	@Override
	public void destroy() throws Exception {
		logger.info("dubbo开始关闭....");
		super.destroy();
		ProtocolConfig.destroyAll();
		RegistryConfig.destroyAll();
	}
}
