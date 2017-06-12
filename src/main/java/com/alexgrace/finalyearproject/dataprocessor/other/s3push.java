package com.alexgrace.finalyearproject.dataprocessor.other;

import com.alexgrace.finalyearproject.dataprocessor.Processor;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by AlexGrace on 12/04/2017.
 */
public class s3push {
    private static final Log LOG = LogFactory.getLog(Processor.class);

    private static String s3Bucket;
    private static AmazonS3 s3client;

    public s3push(String Bucket, String awsKey, String awsSecret) {
        this.s3Bucket = Bucket;
        this.s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsKey,awsSecret))).build();
    }

    public void push(String name, String data) {
        try {
            s3client.putObject(s3Bucket, name , data);
            s3client.setObjectAcl(s3Bucket, name , CannedAccessControlList.PublicRead);
            LOG.info(name + " data updated");
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
