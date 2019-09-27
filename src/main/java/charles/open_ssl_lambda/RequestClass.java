package charles.open_ssl_lambda;

public class RequestClass {

    private String bucketName;
    private String signerFile;
    private String keyFile;
    private String certFile;
    private String fileToSign;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getSignerFile() {
        return signerFile;
    }

    public void setSignerFile(String signerFile) {
        this.signerFile = signerFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getCertFile() {
        return certFile;
    }

    public void setCertFile(String certFile) {
        this.certFile = certFile;
    }

    public String getFileToSign() {
        return fileToSign;
    }

    public void setFileToSign(String fileToSign) {
        this.fileToSign = fileToSign;
    }
}
