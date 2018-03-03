package com.yrf.dilraj.services.downloader;

import java.io.Serializable;


public class UserAgent implements Serializable {

    public static final String DEFAULT_BROWSER_VERSION = "Mozilla/5.0";

    private final String _agentName;
    private final String _userAgentString;

    private UserAgent(Builder builder) {
        this._agentName = builder._agentName;
        this._userAgentString = builder._userAgentString;
    }

    /**
     * Obtain the just the user agent name
     *
     * @return User Agent name (String)
     */
    public String getAgentName() {
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

        private String _agentName;
        private String _emailAddress;
        private String _webAddress;
        private String _browserVersion = DEFAULT_BROWSER_VERSION;
        private String _userAgentString;

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
        private String createUserAgentString() {
            // Mozilla/5.0 (compatible; mycrawler/1.0; +http://www.mydomain.com;
            // mycrawler@mydomain.com)
            StringBuilder sb = new StringBuilder();
            sb.append(_browserVersion);
            sb.append(" (compatible; ");
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
            return sb.toString();
        }

        public UserAgent build() {
            if (_userAgentString == null) {
                _userAgentString = createUserAgentString();
            }
            return new UserAgent(this);
        }

    }
}
