package pro.javatar.security.acl.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("pro.javatar.security.acl.repository")
@EnableAutoConfiguration
@EntityScan("pro.javatar.security.acl.entity")
@ComponentScan(basePackages = "pro.javatar.security.acl")
public class TestDBConfiguration {
}
