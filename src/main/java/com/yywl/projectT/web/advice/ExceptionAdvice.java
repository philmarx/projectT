package com.yywl.projectT.web.advice;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import com.yywl.projectT.bean.ResultModel;

@ControllerAdvice
public class ExceptionAdvice {
	private static Log log = LogFactory.getLog(ExceptionAdvice.class);

	@ExceptionHandler(Throwable.class)
	@ResponseBody
	public Object exception(Exception e, WebRequest webRequest) {
		if (webRequest.getClass()==ServletWebRequest.class) {
			HttpServletRequest request=((ServletWebRequest) webRequest).getRequest();
			String url=request.getRequestURI();
			log.error(url);
		}
		log.error(e.getMessage());
		return new ResultModel(false, e.getMessage(),null);
	}
}
