package com.cos.security1.config.oauth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.cos.security1.config.auth.PrincipalDetails;
import com.cos.security1.config.oauth.provider.FacebookUserInfo;
import com.cos.security1.config.oauth.provider.GoogleUserInfo;
import com.cos.security1.config.oauth.provider.NaverUserInfo;
import com.cos.security1.config.oauth.provider.OAuth2UserInfo;
import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;


//PrincipalDetails 타입으로 return 하려고 만든 클래스
@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService{
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	//구글로부터 받은 userRequest 데이터에 대한 후처리 되는 함수
	//함수 종료 시 @AuthenticationPrincipal 어노테이션이 만들어진다.
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		System.out.println("getClientRegistration: " + userRequest.getClientRegistration()); //registrationId로 어떤 OAuth로 로그인 했는지 확인 가능.
		System.out.println("getAccessToken: " + userRequest.getAccessToken().getTokenValue());
		
		OAuth2User oAuth2User = super.loadUser(userRequest);
		//구글로그인 버튼 클릭 -> 구글로그인 창 -> 로그인 완료 -> code를 리턴받음(OAuth-Client라이브러리) -> AccessToken 요청
		//userRequest 정보 -> loadUser 함수 호출 -> 구글로부터 회원프로필 받음.
		System.out.println("getAttribute: " + oAuth2User.getAttributes());
		
		// 회원가입을 강제로 진행해볼 예정.
		OAuth2UserInfo oAuth2UserInfo = null;
		if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
			System.out.println("구글 로그인 요청");
			oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
		} else if (userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
			System.out.println("페이스북 로그인 요청");
			oAuth2UserInfo = new FacebookUserInfo(oAuth2User.getAttributes());
		} else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
			System.out.println("네이버 로그인 요청");
			oAuth2UserInfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
		} else {
			System.out.println("우리는 구글과 페이스북과 네이버만 지원해요 ㅎㅎㅎ");
		}
		
		//oauth2 통합
		String provider = oAuth2UserInfo.getProvider();
		String providerId = oAuth2UserInfo.getProviderId();
		String username = provider + "_" + providerId;
		String password = bCryptPasswordEncoder.encode("겟인데어");
		String email = oAuth2UserInfo.getEmail();
		String role = "ROLE_USER";
//		String provider = userRequest.getClientRegistration().getRegistrationId(); //"google"
//		String providerId = oAuth2User.getAttribute("sub");	
//		String username = provider + "_" + providerId;
//		String password = bCryptPasswordEncoder.encode("겟인데어");
//		String email = oAuth2User.getAttribute("email");
//		String role = "ROLE_USER";
		
		User userEntity = userRepository.findByUsername(username);
		if (userEntity == null) {
			System.out.println("OAuth 로그인이 최초입니다.");
			userEntity = User.builder()
					.username(username)
					.password(password)
					.email(email)
					.role(role)
					.provider(provider)
					.providerId(providerId)
					.build();
			userRepository.save(userEntity);
		} else {
			System.out.println("로그인을 이미 한적이 있습니다. 당신은 자동회원가입이 되어 있습니다.");
		}
		
		return new PrincipalDetails(userEntity, oAuth2User.getAttributes()); 
	}
}
