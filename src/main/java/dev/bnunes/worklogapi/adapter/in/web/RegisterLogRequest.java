package dev.bnunes.worklogapi.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Only {@code content} is read: the owner comes from the JWT and the rest is assigned on the server. */
record RegisterLogRequest(@NotBlank @Size(max = 140) String content) {
}
