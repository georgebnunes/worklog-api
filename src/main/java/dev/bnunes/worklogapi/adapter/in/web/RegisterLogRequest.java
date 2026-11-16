package dev.bnunes.worklogapi.adapter.in.web;

/** Only {@code content} is read: the owner comes from the JWT and the rest is assigned on the server. */
record RegisterLogRequest(String content) {
}
