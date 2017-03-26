package com.wzsuper.JerseyAPI.aop;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.wzsuper.JerseyAPI.Beans.LogMssage;
import com.wzsuper.JerseyAPI.Server.exception.APIException;
import com.wzsuper.JerseyAPI.Server.annotation.Register;
import com.wzsuper.JerseyAPI.Utils.NullOrEmptyUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import net.sf.json.JSONObject;


public class LogInterceptor {
	
	final static Logger logger = LoggerFactory.getLogger(LogInterceptor.class);
	
	private String requestPath = null;
	private String httpMethod = null;
	private Map<String, Object> outputParamMap = null;
	private long startTimeMillis = 0; 
	private long endTimeMillis = 0;
	private Register register = null;
	private LogMssage logMssage = new LogMssage();
	private AmqpTemplate amqpTemplate;
	private String routingkey;

	public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	public void setRoutingkey(String routingkey) {
		this.routingkey = routingkey;
	}

	@SuppressWarnings("unchecked")
	public void befor(JoinPoint jp){
		startTimeMillis = System.currentTimeMillis();
		logMssage.setStarttime(DateFormatUtils.format(new Date(startTimeMillis), "yyyy-MM-dd HH:mm:ss.SSS"));
	}
	
	public void after(JoinPoint call){
		if(register != null){
			logger.info("end {}, totaltime:{}ms", register.name(), endTimeMillis - startTimeMillis);
		}
	}
	
	public Object around(ProceedingJoinPoint pjp) throws Throwable{
		Object result = null;
		MethodSignature methodSignature = (MethodSignature) pjp.getSignature();  
		Method method = methodSignature.getMethod();
		outputParamMap = new HashMap<String, Object>();
		if(method.isAnnotationPresent(Register.class)){
			register = method.getAnnotation(Register.class);
			ServletRequestAttributes ra = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
			HttpServletRequest request = ra.getRequest();
			requestPath = request.getRequestURI();
			if(NullOrEmptyUtil.isNotEmpty(request.getQueryString())){
				requestPath = String.format("%s?%s", requestPath, request.getQueryString());
			}
			httpMethod = request.getMethod();
			logMssage.setName(register.name());
			logMssage.setVersion(register.version());
			logMssage.setHttpMethod(httpMethod);
			logMssage.setUrl(requestPath);
			logger.info("begin {}, version:{}, httpMethod:{}, path:{}", register.name(), register.version(), httpMethod, requestPath);
			result = pjp.proceed();
			outputParamMap.put("result", result);
		}else{
			throw new APIException(001,"服务未注册");
		}
		return result;
	}
	
	public void AfterReturning(){
		SendLogMsg();
	}
	
	public void AfterThrowing (Exception e) throws Throwable{
		logMssage.setError(e);
		SendLogMsg();
	}

	private void SendLogMsg(){
		endTimeMillis = System.currentTimeMillis();
		logMssage.setEndtime(DateFormatUtils.format(new Date(endTimeMillis), "yyyy-MM-dd HH:mm:ss.SSS"));
		logMssage.setTotaltime(endTimeMillis - startTimeMillis);
		if(amqpTemplate != null){
			amqpTemplate.convertAndSend(routingkey, JSONObject.fromObject(logMssage).toString());
		}
	}

}
