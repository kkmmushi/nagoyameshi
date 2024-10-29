package com.example.nagoyameshi.form;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
 public class SignupForm{
	@NotBlank(message="氏名を入力してください")
	private String name;

    
    @NotBlank(message = "電話番号を入力してください。")
    private String phoneNumber;
    
    @NotBlank(message = "メールアドレスを入力してください。")
    @Email(message = "メールアドレスは正しい形式で入力してください。")
    private String email;
    
    @NotBlank(message = "パスワードを入力してください。")
    @Length(min = 8, message = "パスワードは8文字以上で入力してください。")
    private String password;    
    
    @NotBlank(message = "パスワード（確認用）を入力してください。")
    private String passwordConfirmation;    
}
	
