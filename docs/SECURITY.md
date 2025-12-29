# Security Policy

## Reporting Security Issues

If you discover a security vulnerability, please email security@example.com instead of using the issue tracker. 

**Please do not disclose the vulnerability publicly until we've had a chance to address it.**

## What We Cover

- Authentication vulnerabilities
- Data exposure issues
- SQL injection / code injection
- Crypto/SSL/TLS issues
- Privilege escalation
- Remote code execution

## What We Don't Cover

- Missing security headers (HSTS, CSP, etc.) - N/A for mobile
- Best practices that don't result in security issues
- Social engineering
- Phishing

## Supported Versions

| Version | Supported |
|---------|-----------|
| 1.0.x   | ✅ Current |
| < 1.0   | ❌ Pre-release |

## Security Best Practices for Users

1. **Keep the app updated** - Always update to the latest version
2. **Use strong passwords** - If authentication is added
3. **Don't share collection data** - Keep personal information private
4. **Report suspicious activity** - Let us know immediately

## Privacy & Data

This app:
- ✅ Uses local database for storage
- ✅ Caches card data from public API
- ✅ Does not collect personal analytics
- ✅ Does not share data with third parties
- ✅ Does not require permissions unnecessarily

## Encryption

- API communication uses HTTPS
- Local database is unencrypted (can be encrypted with Room)
- User data is not sent to external servers

## Permissions

The app requests minimal permissions:
- `INTERNET` - For API calls
- `ACCESS_NETWORK_STATE` - To check connection status

## Future Security Improvements

- [ ] Add encryption to local database
- [ ] Implement Firebase Security Rules
- [ ] Add request signing
- [ ] Implement certificate pinning
- [ ] Add security headers
- [ ] Regular security audits

## Contact

For security issues: security@example.com

Thank you for helping keep Pokemon Master Set Tracker secure!
