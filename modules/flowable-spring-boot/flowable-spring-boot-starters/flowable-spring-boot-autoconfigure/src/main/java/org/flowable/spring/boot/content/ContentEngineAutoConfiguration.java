/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.spring.boot.content;

import javax.sql.DataSource;

import org.flowable.content.engine.ContentEngineConfiguration;
import org.flowable.content.engine.configurator.ContentEngineConfigurator;
import org.flowable.content.spring.SpringContentEngineConfiguration;
import org.flowable.content.spring.configurator.SpringContentEngineConfigurator;
import org.flowable.spring.boot.AbstractEngineAutoConfiguration;
import org.flowable.spring.boot.FlowableProperties;
import org.flowable.spring.boot.FlowableTransactionAutoConfiguration;
import org.flowable.spring.boot.ProcessEngineAutoConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.flowable.spring.boot.condition.ConditionalOnContentEngine;
import org.flowable.spring.boot.condition.ConditionalOnProcessEngine;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-Configuration} for the Content Engine
 *
 * @author Filip Hrisafov
 */
@Configuration
@ConditionalOnContentEngine
@EnableConfigurationProperties({
    FlowableProperties.class,
    FlowableContentProperties.class
})
@AutoConfigureAfter({
    FlowableTransactionAutoConfiguration.class
})
@AutoConfigureBefore({
    ProcessEngineAutoConfiguration.class
})
public class ContentEngineAutoConfiguration extends AbstractEngineAutoConfiguration {

    public ContentEngineAutoConfiguration(FlowableProperties flowableProperties) {
        super(flowableProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringContentEngineConfiguration contentEngineConfiguration(DataSource dataSource, PlatformTransactionManager platformTransactionManager) {
        SpringContentEngineConfiguration configuration = new SpringContentEngineConfiguration();

        configuration.setTransactionManager(platformTransactionManager);
        configureEngine(configuration, dataSource);

        return configuration;
    }

    @Configuration
    @ConditionalOnProcessEngine
    public static class ContentEngineProcessConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "contentProcessEngineConfigurationConfigurer")
        public ProcessEngineConfigurationConfigurer contentProcessEngineConfigurationConfigurer(ContentEngineConfigurator contentEngineConfigurator) {
            return processEngineConfiguration -> processEngineConfiguration.addConfigurator(contentEngineConfigurator);
        }

        @Bean
        @ConditionalOnMissingBean
        public ContentEngineConfigurator contentEngineConfigurator(ContentEngineConfiguration configuration) {
            SpringContentEngineConfigurator contentEngineConfigurator = new SpringContentEngineConfigurator();
            contentEngineConfigurator.setContentEngineConfiguration(configuration);
            return contentEngineConfigurator;
        }
    }
}
