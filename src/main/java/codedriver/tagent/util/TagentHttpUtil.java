package codedriver.tagent.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TagentHttpUtil {
	protected static Logger logger = LoggerFactory.getLogger(TagentHttpUtil.class);

	public static byte[] postFileWithParam(String urlStr, Map<String, String> params, List<File> fileList) throws Exception {
		String BOUNDARY = "----MyFormBoundarySMFEtUYQG6r5B920";
		HttpURLConnection hc = null;
		ByteArrayOutputStream bos = null;
		InputStream is = null;
		byte[] res = null;
		try {
			URL url = new URL(urlStr);
			hc = (HttpURLConnection) url.openConnection();

			hc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			hc.setRequestProperty("Charset", String.valueOf(StandardCharsets.UTF_8));
			hc.setDoOutput(true);
			hc.setDoInput(true);
			hc.setUseCaches(false);
			hc.setRequestMethod("POST");

			OutputStream outStream = hc.getOutputStream();
			StringBuffer resSB = new StringBuffer("\r\n");
			String endBoundary = "\r\n--" + BOUNDARY + "--\r\n";
			// strParams 1:key 2:value
			if (params != null) {
				Set<String> keySet = params.keySet();
				for (String key : keySet) {
					String value = params.get(key);
					resSB.append("Content-Disposition: form-data; name=").append(key).append("\r\n").append("\r\n").append(value).append("\r\n").append("--").append(BOUNDARY).append("\r\n");
				}
			}
			String boundaryMessage = resSB.toString();

			outStream.write(("--" + BOUNDARY + boundaryMessage).getBytes(StandardCharsets.UTF_8));

			resSB = new StringBuffer();
			if (fileList != null) {
				for (int i = 0, num = fileList.size(); i < num; i++) {
					File file = fileList.get(i);
					String fileName = file.getName();
					resSB.append("Content-Disposition: form-data; name=").append(fileName).append("; filename=").append(fileName).append("\r\n").append("Content-Type:multipart/form-data ").append("\r\n\r\n");

					outStream.write(resSB.toString().getBytes(StandardCharsets.UTF_8));
					// 开始写文件
					DataInputStream in = new DataInputStream(new FileInputStream(file));
					int bytes = 0;
					byte[] bufferOut = new byte[1024 * 5];
					while ((bytes = in.read(bufferOut)) != -1) {
						outStream.write(bufferOut, 0, bytes);
					}

					if (i < num - 1) {
						outStream.write(endBoundary.getBytes(StandardCharsets.UTF_8));
					}

					in.close();
				}

			}

			// 3.最后写结尾
			outStream.write(endBoundary.getBytes(StandardCharsets.UTF_8));
			outStream.close();

			int ch;
			is = hc.getInputStream();
			bos = new ByteArrayOutputStream();
			while ((ch = is.read()) != -1) {
				bos.write(ch);
			}
			res = bos.toByteArray();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("发送请求到：" + urlStr + "失败");
		} finally {
			try {
				if (bos != null)
					bos.close();
				if (is != null)
					is.close();
			} catch (Exception e2) {
				logger.error(e2.getMessage(), e2);
			}
		}
		return res;
	}

	public static String uploadToUrl(String urlStr, String fileName, File file, Map<String, String> params) throws Exception {

		String uri = UriComponentsBuilder.fromUriString(urlStr).queryParam("form", "upload").build().encode().toString();

		InputStream in = new FileInputStream(file);

		URL urlObj = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);

		// 设置请求头信息
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Charset", String.valueOf(StandardCharsets.UTF_8));

		// 设置边界
		String BOUNDARY = "----------" + System.currentTimeMillis();
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

		// 请求正文信息
		StringBuilder sb = new StringBuilder();
		sb.append("--");
		sb.append(BOUNDARY);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data;name=\"file\";filename=\"").append(fileName).append("\"\r\n");

		// 以流的方式上传
		sb.append("Content-Type:application/octet-stream\r\n\r\n");
		byte[] head = sb.toString().getBytes(StandardCharsets.UTF_8);
		// 获得输出流
		OutputStream out = new DataOutputStream(con.getOutputStream());
		// 输出表头
		out.write(head);
		// 把文件已流文件的方式 推入到url中
		DataInputStream dataIn = new DataInputStream(in);
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while ((bytes = dataIn.read(bufferOut)) != -1) {
			out.write(bufferOut, 0, bytes);
		}
		in.close();
		// 结尾部分
		byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes(StandardCharsets.UTF_8);// 定义最后数据分隔线
		out.write(foot);
		out.flush();
		out.close();

		// 读取返回数据
		StringBuilder strBuf = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			strBuf.append(line).append("\n");
		}
		reader.close();
		con.disconnect();
		con = null;
		return strBuf.toString();
	}

	public static void download(String url, OutputStream out) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		InputStream in = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpclient.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			in = entity.getContent();
			IOUtils.copy(in, out);
			out.flush();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("发送请求到：" + url + "失败");
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static void download(String url, HttpServletResponse response) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		InputStream in = null;
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			HttpGet httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpclient.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			Header[] headers = httpResponse.getAllHeaders();
			for (Header header : headers) {
				response.setHeader(header.getName(), header.getValue());
			}
			in = entity.getContent();
			IOUtils.copy(in, out);
			out.flush();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("发送请求到：" + url + "失败");
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static String get(String url, Map<String, String> header) {
		// 实例化httpclient
		CloseableHttpClient httpclient = HttpClients.createDefault();
		// 实例化get方法
		HttpGet httpget = new HttpGet(url);
		if (header != null && !header.isEmpty()) {
			Set<String> keys = header.keySet();
			for (String key : keys) {
				httpget.setHeader(key, header.get(key));
			}
		}
		// 请求结果
		CloseableHttpResponse response = null;
		String content = "";
		try {
			// 执行get方法
			response = httpclient.execute(httpget);
			if (response.getStatusLine().getStatusCode() == 200) {
				content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("发送请求到：" + url + "失败");
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return content;
	}

	public static String post(String url, Map<String, String> params, Map<String, String> header, Boolean isPayload) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		if (header != null && !header.isEmpty()) {
			Set<String> keys = header.keySet();
			for (String key : keys) {
				httpPost.setHeader(key, header.get(key));
			}
		}
		// 结果
		CloseableHttpResponse response = null;
		String content = "";
		try {
			if (!isPayload) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				Set<String> keySet = params.keySet();
				for (String key : keySet) {
					nvps.add(new BasicNameValuePair(key, params.get(key)));
				}
				UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8);
				httpPost.setEntity(uefEntity);
			} else {
				httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
				StringEntity entity = new StringEntity(JSONObject.toJSONString(params), StandardCharsets.UTF_8);
				httpPost.setEntity(entity);
			}
			response = httpclient.execute(httpPost);
			content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("发送请求到：" + url + "失败");
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return content;
	}
}