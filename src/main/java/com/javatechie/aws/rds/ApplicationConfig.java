package com.javatechie.aws.rds;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Base64;

@Configuration
public class ApplicationConfig {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;


    private Gson gson = new Gson();

    @Bean
    public DataSource dataSource() {
        AwsSecrets secrets = getSecret();
        return DataSourceBuilder
                .create()
                //  .driverClassName("com.mysql.cj.jdbc.driver")
                .url("jdbc:" + secrets.getEngine() + "://" + secrets.getHost() + ":" + secrets.getPort() + "/javatechie")
                .username(secrets.getUsername())
                .password(secrets.getPassword())
                .build();
    }


    private AwsSecrets getSecret() {

        String secretName = "db-credential-on-aws";
        String region = "ap-south-2";


        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .withCredentials(
                        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))
                )
                .build();

        String secret;

        GetSecretValueRequest getSecretValueRequest
                = new GetSecretValueRequest().withSecretId(secretName);

        GetSecretValueResult getSecretValueResult = null;

        try {

            getSecretValueResult = client.getSecretValue(getSecretValueRequest);

        } catch (Exception e) {
            throw e;
        }

        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
            return gson.fromJson(secret, AwsSecrets.class);
        }


        return null;
    }

}
