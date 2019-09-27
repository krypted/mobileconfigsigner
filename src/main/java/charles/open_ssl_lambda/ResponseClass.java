package charles.open_ssl_lambda;

public class ResponseClass {

    private byte[] response;

    public ResponseClass(byte[] response) {
        this.response = response;
    }

    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }
}
