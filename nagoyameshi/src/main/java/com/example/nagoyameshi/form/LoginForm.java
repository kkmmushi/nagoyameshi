package com.example.nagoyameshi.form;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LoginForm{
	@NotBlank(message="メールアドレスは必須です")
	@Email(message="有効なメールアドレスを入力してください")
	private String email;
	
	@NotBlank(message="パスワードは必須です")
	private String password;
	
    @Override
    public String toString() {
        return "LoginForm{" +
               "email='" + email + '\'' +
               ", password='" + password + '\'' +
               '}';
    }

	
}