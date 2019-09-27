# mobileconfigsigner
Signs .mobileconfig files for distribution to iOS, tvOS, iPadOS and macOS devices. Built to be hosted in a Lambda so you don't need to have keys in a client-side app. 

Lambda Input parameters:

- bucketName - this is the name of the S3 bucket that will hold the pem files + file to sign
- signerFile (e.g. ca.key in example source)- name of the file in S3 bucket; this file you supplied as part of the -signer variable
- keyFile (e.g. key.pem in example source) - name of the file in S3 bucket; this file you supplied as part of the -inkey variable
- certFile - (e.g. cert.pem in example source) name of the file in S3 bucket; this file you supplied as part of the -certfile variable
- fileToSign - (e.g. profiletemplate in example source) name of the file in S3 bucket; this is the file you used as part of the -in variable

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

Also, the certs in the example were self-signed and generated from my laptop. I left them in as samples but you should replace them with publicly created objects via a 3rd party signer. The password for the example certs used is just "test"
