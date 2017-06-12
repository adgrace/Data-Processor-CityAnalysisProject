/*
 * Copyright 2013-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Further developed & adapted by Alex Grace for research purposes only. (ag00248@surrey.ac.uk)
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.alexgrace.finalyearproject.dataprocessor.entities;


/**
*  Keys for configuration overrides (via properties file).
*/
public class ConfigKeys {

   /**
    * Name of the application.
    */
   public static final String APPLICATION_NAME_KEY = "appName";


   /**
    * The url of the database
    */
   public static final String DB_HOST = "dbHost";

   /**
    * The port of the database
    */
   public static final String DB_PORT = "dbPort";

   /**
    * Database Username
    */
   public static final String DB_USERNAME = "dbUser";

   /**
    * Database Password
    */
   public static final String DB_PASSWORD = "dbPass";

   /**
    * S3 Bucket Name
    */
   public static final String S3_BUCKET_NAME = "s3bucketName";

   /**
    * S3 Key
    */
   public static final String AWS_ACCESS = "awsaccesskey";

   /**
    * S3 Key
    */
   public static final String AWS_SECRET = "awssecretkey";


   private ConfigKeys() {
   }
}
