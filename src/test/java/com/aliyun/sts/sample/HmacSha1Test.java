package com.aliyun.sts.sample;

import java.io.UnsupportedEncodingException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

public class HmacSha1Test {
	private static final String MAC_NAME = "HmacSHA1";
	private static final String ENCODING = "UTF-8";

	@Test
	public void main() {

		try {
			// hmac-sha1
			String s = "GET\n\n\n" + "Thu, 09 Mar 2017 02:51:08 GMT" + "\n"
					+ "x-oss-security-token:CAIS0AN1q6Ft5B2yfSjIopHtcvjAi6pQz7G+TEyAqjc0OtVpqo7sizz2IHlIfHltCOAWsv8+lWBT5/celrh+W4NIX0rNaY5t9ZlN9wqkbtI4eWBULv9W5qe+EE2/VjQdtq27OpflLr70fvOqdCqL9Etayqf7cjOPRkGsNYbz57dsctUQWHvfD19BH8wECQZ+j8UYOHDNT9zPVCTnmW3NFkFllxNhgGdkk8SFz9ab9wDVgS+0qKMcrJ+jJYO/PYs+Zc0nC4nlg7QtJ/Gain4LtUpS7qB0gadY5XW1RLj/bnBV5xKZSbu2lvRkMA5+YIUjBqdAt4KSvPZku+vV5c2VrhFWJrNtTjj4ToKty9emYOSyLYQVeLL2J3nq2NKCPYWPlWFCW38AMx5QcNcMM2J5DQdWKgvXMai64lvHEF3BE6GOy/MxyoEnjQexp4LSeQjQG+7Iin1CasRiPVsyMBwb1nD9NbIdfhYLIwM3V+bFD6cLNUoG8f244lWJB3Y7lCEM4ceTPa2G5votDqzkRY9D3IYnY5BLjnAnVVyfScj10xtMJDE0EeYKi/i8acTuueOfs+yXYP/bDPccoUldcj3XoCeLRGlZMSrr/NonZEoUEHFab0Q1mRqAAWvj8ItPRt/6NelaSJmj7E5ycbvaaGQX0l7KQmO3toEmbCDJOZ21YZy1ofwKJLL1RdUyN5je7j6T5SYT8M2lx7ISaz8F06sRuHYOvZegHvbdvpL/ZiGHG5x0p8WW787tUJ7NZ6saCvEeXTDu7AfznH9Pe5PWg6ALsWDzuSP6qtxb\n"
					+ "/projectt/user/10000000002/avatar";
			byte[] hmacSHA1Encrypt = HmacSHA1Encrypt("CXedEt8aycp6jdgk7AGRyxSTdhjJYfLt7viihvFKAjoH", s);
			System.out.println("hmacSHA1Encrypt:  " + new String(hmacSHA1Encrypt));
			// base64
			byte[] base64 = Base64.encodeBase64(hmacSHA1Encrypt);
			System.out.println("base64:  " + new String(base64));
			System.out.println("OSS STS.CRX9BtfuqxsUNj1N7a6ZEEKGi:" + new String(base64));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
		byte[] data = encryptKey.getBytes(ENCODING);
		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
		// 生成一个指定 Mac 算法 的 Mac 对象
		Mac mac = Mac.getInstance(MAC_NAME);
		// 用给定密钥初始化 Mac 对象
		mac.init(secretKey);

		byte[] text = encryptText.getBytes(ENCODING);
		// 完成 Mac 操作
		return mac.doFinal(text);
	}

}
