package org.octri.hpoonfhir;

import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.mustache.MustacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.samskivert.mustache.Mustache;

@Configuration
@EnableConfigurationProperties({ MustacheProperties.class })
public class MustacheConfig extends MustacheAutoConfiguration {

	public MustacheConfig(MustacheProperties mustache, Environment environment, ApplicationContext applicationContext) {
		super(mustache, environment, applicationContext);
	}

	@Override
	public Mustache.Compiler mustacheCompiler(Mustache.TemplateLoader mustacheTemplateLoader) {
		return super.mustacheCompiler(mustacheTemplateLoader).defaultValue("");
	}
}