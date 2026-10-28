package dev.bnunes.worklogapi.application.port.in;

import dev.bnunes.worklogapi.domain.model.Log;

public interface RegisterLogUseCase {

    Log register(String accessToken, String content);
}
