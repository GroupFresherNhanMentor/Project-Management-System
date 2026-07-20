package fpt.qn.pms.auth.service;

import fpt.qn.pms.auth.dto.request.LoginRequest;
import fpt.qn.pms.auth.dto.request.RefreshTokenRequest;
import fpt.qn.pms.auth.dto.response.LoginResponse;
import fpt.qn.pms.auth.dto.response.RefreshTokenResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request, String authHeader);
}
