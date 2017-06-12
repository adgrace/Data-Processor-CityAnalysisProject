package com.alexgrace.finalyearproject.dataprocessor;

import com.alexgrace.finalyearproject.dataprocessor.entities.ConfigKeys;
import com.alexgrace.finalyearproject.dataprocessor.entities.LocationFilter;
import com.alexgrace.finalyearproject.dataprocessor.other.s3push;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.codehaus.jackson.map.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class DataProcessor
{
    private static Processor processor;
    private static s3push s3;

    private static ObjectMapper mapper;
    private static final Log LOG = LogFactory.getLog(DataProcessor.class);


    private static final String DEFAULT_APP_NAME = "Data-Processor";

    private static String applicationName = DEFAULT_APP_NAME;
    private static String dbHost;
    private static int dbPort = 3306;
    private static String dbUser;
    private static String dbPass;
    private static String s3Bucket;
    private static String awsAccess;
    private static String awsSecret;




    private static List<LocationFilter> locationFilter;
    private static String locationFilterString;

    public static void main( String[] args ) throws IOException {
        String propertiesFile = null, locationFilters = null;
        mapper = new ObjectMapper();

        if (args.length != 2) {
            System.err.println("Usage: java " + DataProcessor.class.getName() + " <propertiesFile>");
            System.exit(1);
        } else {
            propertiesFile = args[0];
            locationFilters= args[1];
        }

        configure(propertiesFile, locationFilters);

        LOG.info("Starting " + applicationName);

        int exitCode = 0;
        s3 = new s3push(s3Bucket, awsAccess, awsSecret);
        processor = new Processor(dbHost, dbPort, dbUser, dbPass, locationFilter);
        try {
            while (true) {
                processor.Run(s3);
                Thread.sleep(3600 * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            exitCode = 1;
        }
        System.exit(exitCode);
    }

    private static void configure(String propertiesFile, String locationFilters) throws IOException {

        if (propertiesFile != null) {
            loadProperties(propertiesFile);
            LOG.info("Success: Importing Properties Done");

        }
        if (locationFilters != null) {
            locationFilterString = new String(Files.readAllBytes(Paths.get(locationFilters)));
            locationFilter = mapper.readValue(locationFilterString, mapper.getTypeFactory().constructCollectionType(List.class, LocationFilter.class));
            LOG.info("Success: Importing Filtering Locations Done");
        }
    }

    /**
     * @param propertiesFile
     * @throws IOException Thrown when we run into issues reading properties
     */
    private static void loadProperties(String propertiesFile) throws IOException {
        FileInputStream inputStream = new FileInputStream(propertiesFile);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } finally {
            inputStream.close();
        }

        String appNameOverride = properties.getProperty(ConfigKeys.APPLICATION_NAME_KEY);
        if (appNameOverride != null) {
            applicationName = appNameOverride;
        }
        LOG.info("Using application name " + applicationName);

        String dbHostOverride = properties.getProperty(ConfigKeys.DB_HOST);
        if (dbHostOverride != null) {
            dbHost = dbHostOverride;
        }
        LOG.info("Using database host " + dbHost);

        String dbPortOverride = properties.getProperty(ConfigKeys.DB_PORT);
        if (dbPortOverride != null) {
            try {
                dbPort = Integer.parseInt(dbPortOverride);
            } catch(Exception e) {

            }
        }
        LOG.info("Using database port " + dbPort);

        String dbUserOverride = properties.getProperty(ConfigKeys.DB_USERNAME);
        if (dbUserOverride != null) {
            dbUser = dbUserOverride;
        }
        LOG.info("Using database username " + dbUser);

        String dbPassOverride = properties.getProperty(ConfigKeys.DB_PASSWORD);
        if (dbPassOverride != null) {
            dbPass = dbPassOverride;
        }
        LOG.info("Using database password " + dbPass);

        String s3BucketOverride = properties.getProperty(ConfigKeys.S3_BUCKET_NAME);
        if (s3BucketOverride != null) {
            s3Bucket = s3BucketOverride;
        }
        LOG.info("Using s3Bucket: " + s3Bucket);

        String awsAccessOverride = properties.getProperty(ConfigKeys.AWS_ACCESS);
        if (awsAccessOverride != null) {
            awsAccess = awsAccessOverride;
        }
        LOG.info("Using awsAccess: " + awsAccess);

        String awsSecretOverride = properties.getProperty(ConfigKeys.AWS_SECRET);
        if (awsSecretOverride != null) {
            awsSecret = awsSecretOverride;
        }
        LOG.info("Using awsSecret: " + awsSecret);
    }
}
