package com.reger.dubbo.config;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerWithGeneratedName;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.ClassUtils.resolveClassName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.context.annotation.DubboClassPathBeanDefinitionScanner;

public class AnnotationBean extends AbstractConfig implements DisposableBean, BeanDefinitionRegistryPostProcessor,
		ResourceLoaderAware, EnvironmentAware, BeanClassLoaderAware {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(AnnotationBean.class);
	
	private BeanDefinitionRegistry registry;
	
	private ResourceLoader resourceLoader;
	
	private Environment environment;
	
	private ClassLoader classLoader;
	
	private String[] annotationPackages;

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		this.registry = registry;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	}

	public void setPackage(String annotationPackage) {
		if (StringUtils.hasText(annotationPackage)) {
			this.annotationPackages = trims(annotationPackage);
		} else {
			this.annotationPackages = new String[] {};
		}
	}

	protected void postProcessAnnotationPackageService() {
		if (this.annotationPackages.length == 0) {
			return;
		}
		DubboClassPathBeanDefinitionScanner dubboClassPathBeanDefinitionScanner = new DubboClassPathBeanDefinitionScanner( registry, environment, resourceLoader);
		dubboClassPathBeanDefinitionScanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));
		Set<BeanDefinitionHolder> beanDefinitionHolders = dubboClassPathBeanDefinitionScanner.doScan(this.annotationPackages);
		for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
			registerServiceBean(beanDefinitionHolder, registry);
		}
		logger.debug("{} annotated @Service Components { {} } were scanned under package[{}]", beanDefinitionHolders.size(), beanDefinitionHolders, this.annotationPackages);
	}

	/**
	 * Registers {@link ServiceBean} from new annotated {@link Service}
	 * {@link BeanDefinition}
	 *
	 * @param beanDefinitionHolder
	 * @param registry
	 * @see ServiceBean
	 * @see BeanDefinition
	 */
	private void registerServiceBean(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry) {
		Class<?> beanClass = resolveClass(beanDefinitionHolder);
		Service service = findAnnotation(beanClass, Service.class);
		Class<?> interfaceClass = resolveServiceInterfaceClass(beanClass, service);
		String beanName = beanDefinitionHolder.getBeanName();
		if(interfaceClass==null){
			Class<?>[] interfacess = beanClass.getInterfaces();
			Assert.isTrue(interfacess.length!=0, beanClass+"没有实现任何接口，不可以发布服务");
			for (Class<?> interfaces : interfacess) {
				AbstractBeanDefinition serviceBeanDefinition = buildServiceBeanDefinition(service, interfaces, beanName);
				registerWithGeneratedName(serviceBeanDefinition, registry);
			}
		}else{
			AbstractBeanDefinition serviceBeanDefinition = buildServiceBeanDefinition(service, interfaceClass, beanName);
			registerWithGeneratedName(serviceBeanDefinition, registry);
		}
	}

	private ManagedList<RuntimeBeanReference> toRuntimeBeanReferences(String... beanNames) {
		ManagedList<RuntimeBeanReference> runtimeBeanReferences = new ManagedList<RuntimeBeanReference>();
		if (!ObjectUtils.isEmpty(beanNames)) {
			for (String beanName : beanNames) {
				runtimeBeanReferences.add(new RuntimeBeanReference(beanName));
			}
		}
		return runtimeBeanReferences;
	}

	private AbstractBeanDefinition buildServiceBeanDefinition(Service service, Class<?> interfaceClass,
			String annotatedServiceBeanName) {
		BeanDefinitionBuilder builder = 
				rootBeanDefinition(ServiceBean.class)
				.addConstructorArgValue(service)
				.addPropertyReference("ref", annotatedServiceBeanName)
				.addPropertyValue("interfaceClass", interfaceClass);
		/**
		 * Add {@link com.alibaba.dubbo.config.ProviderConfig} Bean reference
		 */
		String providerConfigBeanName = service.provider();
		if (StringUtils.hasText(providerConfigBeanName)) {
			builder.addPropertyReference("provider", providerConfigBeanName);
		}
		/**
		 * Add {@link com.alibaba.dubbo.config.MonitorConfig} Bean reference
		 */
		String monitorConfigBeanName = service.monitor();
		if (StringUtils.hasText(monitorConfigBeanName)) {
			builder.addPropertyReference("monitor", monitorConfigBeanName);
		}
		/**
		 * Add {@link com.alibaba.dubbo.config.ApplicationConfig} Bean reference
		 */
		String applicationConfigBeanName = service.application();
		if (StringUtils.hasText(applicationConfigBeanName)) {
			builder.addPropertyReference("application", applicationConfigBeanName);
		}
		/**
		 * Add {@link com.alibaba.dubbo.config.ModuleConfig} Bean reference
		 */
		String moduleConfigBeanName = service.module();
		if (StringUtils.hasText(moduleConfigBeanName)) {
			builder.addPropertyReference("application", moduleConfigBeanName);
		}
		/**
		 * Add {@link com.alibaba.dubbo.config.RegistryConfig} Bean reference
		 */
		String[] registryConfigBeanNames = service.registry();
		List<RuntimeBeanReference> registryRuntimeBeanReferences = toRuntimeBeanReferences(registryConfigBeanNames);
		if (!registryRuntimeBeanReferences.isEmpty()) {
			builder.addPropertyValue("registries", registryRuntimeBeanReferences);
		}
		/**
		 * Add {@link com.alibaba.dubbo.config.ProtocolConfig} Bean reference
		 */
		String[] protocolConfigBeanNames = service.protocol();
		List<RuntimeBeanReference> protocolRuntimeBeanReferences = toRuntimeBeanReferences(protocolConfigBeanNames);
		if (!registryRuntimeBeanReferences.isEmpty()) {
			builder.addPropertyValue("protocols", protocolRuntimeBeanReferences);
		}
		return builder.getBeanDefinition();

	}

	private Class<?> resolveServiceInterfaceClass(Class<?> annotatedServiceBeanClass, Service service) {
		Class<?> interfaceClass = service.interfaceClass();
		if (void.class.equals(interfaceClass)) {
			interfaceClass = null;
			String interfaceClassName = service.interfaceName();
			if (StringUtils.hasText(interfaceClassName)) {
				if (ClassUtils.isPresent(interfaceClassName, classLoader)) {
					interfaceClass = resolveClassName(interfaceClassName, classLoader);
				}
			}
		}
		if(interfaceClass==null){
			return null;
		}
		Assert.isTrue(interfaceClass.isInterface(), "The type that was annotated @Service is not an interface!");
		return interfaceClass;
	}


    private Class<?> resolveClass(BeanDefinitionHolder beanDefinitionHolder) {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
        return resolveClass(beanDefinition);

    }

    private Class<?> resolveClass(BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        return resolveClassName(beanClassName, classLoader);

    }

	/**
	 * 切包名字符串
	 * 
	 * @param annotationPackage
	 *            包名
	 * @return 切好后的字符串
	 */
	private static String[] trims(String annotationPackage) {
		String[] tmpes = Constants.COMMA_SPLIT_PATTERN.split(annotationPackage);
		List<String> packages = new ArrayList<String>();
		for (String tmpe : tmpes) {
			tmpe = tmpe.trim();
			if (!tmpe.isEmpty()) {
				packages.add(tmpe);
			}
		}
		return packages.toArray(new String[] {});
	}

	/**
	 * 匹配类实例是否在包中
	 * 
	 * @param bean
	 *            被判断的类
	 * @return 是否包含
	 */
	protected boolean isMatchPackage(Object bean) {
		if (annotationPackages.length == 0) {
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
		if (AopUtils.isAopProxy(bean)) {
			return AopUtils.getTargetClass(bean);
		}
		return bean.getClass();
	}

	@Override
	public void destroy() throws Exception {
		logger.info("dubbo开始关闭....");
		ProtocolConfig.destroyAll();
		RegistryConfig.destroyAll();
	}

}
