package com.thehomearchive.library.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Annotation to inject the current authenticated user's principal.
 * Meta-annotation for {@link AuthenticationPrincipal} with specific configuration
 * for our UserPrincipal interface.
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}