package com.example.nagoyameshi.controller;

import javax.validation.Valid;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.event.SignupEventPublisher;
import com.example.nagoyameshi.form.LoginForm;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.security.UserDetailsServiceImpl;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;


@Controller
public class AuthController{
	
	private final UserDetailsServiceImpl userDetailsService;
	private final UserService userService;
	private final SignupForm signupForm;
	private final SignupEventPublisher signupEventPublisher;
	private final VerificationTokenService verificationTokenService;
	
	
	@Autowired
	public AuthController(UserDetailsServiceImpl userDetailsService, UserService userService, SignupForm signupForm,SignupEventPublisher signupEventPublisher,VerificationTokenService verificationTokenService) {
		this.userDetailsService = userDetailsService;
		this.userService = userService;
		this.signupForm = signupForm;
		this.signupEventPublisher = signupEventPublisher;
		this.verificationTokenService =verificationTokenService;
		
	}
	
	 //----------------------------------------ログイン関連-------------------------------------------
	
	
	@GetMapping("/session")
	public String getSessionInfo(HttpSession session, Model model) {
	    Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");

	    if (securityContext != null) {
	        System.out.println("セッションにユーザー情報があります: " + securityContext);
	    } else {
	        System.out.println("セッションにユーザー情報がありません。");
	    }

	    model.addAttribute("user", securityContext);
	    return "auth/login"; // セッション情報を表示するテンプレート
	}
	
	
	
	@GetMapping("/login")
		public String loginForm(Model model){
			model.addAttribute("loginForm", new LoginForm());		
			
			
			return "auth/login";
		}
	
	
	
	
	
	@PostMapping("/login")
	public String login(@Valid LoginForm loginForm,BindingResult result,Model model) {
	System.out.println("ログインメソッドが呼ばれました"); 
	System.out.println("LoginForm: " + loginForm);
	try {
    System.out.println("ログイン試行: " + loginForm.getEmail());
	
		if(result.hasErrors()) {
			System.out.println("バリデーションエラーがあります: " + result.getErrorCount());
			   result.getAllErrors().forEach(error -> {
			        System.out.println("エラー: " + error.getDefaultMessage());
			    });
			return "auth/login";
		}

	    System.out.println("ログイン試行: " + loginForm.getEmail());     //ログイン試行のログ
	    
		
	
	boolean isAuthenticated = userDetailsService.authenticate(loginForm.getEmail(),loginForm.getPassword());

	System.out.println("認証結果: " + isAuthenticated);
	
	if(!isAuthenticated) {
		System.out.println("ログイン失敗: メールまたはパスワードが正しくありません");
		model.addAttribute("error","メールまたはパスワードが正しくありません");
		return "auth/login";
	}
	
	System.out.println("ログイン成功: " + loginForm.getEmail());
	return"redirect:/index";
}catch (Exception e) {
    System.out.println("例外が発生しました: " + e.getMessage());
    e.printStackTrace();
    model.addAttribute("error", "システムエラーが発生しました");
    return "auth/login";
}
	}
	
	
	 //----------------------------------------会員登録関連-----------------------------------------------
	
	
	@GetMapping("/signup")
	 public String showsignupForm(Model model) {
		
	
		model.addAttribute("signupForm", new SignupForm());
		return "auth/signup";
	}
	
	
	@PostMapping("/signup")
	public String signup(@Valid  SignupForm signupForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpServletRequest httpServletRequest) {
        // メールアドレスが登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
        if (userService.isEmailRegistered(signupForm.getEmail())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
            bindingResult.addError(fieldError);                       
        }    
        
        // パスワードとパスワード（確認用）の入力値が一致しなければ、BindingResultオブジェクトにエラー内容を追加する
        if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
            bindingResult.addError(fieldError);
        }        
        
        if (bindingResult.hasErrors()) {
        	
  
        	    System.out.println("エラー数: " + bindingResult.getErrorCount());
        	    bindingResult.getAllErrors().forEach(error -> {
        	        System.out.println("エラー: " + error.getDefaultMessage());
        	    });
            return "auth/signup";
        }
	
        
        User createdUser = userService.create(signupForm);
        String requestUrl = new String(httpServletRequest.getRequestURL());
        System.out.println("Request URL: " + requestUrl);
        signupEventPublisher.publishSignupEvent(createdUser, requestUrl);
        redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。");        
        
        return "redirect:/auth/verify";
        
	}
	
	@GetMapping("/auth/verify")
	public String verifyPage(@ModelAttribute("successMessage") String successMessage, Model model) {
	    model.addAttribute("successMessage", successMessage);
	    return "auth/verify"; 
	}
	
	
     @GetMapping("/signup/verify")
	public String verify(@RequestParam(name="token")String token, Model model) {
    	 VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
    	
    	 if(verificationToken !=null) {
    		 User user = verificationToken.getUser();
    		 userService.enableUser(user);
    		 String successMessage ="会員登録が完了しました。";
    		 model.addAttribute("successMessage",successMessage);
    	 }else {
    		 String errorMessage ="トークンが無効です。";
    		 model.addAttribute("errorMessage",errorMessage);
    	 }
    	 return "auth/verify";
	}
	
	
}