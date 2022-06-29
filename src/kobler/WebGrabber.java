package kobler;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class WebGrabber {

	public static void main(String[] args) {
	
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(args[0]);
		CloseableHttpResponse response=null;
		try {
			response = httpclient.execute(httpget);
			System.out.println(response.toString());
			System.out.println("======================================");
			HttpEntity entity = response.getEntity();
			/*
			InputStream in = entity.getContent();
			int i=0;
			while((i = in.read()) != -1) {
				System.out.print((char) i);
			}
			*/
			entity.writeTo(System.out);
			System.out.println("\n======================================");
			//in.close();
			response.close();
	
		} catch (Exception exc) {
			exc.printStackTrace();
		} 

	
	}
	
/*	
    private final HttpClientConnectionManager connMgr;
    private volatile boolean shutdown;
	private static PoolingHttpClientConnectionManager connMgr;
	private static HttpClientBuilder clientBuilder;
	//private static IdleConnectionMonitorThread idleThread;
*/	
	

}
