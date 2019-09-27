# mobileconfigsigner
Signs .mobileconfig files for distribution to iOS, tvOS, iPadOS and macOS devices. Built to be hosted in a Lambda so you don't need to have keys in a client-side app. 

Lambda Input parameters:

- bucketName - this is the name of the S3 bucket that will hold the pem files + file to sign
- signerFile - name of the file in S3 bucket; this file you supplied as part of the -signer variable
- keyFile - name of the file in S3 bucket; this file you supplied as part of the -inkey variable
- certFile - name of the file in S3 bucket; this file you supplied as part of the -certfile variable
- fileToSign - name of the file in S3 bucket; this is the file you used as part of the -in variable

This function generates byte output to the console, that you can copy and use as you like, or pipe into another service.

Important:
- the role you assing to the lambda function needs to have access to the S3 bucket, a sample policy would be:
"Version": "2019-09-25",
"Statement": [{
  "Effect": "Allow",
  "Action": [
    "s3:*"
  ],
  "Resource": [
    "arn:aws:s3:::mybucket",
    "arn:aws:s3:::mybucket/*"
  ]
}]

