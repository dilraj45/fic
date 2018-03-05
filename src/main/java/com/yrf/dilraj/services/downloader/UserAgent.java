package com.yrf.dilraj.services.downloader;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * {@link UserAgent} helps in defining the User-Agent for Crawler
 *
 */
public class UserAgent implements Serializable {

    public static final String DEFAULT_BROWSER_VERSION = "Mozilla/5.0";

    @Nullable private final String _agentName;
    private final String _userAgentString;

    private UserAgent(@Nullable String _agentName, @NonNull String _userAgentString) {
        this._agentName = _agentName;
        this._userAgentString = _userAgentString;
    }

    /**
     * Obtain the just the user agent name
     *
     * @return User Agent name (String)
     */
    public @Nullable String getAgentName() {
        return _agentName;
    }

    /**
     * Obtain a String representing the user agent characteristics.
     *
     * @return User Agent String
     */
    public String getUserAgentString() {
        return _userAgentString;
    }

    /**
     * Builds a user agent with custom characteristics
     */
    public static class Builder {

        @Nullable private String _agentName;
        @Nullable private String _emailAddress;
        @Nullable private String _webAddress;
        private String _browserVersion = DEFAULT_BROWSER_VERSION;
        @MonotonicNonNull private String _userAgentString;

        public Builder() {
        }

        public Builder setAgentName(String _agentName) {
            this._agentName = _agentName;
            return this;
        }

        public Builder setEmailAddress(String _emailAddress) {
            this._emailAddress = _emailAddress;
            return this;
        }

        public Builder setWebAddress(String _webAddress) {
            this._webAddress = _webAddress;
            return this;
        }

        public Builder setBrowserVersion(String _browserVersion) {
            this._browserVersion = _browserVersion;
            return this;
        }

        public Builder setUserAgentString(String _userAgentString) {
            this._userAgentString = _userAgentString;
            return this;
        }

        /**
         * Creates a string representing the user agent characteristics.
         *
         * @return User Agent String
         */
        @EnsuresNonNull("_userAgentString")
        private void setUserAgentString() {
            // Mozilla/5.0 (compatible; mycrawler/1.0; +http://www.mydomain.com;
            // mycrawler@mydomain.com)
            StringBuilder sb = new StringBuilder();
            sb.append(_browserVersion);
            sb.append(" (compatible; ");
            if (_agentName != null && _agentName.isEmpty())
                sb.append(_agentName);
            sb.append("/");
            if (_webAddress != null && !_webAddress.isEmpty()) {
                sb.append("; +");
                sb.append(_webAddress);
            }
            if (_emailAddress != null && !_emailAddress.isEmpty()) {
                if (_webAddress == null || _webAddress.isEmpty()) {
                    sb.append(";");
                }
                sb.append(" ");
                sb.append(_emailAddress);
            }
            sb.append(")");
            this._userAgentString = sb.toString();
        }

        public UserAgent build() {
            if (_userAgentString == null) {
                setUserAgentString();
            }
            return new UserAgent(_agentName, _userAgentString);
        }

    }
}
