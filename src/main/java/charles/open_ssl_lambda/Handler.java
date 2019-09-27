package charles.open_ssl_lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class Handler implements RequestHandler<RequestClass, ResponseClass> {

    // requirement openssl smime -sign -signer certificate.pem -inkey private_key.pem -certfile intermediate_certificates.pem -nodetach -outform der -in profiletemplate.mobileconfig -out signedprofile.mobileconfig
    public ResponseClass openSSLHandler(RequestClass requestClass) throws CertificateEncodingException, PEMException, FileNotFoundException, IOException, CertificateException, OperatorCreationException, CMSException {
        Security.addProvider(new BouncyCastleProvider());

        X509CertificateHolder caCertificate = loadCertfile(requestClass.getBucketName(), requestClass.getCertFile());

        JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();
        X509Certificate serverCertificate = certificateConverter.getCertificate(loadSigner(requestClass.getBucketName(), requestClass.getSignerFile()));

        PrivateKeyInfo privateKeyInfo = loadInKey(requestClass.getBucketName(), requestClass.getKeyFile());
        PrivateKey inKey = new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
        ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(inKey);

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        JcaDigestCalculatorProviderBuilder digestProviderBuilder = new JcaDigestCalculatorProviderBuilder().setProvider("BC");
        JcaSignerInfoGeneratorBuilder generatotBuilder = new JcaSignerInfoGeneratorBuilder(digestProviderBuilder.build());

        generator.addSignerInfoGenerator(generatotBuilder.build(sha1Signer, serverCertificate));
        generator.addCertificate(new X509CertificateHolder(serverCertificate.getEncoded()));
        generator.addCertificate(new X509CertificateHolder(caCertificate.getEncoded()));

        CMSProcessableByteArray bytes = new CMSProcessableByteArray(fileToSignAsBytes(requestClass.getBucketName(), requestClass.getFileToSign()));
        CMSSignedData signedData = generator.generate(bytes, true);

        //OpenSSL.pkcs7
        return new ResponseClass(signedData.getEncoded());
    }

    public X509CertificateHolder loadSigner(String bucketName, String fileName) throws FileNotFoundException, IOException {
        PEMParser parser = new PEMParser(new InputStreamReader(loadFileFromS3(bucketName, fileName)));
        return (X509CertificateHolder) parser.readObject();
    }

    public PrivateKeyInfo loadInKey(String bucketName, String fileName) throws FileNotFoundException, IOException {
        PEMParser parser = new PEMParser(new InputStreamReader(loadFileFromS3(bucketName, fileName)));
        return (PrivateKeyInfo) parser.readObject();
    }

    public X509CertificateHolder loadCertfile(String bucketName, String fileName) throws FileNotFoundException, IOException {
        PEMParser parser = new PEMParser(new InputStreamReader(loadFileFromS3(bucketName, fileName)));
        return (X509CertificateHolder) parser.readObject();
    }

    public byte[] fileToSignAsBytes(String bucketName, String fileName) {
        InputStream inputStream = loadFileFromS3(bucketName, fileName);

        try {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            System.out.println("Error reading file to sign.");
            System.out.println(e.getMessage());
            return new byte[0];
        }
    }

    public ResponseClass handleRequest(RequestClass requestClass, Context context) {
        try {
            return openSSLHandler(requestClass);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseClass(null);
        }
    }

    private InputStream loadFileFromS3(String bucketName, String fileName) {
        AmazonS3Client client = new AmazonS3Client();
        S3Object file = client.getObject(bucketName, fileName);
        return file.getObjectContent();
    }

}
