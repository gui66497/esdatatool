package com.zzjz.esdatatool.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

/**
 * @author 房桂堂
 * @description ElasticsearchConfiguration
 * @reference https://www.cnblogs.com/w-bb/articles/9743960.html
 * @date 2019/1/24 9:59
 */
@Configuration
public class ElasticsearchConfiguration implements FactoryBean<RestHighLevelClient>, InitializingBean, DisposableBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchConfiguration.class);

    @Value("${ES_HOST}")
    String esHost;

    @Value("${ES_PORT}")
    int esPort;

    @Value("${ES_METHOD}")
    String esMethod;

    @Value("${ES_USER}")
    String esUser;

    @Value("${ES_PASS}")
    String esPass;

    private RestHighLevelClient restHighLevelClient;

    @Override
    public void destroy() {
        try {
            if (restHighLevelClient != null) {
                restHighLevelClient.close();
            }
        } catch (IOException e) {
            LOGGER.error("Error closing ElasticSearch client: ", e);
        }
    }

    /**
     * 实例化
     * @return restHighLevelClient
     */
    @Override
    public RestHighLevelClient getObject() {
        return restHighLevelClient;
    }

    @Override
    public Class<?> getObjectType() {
        return RestHighLevelClient.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void afterPropertiesSet() {
        restHighLevelClient = buildClient();
    }

    private RestHighLevelClient buildClient() {
        try {
            /*restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(esHost, esPort, esMethod)));*/
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(esUser, esPass));
            RestClientBuilder builder = RestClient.builder(new HttpHost(esHost, esPort))
                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
            restHighLevelClient = new RestHighLevelClient(builder);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return restHighLevelClient;
    }
}
