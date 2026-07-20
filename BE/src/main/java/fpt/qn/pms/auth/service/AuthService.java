package fpt.qn.pms.auth.service;

import fpt.qn.pms.auth.dto.LoginRequest;
import fpt.qn.pms.auth.dto.LoginResponse;
import fpt.qn.pms.auth.dto.RefreshTokenRequest;
import fpt.qn.pms.auth.dto.RefreshTokenResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
