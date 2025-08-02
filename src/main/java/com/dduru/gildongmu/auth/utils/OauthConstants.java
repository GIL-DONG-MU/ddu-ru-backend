package com.dduru.gildongmu.auth.utils;

public final class OauthConstants {

    public static final class Google {
        public static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
        public static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
        public static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
        public static final String PEOPLE_API_URL = "https://people.googleapis.com/v1/people/me";
        public static final String SCOPE = "openid email profile https://www.googleapis.com/auth/user.birthday.read";

        public static final String PERSON_FIELDS = "genders,birthdays,phoneNumbers";

        private Google() {}
    }

    public static final class Kakao {
        public static final String AUTH_URL = "https://kauth.kakao.com/oauth/authorize";
        public static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
        public static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
        public static final String SCOPE = "openid,profile_nickname,profile_image,account_email,gender,age_range,phone_number";

        private Kakao() {}
    }

    public static final class Common {
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String CONTENT_TYPE_HEADER = "Content-Type";
        public static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
        public static final String GRANT_TYPE_AUTH_CODE = "authorization_code";
        public static final String RESPONSE_TYPE_CODE = "code";

        private Common() {}
    }

    private OauthConstants() {}
}
