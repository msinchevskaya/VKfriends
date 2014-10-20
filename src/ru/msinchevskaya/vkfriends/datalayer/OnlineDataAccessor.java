package ru.msinchevskaya.vkfriends.datalayer;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

public class OnlineDataAccessor {
	public static final String TAG = "datalayer.OnlineDataAccessor";

	protected static final String ERROR_UNHANDLED_STATUS_CODE_PATTERN = "Unhandled Status Code in Response (%s)";

	// Заждержка, после которых будем считать, что данные больше не придут. Как праивло коннект не должен быть медленным. 
	protected static final int TIMEOUT_CONNECTION_DEFAULT = 2000;
	protected static final int TIMEOUT_SOCKET_DEFAULT = 5000;

	// Буффер для считывания данных из потока
	protected static final int BUFFER_SIZE = 1024;

	// Для уменьшения размера передачи данных, пробуем запросить данные с сервера в жатом виде
	protected static final String HEADER_NAME_ACCEPT_ENCODING = "Accept-Encoding";
	protected static final String HEADER_NAME_CONTENT_ENCODING = HTTP.CONTENT_ENCODING;
	protected static final String HEADER_VALUE_GZIP = "gzip";

	protected static final String HEADER_NAME_USER_AGENT = "User-Agent";
	protected static final String HEADER_USER_AGENT = "Awesome-Octocat-App";
	
	OnlineDataAccessor(){};

	public static String getAsString(String url) throws Exception {
		final HttpGet httpRequest = new HttpGet(url);
		final DefaultHttpClient httpClient = createHttpClient();
		final HttpResponse httpResponse = getResponse(httpClient, httpRequest);

		return getBodyAsString(httpResponse);
	}

	protected static synchronized HttpResponse getResponse(DefaultHttpClient client, HttpUriRequest request) throws ClientProtocolException, IOException, IllegalStateException {
		request.addHeader(HEADER_NAME_ACCEPT_ENCODING, HEADER_VALUE_GZIP);
		request.addHeader(HEADER_NAME_USER_AGENT, HEADER_USER_AGENT);

		final HttpResponse response = client.execute(request);
		final int statusCode = response.getStatusLine().getStatusCode();

		switch (statusCode) {
			//200
			case HttpStatus.SC_OK:
			case HttpStatus.SC_CREATED:
			case HttpStatus.SC_ACCEPTED:
			case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
			case HttpStatus.SC_NO_CONTENT:
			case HttpStatus.SC_RESET_CONTENT:
			case HttpStatus.SC_PARTIAL_CONTENT:
			case HttpStatus.SC_MULTI_STATUS:
				break;

			default:
				unhandleableStatus(statusCode);
				break;
		}

		return response;
	}

	protected static void unhandleableStatus(int statusCode) throws IllegalStateException {
		final String message = String.format(ERROR_UNHANDLED_STATUS_CODE_PATTERN, statusCode);

		throw new IllegalStateException(message);
	}

	protected static DefaultHttpClient createHttpClient() {
		final HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_CONNECTION_DEFAULT);
		HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_SOCKET_DEFAULT);

		return new DefaultHttpClient(httpParameters);
	}

	protected static String getBodyAsString(HttpResponse response) throws IOException {
		final InputStream stream = getStreamFromResponse(response);
		if (stream == null) {
			return null;
		}

		final StringWriter writer = new StringWriter();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			final char[] tmp = new char[BUFFER_SIZE];
			int length;
			while((length = reader.read(tmp)) != -1) {
				writer.write(tmp, 0, length);
			}
		} finally {
			flush(writer);
			close(reader, writer);
		}

		return writer.toString();
	}

	protected static InputStream getStreamFromResponse(HttpResponse response) throws IOException {
		if (response.getEntity() == null) {
			return null;
		}

		final InputStream stream = response.getEntity().getContent();
		final Header contentEncoding = response.getFirstHeader(HEADER_NAME_CONTENT_ENCODING);
		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase(HEADER_VALUE_GZIP)) {
			return new GZIPInputStream(stream);
		}

		return stream;
	}

	protected static void flush(Flushable... flushables) {
		for (Flushable flushable: flushables) {
			if (flushable == null) {
				continue;
			}

			try {
				flushable.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected static void close(Closeable... closeables) {
		for (Closeable closeable: closeables) {
			if (closeable == null) {
				continue;
			}

			try {
				closeable.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}