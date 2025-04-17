package org.example.expert.Aop;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LogAop {

	private final ObjectMapper objectMapper;

	public LogAop(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	@Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
	"execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
	public void adminOnlyEndpoints() {}

	@Around("adminOnlyEndpoints()")
	public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Method method = sig.getMethod();
		Object[] args = pjp.getArgs();
		Parameter[] params = method.getParameters();

		ServletRequestAttributes attrs =
			(ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = attrs.getRequest();

		String url = request.getRequestURI();
		LocalDateTime requestTime = LocalDateTime.now();
		Long userId = (Long)request.getAttribute("userId");
		String reqJson = "";

		for(int i = 0; i < params.length; i ++) {
			if(params[i].isAnnotationPresent(RequestBody.class)) {
				reqJson = objectMapper.writeValueAsString(args[i]);
			}
		}

		Object obj = pjp.proceed();
		String resJson=objectMapper.writeValueAsString(obj);

		log.info("[{}] method={} user={} url={} RequestBody={} ResponseBody : {}",
			requestTime, request.getMethod(), userId, url, reqJson, resJson);

		return obj;
	}
}
