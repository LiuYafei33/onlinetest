package org.base.system.web;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.subject.WebSubject;
import org.apache.shiro.web.util.WebUtils;
import org.base.frame.controller.BaseController;
import org.base.frame.shiro.FrameAuthenticationToken;
import org.base.frame.shiro.ShiroUser;
import org.base.frame.util.CaptchaUtils;
import org.base.frame.util.GlobalStatic;
import org.base.system.entity.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController extends BaseController {

	/**
	 * 首页的映射
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/")
	public String index() throws Exception {
		return super.redirect + "/index";

	}

	/**
	 * 首页的映射
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/index")
	public String index(Model model) throws Exception {
		return "/index";

	}

	/**
	 * 渲染登录/login的界面展示,如果已经登录,跳转到首页,如果没有登录,就渲染login.html
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(Model model, HttpServletRequest request)
			throws Exception {
		// 判断用户是否登录
		if (SecurityUtils.getSubject().isAuthenticated()) {
			return redirect + "/index";
		}
		// 默认赋值message,避免freemarker尝试从session取值,造成异常
		model.addAttribute("message", "");
		return "/login";
	}

	/**
	 * 处理登录提交的方法
	 * 
	 * @param currUser
	 *            自动封装当前登录人的 账号,密码,验证码
	 * @param session
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String loginPost(User currUser, HttpSession session, Model model,
			HttpServletRequest request) throws Exception {
		Subject user = SecurityUtils.getSubject();
		// 系统产生的验证码
		String code = (String) session
				.getAttribute(GlobalStatic.DEFAULT_CAPTCHA_PARAM);
		if (StringUtils.isNotBlank(code)) {
			code = code.toLowerCase().toString();
		}
		// 用户产生的验证码
		String submitCode = WebUtils.getCleanParam(request,
				GlobalStatic.DEFAULT_CAPTCHA_PARAM);
		if (StringUtils.isNotBlank(submitCode)) {
			submitCode = submitCode.toLowerCase().toString();
		}
		// 如果验证码不匹配,跳转到登录
		if (StringUtils.isBlank(submitCode) || StringUtils.isBlank(code)
				|| !code.equals(submitCode)) {
			model.addAttribute("message", "验证码错误!");
			return "/login";
		}
		// 通过账号和密码获取 UsernamePasswordToken token
		FrameAuthenticationToken token = new FrameAuthenticationToken(
				currUser.getAccount(), currUser.getPassword());

		String rememberme = request.getParameter("rememberme");
		if (StringUtils.isNotBlank(rememberme)) {
			token.setRememberMe(true);
		} else {
			token.setRememberMe(false);
		}

		try {
			// 会调用 shiroDbRealm 的认证方法
			// com.yidu.frame.shiro.ShiroDbRealm.doGetAuthenticationInfo(AuthenticationToken)
			user.login(token);
		} catch (UnknownAccountException uae) {
			model.addAttribute("message", "账号不存在!");
			return "/login";
		} catch (IncorrectCredentialsException ice) {
			model.addAttribute("message", "密码错误!");
			return "/login";
		} catch (LockedAccountException lae) {
			model.addAttribute("message", "账号被锁定!");
			return "/login";
		} catch (Exception e) {
			model.addAttribute("message", "未知错误,请联系管理员.");
			return "/login";
		}

		// String sessionId = session.getId();

		// Cache<Object, Object> cache =
		// shiroCacheManager.getCache(GlobalStatic.authenticationCacheName);
		// cache.put(GlobalStatic.authenticationCacheName+"-"+currUser.getAccount(),
		// sessionId);

		/*
		 * Cache<String, Object> cache =
		 * shiroCacheManager.getCache(GlobalStatic.shiroActiveSessionCacheName);
		 * Serializable oldSessionId = (Serializable)
		 * cache.get(currUser.getAccount()); if(oldSessionId!=null){ Subject
		 * subject=new Subject.Builder().sessionId(oldSessionId).buildSubject();
		 * subject.logout(); } cache.put(currUser.getAccount(),
		 * session.getId());
		 */

		return redirect + "/index";
	}

	/**
	 * 没有权限
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/unauth")
	public String unauth(Model model) throws Exception {
		if (SecurityUtils.getSubject().isAuthenticated() == false) {
			return redirect + "/login";
		}
		return "/unauth";

	}

	/**
	 * 退出
	 * 
	 * @param request
	 */
	@RequestMapping(value = "/logout")
	public void logout(HttpServletRequest request) {
		Subject subject = SecurityUtils.getSubject();
		if (subject != null) {
			subject.logout();
		}
		request.getSession().invalidate();
	}

	/**
	 * 自动登录,无需账号密码,测试代码,默认注释，根据实际修改
	 * 
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	// @RequestMapping(value = "/auto/login")
	public String autologin(Model model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ShiroUser shiroUser = new ShiroUser();
		shiroUser.setId("admin");
		shiroUser.setName("admin");
		shiroUser.setAccount("admin");
		SimplePrincipalCollection principals = new SimplePrincipalCollection(
				shiroUser, GlobalStatic.authorizingRealmName);
		WebSubject.Builder builder = new WebSubject.Builder(request, response);
		builder.principals(principals);
		builder.authenticated(true);
		WebSubject subject = builder.buildWebSubject();
		ThreadContext.bind(subject);
		return redirect + "/index";
	}

	/**
	 * 生成验证码
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/getCaptcha")
	public void getCaptcha(HttpSession session, HttpServletResponse response)
			throws IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);

		CaptchaUtils tool = new CaptchaUtils();
		StringBuffer code = new StringBuffer();
		BufferedImage image = tool.genRandomCodeImage(code);
		session.removeAttribute(GlobalStatic.DEFAULT_CAPTCHA_PARAM);
		session.setAttribute(GlobalStatic.DEFAULT_CAPTCHA_PARAM,
				code.toString());

		// 将内存中的图片通过流动形式输出到客户端
		ImageIO.write(image, "JPEG", response.getOutputStream());
		return;
	}

}
