package spring.mine.config;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
//	@ComponentScans(value = { @ComponentScan("com.howtodoinjava.demo.spring")})
public class HibernateConfig {

	@Autowired
	private ApplicationContext context;

	static HibernateTransactionManager transactionManager;
	LocalSessionFactoryBean factoryBean;

	@Bean
	public LocalSessionFactoryBean getSessionFactory() {
		if (factoryBean == null) {
			factoryBean = new LocalSessionFactoryBean();
			factoryBean.setConfigLocation(context.getResource("classpath:hibernate/hibernate.cfg.xml"));
		}
//		factoryBean.setAnnotatedClasses(User.class);//
		return factoryBean;
	}

	@Bean
	public HibernateTransactionManager getTransactionManager(SessionFactory sessionFactory) {
		if (transactionManager == null) {
			transactionManager = new HibernateTransactionManager();
			transactionManager.setSessionFactory(sessionFactory);
		}
		return transactionManager;
	}

}