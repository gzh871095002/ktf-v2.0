package com.kivi.dashboard.shiro;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 输错5次密码锁定半小时
 *
 */
@Slf4j
public class RetryLimitCredentialsMatcher extends HashedCredentialsMatcher {
	private Cache<String, AtomicInteger> passwordRetryCache;

	public RetryLimitCredentialsMatcher(CacheManager cacheManager) {
		passwordRetryCache = cacheManager.getCache("passwordRetryCache");
	}

	@Override
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		String			username	= (String) token.getPrincipal();
		// retry count + 1
		AtomicInteger	retryCount	= passwordRetryCache.get(username);
		if (retryCount == null) {
			retryCount = new AtomicInteger(0);
			passwordRetryCache.put(username, retryCount);
		}
		if (retryCount.incrementAndGet() > 5) {
			// if retry count > 5 throw
			log.error("用户名: {} 密码连续输入错误超过5次，锁定半小时！", username);
			throw new ExcessiveAttemptsException("用户名: " + username + " 密码连续输入错误超过5次，锁定半小时！");
		} else {
			passwordRetryCache.put(username, retryCount);
		}

		boolean matches = super.doCredentialsMatch(token, info);
		if (matches) {
			// clear retry count
			passwordRetryCache.remove(username);
		}
		return matches;
	}
}
