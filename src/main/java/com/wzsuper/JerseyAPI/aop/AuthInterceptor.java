package com.wzsuper.JerseyAPI.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.wzsuper.JerseyAPI.Server.annotation.Register;
import com.wzsuper.JerseyAPI.redis.RedisAPI;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.wzsuper.JerseyAPI.Beans.PR;

import net.sf.json.JSONObject;

public class AuthInterceptor {

	final static Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

	private Map<?, ?> inputParamMap = null; // 传入参数
	private Map<String, Object> outputParamMap = null; // 存放输出结果
	private Register register = null;
	private boolean authPass = true;

	public void before(JoinPoint jp) {
		MethodSignature methodSignature = (MethodSignature) jp.getSignature();
		Method method = methodSignature.getMethod();
		if (method.isAnnotationPresent(Register.class)) {
			register = method.getAnnotation(Register.class);
			if (!register.stateless()) {
				/**
				 * 需要鉴权
				 */
				authPass = false;
				ServletRequestAttributes ra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
				HttpServletRequest request = ra.getRequest();
				inputParamMap = request.getParameterMap();
				JSONObject param = JSONObject.fromObject(inputParamMap);
				if (param.has("accesstoken")) {
					String accessToken = param.getString("accesstoken");
					RedisAPI redis = new RedisAPI(10);
					String value = redis.get(accessToken);
					if (value != null && !"".equals(value)) {
						//JSONObject Resource = JSONObject.fromObject(value);
						authPass = true;
					}
				}

			}
		}
	}

	public Object around(ProceedingJoinPoint pjp) {
		Object result = null;
		try {
			if(!authPass)
			{
				return new PR(0,"无权调用该接口",null);
			}
			result = pjp.proceed();
			outputParamMap = new HashMap<String, Object>();
			outputParamMap.put("result", result);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}
}
